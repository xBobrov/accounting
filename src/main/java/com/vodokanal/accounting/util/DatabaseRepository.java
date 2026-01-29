package com.vodokanal.accounting.util;

import com.vodokanal.accounting.dto.AccountUpdateDto;
import com.vodokanal.accounting.dto.TariffDto;
import com.vodokanal.accounting.entity.AccountEntity;
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

//import javax.management.Query;
import java.util.List;
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
            throw e;

        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }

            throw e;
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
            ServiceEntity serviceEntity = session.get(ServiceEntity.class, tariffDto.service());

            if (serviceEntity == null) {
                throw new NoSuchElementException("Услуга для устанавливаемого тарифа не найдена");
            }

            TariffEntity tariffEntity = mappingUtil.mapTariffDtoToEntity(tariffDto, serviceEntity);

            session.persist(tariffEntity);

            return mappingUtil.mapTariffEntityToDto(tariffEntity);
        });
    }

    //Добавление услуг по умолчанию при запуске программы
    public void addService(List<ServiceEntity> serviceEntityList) {
        executeTransaction(session -> {
            serviceEntityList.forEach(session::persist);

            return "";
        });
    }

    public String getAccountNumberByTelegramID(long userID) {
        return executeQuery(session -> {
            String hql = "FROM AccountEntity WHERE telegramID = :userID";
            Query<AccountEntity> query = session.createQuery(hql, AccountEntity.class);
            query.setParameter("userID", userID);

            List<AccountEntity> resultList = query.getResultList();

            if (resultList.isEmpty()) {
                return "";
            }

            return resultList.getFirst().getNumber();
        });
    }

    public boolean bindTelegramID(long chatID, String accountNumber) {
        return executeTransaction(session -> {
            String hql = "FROM AccountEntity WHERE number = :accountNumber";
            Query<AccountEntity> query = session.createQuery(hql, AccountEntity.class);
            query.setParameter("accountNumber", accountNumber);

            List<AccountEntity> resultList = query.getResultList();

            if (resultList.isEmpty()) {
                return false;
            }

            AccountEntity updateAccountEntity = resultList.getFirst();
            updateAccountEntity.setTelegramID(chatID);
            session.merge(updateAccountEntity);

            return true;
        });

    }
}
