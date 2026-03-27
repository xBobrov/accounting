package com.vodokanal.accounting.util;

import com.vodokanal.accounting.dto.*;
import com.vodokanal.accounting.entity.*;

import com.vodokanal.accounting.exception.DataAlreadyExistsException;
import com.vodokanal.accounting.exception.DataNotFoundException;
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

    private <T> T executeTransaction(Function<Session, T> applicable) {
        Transaction transaction = null;

        try (Session session = sessionFactory.openSession()) {
            session.setJdbcBatchSize(10);
            transaction = session.getTransaction();
            transaction.begin();

            var entityList = applicable.apply(session);

            transaction.commit();

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

    private <T> T executeQuery(Function<Session, T> applicable) {
        try (Session session = sessionFactory.openSession()) {
            return applicable.apply(session);
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

    public TariffDto addTariff(TariffDto tariffDto) {
        return executeTransaction(session -> {

            ServiceEntity serviceEntity = getServiceEntity(session, tariffDto.service());

            if (serviceEntity == null) {
                throw new DataNotFoundException("Услуга для устанавливаемого тарифа не найдена");
            }

            TariffEntity tariffEntity;
            List<TariffEntity> tariffEntityList = getTariffEntity(session, tariffDto.implementationDate(), tariffDto.service());

            if (!tariffEntityList.isEmpty()) {
                tariffEntity = tariffEntityList.getFirst();
                tariffEntity.setRate(new BigDecimal(tariffDto.rate()));
                updateTariffEntity(session, tariffEntity);

                log.info("В базе данных обновлена запись: {}", tariffEntity);
            } else {
                tariffEntity = mappingUtil.mapTariffDtoToEntity(tariffDto);
                tariffEntity.setService(serviceEntity);
                session.persist(tariffEntity);

                log.info("В базу данных добавлена запись: {}", tariffEntity);
            }

            return mappingUtil.mapTariffEntityToDto(tariffEntity);
        });
    }

    public MeterDto addMeter(MeterDto meterDto, LocalDate validThru) {
        return executeTransaction(session -> {
            ServiceEntity serviceEntity = getServiceEntity(session, meterDto.service());

            if (serviceEntity == null) {
                throw new DataNotFoundException("Услуга для добовляемого ИПУ не найдена");
            }

            List<AccountEntity> accountEntityList = getAccountEntityList(session, meterDto.accountNumber());

            if (accountEntityList.isEmpty()) {
                throw new DataNotFoundException("Лицевой счет для добовляемого ИПУ не найден");
            }

            AccountEntity accountEntity = accountEntityList.getFirst();

            List<MeterEntity> meterEntityList = getMeterEntityList(
                    session,
                    accountEntity,
                    meterDto.serialNumber());

            if (!meterEntityList.isEmpty()) {
                throw new DataAlreadyExistsException("ИПУ с таким номером уже привязан к лицевому счету");
            }

            MeterEntity meterEntity = mappingUtil.mapMeterDtoToEntity(meterDto);
            meterEntity.setAccount(accountEntity);
            meterEntity.setService(serviceEntity);
            meterEntity.setValidThru(validThru);

            session.persist(meterEntity);
            log.info("В базу данных добавлена запись: {}", meterEntity);

            return mappingUtil.mapMeterEntityToDto(meterEntity);
        });
    }

    //Добавление услуг по умолчанию при запуске программы, убрать на релизе
    public void addService(List<ServiceEntity> serviceEntityList) {
        executeTransaction(session -> {
            serviceEntityList.forEach(session::persist);

            return "";
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

    private void updateTariffEntity(Session session, TariffEntity tariffEntity) {
        session.merge(tariffEntity);
    }

    private List<TariffEntity> getTariffEntity(Session session, String date, long service) {
        String hql = "FROM TariffEntity WHERE implementationDate = :date AND service = :service";
        Query<TariffEntity> query = session.createQuery(hql, TariffEntity.class);
        query.setParameter("date", LocalDate.parse(date));
        query.setParameter("service", getServiceEntity(session, service));

        return query.getResultList();
    }

    public AccountEntity getAccountEntity(Session session, long id) {
        return session.get(AccountEntity.class, id);
    }

    private List<AccountEntity> getAccountEntityList(Session session, long chatID) {
        String hql = "FROM AccountEntity WHERE telegramID = :chatID";
        Query<AccountEntity> query = session.createQuery(hql, AccountEntity.class);
        query.setParameter("chatID", chatID);

        return query.getResultList();
    }

    private List<AccountEntity> getAccountEntityList(Session session, String accountNumber) {
        String hql = "FROM AccountEntity WHERE number = :accountNumber";
        Query<AccountEntity> query = session.createQuery(hql, AccountEntity.class);
        query.setParameter("accountNumber", accountNumber);

        return query.getResultList();
    }

    public String getAccountData(long chatID) {
        return executeTransaction(session -> {
            List<AccountEntity> resultList = getAccountEntityList(session, chatID);

            if (resultList.isEmpty()) {
                return "";
            }

            return mappingUtil.mapObjectToJson(resultList.getFirst());
        });
    }

    private List<MeterEntity> getMeterEntityList(Session session, AccountEntity accountEntity, String meterNumber) {
        String hql = "FROM MeterEntity WHERE serialNumber = :meterNumber AND account = :accountEntity";
        Query<MeterEntity> query = session.createQuery(hql, MeterEntity.class);
        query.setParameter("meterNumber", meterNumber);
        query.setParameter("accountEntity", accountEntity);

        return query.getResultList();
    }

    private ServiceEntity getServiceEntity(Session session, long id) {
        return session.get(ServiceEntity.class, id);
    }

    public String bindTelegramID(long chatID, String accountNumber) {
        return executeTransaction(session -> {
            List<AccountEntity> resultList = getAccountEntityList(session, accountNumber);

            if (resultList.isEmpty()) {
                log.info("Лицевой счет с номером {} не найден в базе данных", accountNumber);
                return "";
            }

            AccountEntity accountEntity = resultList.getFirst();
            accountEntity.setTelegramID(chatID);
            session.merge(accountEntity);

            log.info("К лицевому счету {} привязан телеграм id {}", accountNumber, chatID);

            return mappingUtil.mapObjectToJson(accountEntity);
        });
    }

    public MeterUpdateDto updateMeter(MeterUpdateDto meterUpdateDto, LocalDate validThru) {
        return executeTransaction(session -> {
            List<AccountEntity> accountEntityList = getAccountEntityList(session, meterUpdateDto.accountNumber());

            if (accountEntityList.isEmpty()) {
                throw new DataNotFoundException("Лицевой счет для обновляемого ИПУ не найден");
            }

            AccountEntity accountEntity = accountEntityList.getFirst();

            List<MeterEntity> meterEntityList = getMeterEntityList(
                    session,
                    accountEntity,
                    meterUpdateDto.serialNumber());

            if (meterEntityList.isEmpty()) {
                throw new DataNotFoundException("ИПУ с номером %s не привязан к лицевому счету %s".formatted(
                        meterUpdateDto.serialNumber(),
                        accountEntity.getNumber()
                ));
            }

            MeterEntity meterEntity = meterEntityList.getFirst();
            meterEntity.setVerificationDate(LocalDate.parse(meterUpdateDto.verificationDate()));
            meterEntity.setValidThru(validThru);
            session.merge(meterEntity);

            log.info("В базе данных обновлена запись: {}", meterEntity);

            return mappingUtil.mapMeterEntityToUpdateDto(meterEntity);
        });
    }

    public String getAllMetersData(long chatID) {
        return executeQuery(session -> {
            List<MeterDataDto> meterDataDtoList = session.createNativeQuery(
                            """
                                    SELECT\s
                                        mtr.id AS id,
                                        mtr.serial_number AS number,
                                        srv.name AS service,
                                        mtr.valid_thru AS valid,
                                        COALESCE(rdg.value, mtr.initial_value) AS last_reading
                                    FROM meter mtr
                                    JOIN account acc ON mtr.account_id = acc.id
                                    JOIN service srv ON mtr.service_id = srv.id
                                    LEFT JOIN LATERAL (
                                        SELECT value\s
                                        FROM reading\s
                                        WHERE meter_id = mtr.id\s
                                        ORDER BY date DESC\s
                                        LIMIT 1
                                    ) rdg ON TRUE
                                    WHERE acc.telegram_id = :telegram_id;""",
                            MeterDataDto.class)
                    .setParameter("telegram_id", chatID)
                    .getResultList();

            return mappingUtil.mapObjectToJson(meterDataDtoList);
        });
    }

    public String changeEmail(long chatID, String email) {
        return executeTransaction(session -> {
            AccountEntity accountEntity = getAccountEntityList(session, chatID).getFirst();

            if (email.equals("0")) {
                accountEntity.setEmail("");
            } else {
                accountEntity.setEmail(email);
            }

            session.merge(accountEntity);
            log.info("У лицевого счета {} изменен email: {}", accountEntity.getNumber(), accountEntity.getEmail());

            return accountEntity.getEmail();
        });

    }

    public String getMetersData(long chatID, String meterNumber) {
        return executeQuery(session -> {
            List<MeterDataDto> meterDataDtoList = session.createNativeQuery(
                            """
                                    SELECT\s
                                        mtr.id AS id,
                                        mtr.serial_number AS number,
                                        srv.name AS service,
                                        mtr.valid_thru AS valid,
                                        COALESCE(rdg.value, mtr.initial_value) AS last_reading
                                    FROM meter mtr
                                    JOIN account acc ON mtr.account_id = acc.id
                                    JOIN service srv ON mtr.service_id = srv.id
                                    LEFT JOIN LATERAL (
                                        SELECT value\s
                                        FROM reading\s
                                        WHERE meter_id = mtr.id\s
                                        ORDER BY date DESC\s
                                        LIMIT 1
                                    ) rdg ON TRUE
                                    WHERE acc.telegram_id = :telegram_id
                                    AND mtr.serial_number = :meter_number;""",
                            MeterDataDto.class)
                    .setParameter("telegram_id", chatID)
                    .setParameter("meter_number", meterNumber)
                    .getResultList();

            if (meterDataDtoList.isEmpty()) {
                return "";
            } else {
                return mappingUtil.mapObjectToJson(meterDataDtoList.getFirst());
            }
        });
    }

    public String saveReading(
            long chatID,
            String meterNumber,
            BigDecimal currentReading,
            BigDecimal consumption,
            LocalDate date) {
        return executeTransaction(session -> {

            int rowsInserted = session.createNativeMutationQuery("""
                            INSERT INTO reading (consumption, date, value, meter_id)
                            SELECT :consumption, :date, :current_reading, mtr.id
                            FROM meter mtr
                            JOIN account acc
                            ON mtr.account_id = acc.id
                            WHERE mtr.serial_number = :serial_number
                            AND acc.telegram_id = :telegram_id
                            ON CONFLICT (date, meter_id)
                            DO UPDATE SET\s
                                consumption = EXCLUDED.consumption,
                            	value = EXCLUDED.value
                            """)
                    .setParameter("serial_number", meterNumber)
                    .setParameter("telegram_id", chatID)
                    .setParameter("consumption", consumption)
                    .setParameter("current_reading", currentReading)
                    .setParameter("date", date).executeUpdate();

            return String.valueOf(rowsInserted);
        });
    }

    public String getEmail(long chatID) {
        return executeQuery(session -> {
            AccountEntity accountEntity = getAccountEntityList(session, chatID).getFirst();

            return accountEntity.getEmail();
        });
    }
}
