package com.vodokanal.accounting.util;

import com.vodokanal.accounting.dto.AccountUpdateDto;
import com.vodokanal.accounting.dto.TariffDto;
import com.vodokanal.accounting.entity.AccountEntity;
import com.vodokanal.accounting.entity.MeterEntity;
import com.vodokanal.accounting.entity.ServiceEntity;
import com.vodokanal.accounting.entity.TariffEntity;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.query.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Function;

@Component
public class DatabaseRepository {
    private final SessionFactory sessionFactory;
    private final MappingUtil mappingUtil;
    private static final Logger log = LoggerFactory.getLogger(DatabaseRepository.class);


    public DatabaseRepository(SessionFactory sessionFactory, MappingUtil mappingUtil) {
        this.sessionFactory = sessionFactory;
        this.mappingUtil = mappingUtil;
    }

    private <T> T executeTransaction(Function<Session, T> executable) {
        Transaction transaction = null;

        try (Session session = sessionFactory.openSession()) {
            session.setJdbcBatchSize(10);
            transaction = session.getTransaction();
            transaction.begin();

            var entityList = executable.apply(session);

            transaction.commit();

            log.info("В базу данных добавленна/обновлена запись: {}", executable); // переделать!  в лог пишет херню

            return entityList;
        } catch (ConstraintViolationException e) {
            throw new RuntimeException(e);

        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }

            throw new RuntimeException(e);
        }
    }

    private <T> T executeQuery(Function<Session, T> executable) {
        try (Session session = sessionFactory.openSession()) {
            return executable.apply(session);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<AccountEntity> addAccount(List<AccountEntity> accountEntityList) {
        return executeTransaction(session -> {
            for (int i = 0; i < accountEntityList.size(); i++) {
                session.persist(accountEntityList.get(i));

                if ((i + 1) % session.getJdbcBatchSize() == 0) {
                    session.flush();
                    session.clear();
                }
            }

            return accountEntityList;
        });
    }

    public AccountUpdateDto updateAccount(long id, AccountUpdateDto accountUpdateDto) {
        return executeTransaction(session -> {
            AccountEntity accountEntity = session.get(AccountEntity.class, id);

            if (accountEntity == null) {
                throw new NoSuchElementException("Лицевой счет не найден");
            }

            AccountEntity updatedAccountEntity = mappingUtil.mapAccountToUpdate(accountUpdateDto,
                    accountEntity);

            session.merge(updatedAccountEntity);

            return accountUpdateDto;
        });
    }

    public TariffDto addTariff(TariffDto tariffDto) {
        return executeTransaction(session -> {
            ServiceEntity serviceEntity = getServiceEntityById(session, tariffDto.service());

            if (serviceEntity == null) {
                throw new NoSuchElementException("Услуга для устанавливаемого тарифа не найдена");
            }

            TariffEntity tariffEntity = mappingUtil.mapTariffDtoToEntity(tariffDto, serviceEntity);

            session.persist(tariffEntity);

            return mappingUtil.mapTariffEntityToDto(tariffEntity);
        });
    }

    public ServiceEntity getServiceEntityById(Session session, long id) {
        return session.get(ServiceEntity.class, id);
    }

    //Добавление услуг по умолчанию при запуске программы, убрать на релизе
    public void addService(List<ServiceEntity> serviceEntityList) {
        executeTransaction(session -> {
            serviceEntityList.forEach(session::persist);

            return "";
        });
    }

    public String getAccountDataByChatID(long chatID) {
        return executeQuery(session -> {
            List<AccountEntity> resultList = getAccountEntityByChatID(session, chatID);

            if (resultList.isEmpty()) {
                return "";
            }

            AccountEntity accountEntity = resultList.getFirst();

            Map<String, String> responseMap = new HashMap<>();
            responseMap.put("accountNumber", accountEntity.getNumber());
            responseMap.put("balance", String.valueOf(accountEntity.getBalance()));

            return mappingUtil.mapObjectToJson(responseMap);
        });
    }

    public List<AccountEntity> getAccountEntityByChatID(Session session, long chatID) {
        String hql = "FROM AccountEntity WHERE telegramID = :chatID";
        Query<AccountEntity> query = session.createQuery(hql, AccountEntity.class);
        query.setParameter("chatID", chatID);

        return query.getResultList();
    }

    public String bindTelegramID(long chatID, String accountNumber) {
        return executeTransaction(session -> {
            String hql = "FROM AccountEntity WHERE number = :accountNumber";
            Query<AccountEntity> query = session.createQuery(hql, AccountEntity.class);
            query.setParameter("accountNumber", accountNumber);

            List<AccountEntity> resultList = query.getResultList();

            if (resultList.isEmpty()) {
                return "";
            }

            AccountEntity updateAccountEntity = resultList.getFirst();
            updateAccountEntity.setTelegramID(chatID);
            session.merge(updateAccountEntity);

            log.info("К лицевому счету {} привязан телеграм id {}", accountNumber, chatID);

            return String.valueOf(updateAccountEntity.getBalance());
        });
    }

    private MeterEntity getMeterEntityByNumberAndAccount(Session session, AccountEntity accountEntity, String meterNumber) {
        String hql = "FROM MeterEntity WHERE serialNumber = :meterNumber AND account = :accountEntity";
        Query<MeterEntity> query = session.createQuery(hql, MeterEntity.class);
        query.setParameter("meterNumber", meterNumber);
        query.setParameter("accountEntity", accountEntity);

        if (query.getResultList().isEmpty()) {
            return null;
        } else {
            return query.getResultList().getFirst();
        }
    }

    public String addMeter(long chatID, String meterJson) {
        return executeTransaction(session -> {
            Map<String, String> meterMap = mappingUtil.parseJsonToHashMap(meterJson);
            AccountEntity accountEntity = getAccountEntityByChatID(session, chatID).getFirst();

            MeterEntity meterEntity = getMeterEntityByNumberAndAccount(
                    session,
                    accountEntity,
                    meterMap.get("serialNumber"));

            if (meterEntity != null) {
                return "";
            }

            ServiceEntity serviceEntity = getServiceEntityById(session, Long.parseLong(meterMap.get("service")));

            meterEntity = new MeterEntity();
            meterEntity.setSerialNumber(meterMap.get("serialNumber"));
            meterEntity.setInitialValue(new BigDecimal(meterMap.get("initialValue")));
            meterEntity.setService(serviceEntity);
            meterEntity.setAccount(accountEntity);

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
            LocalDate date = LocalDate.parse(meterMap.get("expirationDate"), formatter);
            meterEntity.setValidThru(date);

            session.persist(meterEntity);

            return String.valueOf(meterEntity.getId());
        });
    }
}
