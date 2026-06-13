package com.vodokanal.accounting.util;

import com.vodokanal.accounting.dto.*;
import com.vodokanal.accounting.entity.*;

import com.vodokanal.accounting.exception.DataAlreadyExistsException;
import com.vodokanal.accounting.exception.DataNotFoundException;
import jakarta.annotation.PostConstruct;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Function;

/**
 * Low-level repository service providing direct interaction with the database via Hibernate {@link SessionFactory}.
 * <p>
 * This component implements the "Execute-Around" pattern to manage sessions and transactions manually.
 * It is optimized for high-performance billing operations, utilizing:
 * <ul>
 *     <li>Native SQL scripts loaded from the classpath.</li>
 *     <li>Batch processing (Flush & Clear) for bulk data insertion.</li>
 *     <li>Upsert logic (ON CONFLICT) to maintain data idempotency.</li>
 *     <li>Lateral joins for efficient retrieval of the latest meter readings.</li>
 * </ul>
 * </p>
 */
@Component
public class DatabaseRepository {
    private final SessionFactory sessionFactory;
    private final MappingUtil mappingUtil;
    private static final Logger log = LoggerFactory.getLogger(DatabaseRepository.class);

    @Value("classpath:sql/calculation.sql")
    private Resource calculationSqlFile;
    private String calculationSqlText;

    @Value("classpath:sql/bill_calculation.sql")
    private Resource billCalculationSqlFile;
    private String billCalculationSqlText;

    @Value("classpath:sql/bill_meters.sql")
    private Resource billMeterSqlFile;
    private String billMeterSqlText;

    @PostConstruct
    public void init() throws Exception {
        this.calculationSqlText = StreamUtils.copyToString(calculationSqlFile.getInputStream(), StandardCharsets.UTF_8);
        this.billCalculationSqlText = StreamUtils.copyToString(
                billCalculationSqlFile.getInputStream(),
                StandardCharsets.UTF_8);
        this.billMeterSqlText = StreamUtils.copyToString(
                billMeterSqlFile.getInputStream(),
                StandardCharsets.UTF_8);
    }

    public DatabaseRepository(SessionFactory sessionFactory, MappingUtil mappingUtil) {
        this.sessionFactory = sessionFactory;
        this.mappingUtil = mappingUtil;
    }

    /**
     * A core "Execute-Around" template method for managing Hibernate sessions and transactions.
     * <p>
     * This method centralizes session lifecycle management and error handling.
     * It supports two modes of operation:
     * <ul>
     *     <li><b>Read-Only Mode:</b> Optimizes performance by enabling {@code DefaultReadOnly}
     *     on the session, which bypasses Hibernate's dirty checking mechanism.</li>
     *     <li><b>Transactional Mode:</b> Ensures atomicity for data-modifying operations
     *     by manually managing transaction boundaries (begin, commit, and rollback).</li>
     * </ul>
     * </p>
     *
     * @param <T>      the type of the execution result.
     * @param readOnly {@code true} to enable read-only optimizations,
     *                 {@code false} to execute within a managed transaction.
     * @param action   a {@link Function} containing the logic to be executed within the session context.
     * @return the result produced by the action.
     * @throws RuntimeException if a database error occurs, with automatic transaction rollback.
     */
    private <T> T execute(boolean readOnly, Function<Session, T> action) {
        Transaction transaction = null;
        try (Session session = sessionFactory.openSession()) {
            if (readOnly) {
                session.setDefaultReadOnly(true);
                return action.apply(session);
            } else {
                transaction = session.beginTransaction();
                T result = action.apply(session);
                transaction.commit();
                return result;
            }
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            log.error("Database operation failed: {}", e.getMessage());
            throw new RuntimeException("Database error during operation", e);
        }
    }

    /**
     * Executes a database operation within a managed transaction.
     * <p>
     * Use this method for operations that modify data (INSERT, UPDATE, DELETE).
     * It ensures that changes are committed atomically or rolled back on failure.
     * </p>
     *
     * @param action a {@link Function} containing the transactional logic.
     * @return the result of the transaction.
     */
    private <T> T executeTransaction(Function<Session, T> action) {
        return execute(false, action);
    }

    /**
     * Executes a read-only database query with performance optimizations.
     * <p>
     * Use this method for data retrieval (SELECT) to benefit from
     * disabled dirty checking and reduced overhead.
     * </p>
     *
     * @param action a {@link Function} containing the query logic.
     * @return the result of the query.
     */
    private <T> T executeQuery(Function<Session, T> action) {
        return execute(true, action);
    }

    public AccountEntity addAccount(AccountEntity entity) {
        return executeTransaction(session -> {
            session.persist(entity);

            return entity;
        });
    }

    /**
     * Performs a batch insertion of customer accounts.
     * <p>
     * Optimizes memory usage by periodically flushing the session and clearing
     * the first-level cache according to the configured JDBC batch size.
     * </p>
     *
     * @param accountEntityList list of accounts to be persisted.
     * @return the list of persisted entities.
     */
    public List<AccountEntity> addAccountList(List<AccountEntity> accountEntityList) {
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

            return mappingUtil.mapObjectToJson(resultList.getFirst());
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

    /**
     * Updates or removes the email address linked to a Telegram account.
     *
     * @param chatID the unique Telegram chat identifier.
     * @param email  the new email address to be persisted, or "0" to clear the existing email.
     * @return the newly updated email address as stored in the database.
     */
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

    /**
     * Retrieves current data for a specific meter linked to a Telegram user.
     * <p>
     * Uses a native PostgreSQL query with a {@code LATERAL JOIN} to efficiently
     * fetch the single most recent reading for the specified meter.
     * The result is returned as a JSON string for seamless bot integration.
     * </p>
     *
     * @param chatID      the unique Telegram chat identifier.
     * @param meterNumber the serial number of the meter to retrieve.
     * @return a JSON representation of {@link MeterDataDto}, or an empty string if not found.
     */
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

    /**
     * Persists a new meter reading using a high-performance native SQL UPSERT query.
     * <p>
     * If a reading for the given date and meter already exists, it updates the consumption
     * and value fields instead of creating a duplicate.
     * </p>
     *
     * @param chatID         Telegram ID for account identification.
     * @param meterNumber    Serial number of the meter.
     * @param currentReading New reading value.
     * @param consumption    Calculated consumption since the last reading.
     * @param date           Billing period date.
     * @return String representation of the number of affected rows.
     */
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

    /**
     * Retrieves the email address associated with a specific Telegram chat.
     *
     * @param chatID the unique Telegram chat identifier.
     * @return the linked email address as a {@link String}.
     */
    public String getEmail(long chatID) {
        return executeQuery(session -> {
            AccountEntity accountEntity = getAccountEntityList(session, chatID).getFirst();

            return accountEntity.getEmail();
        });
    }

    /**
     * Triggers a bulk financial calculation for the entire district.
     * <p>
     * Executes an external SQL script that processes all active accounts and
     * generates charges based on current rates and consumption data.
     * </p>
     *
     * @param readingDate the reference date for the billing period.
     */
    public void fulfillCalculation(LocalDate readingDate) {
        executeTransaction(session -> {
            int rowsInserted = session.createNativeMutationQuery(calculationSqlText)
                    .setParameter("date", readingDate)
                    .executeUpdate();

            return "";
        });
    }

    /**
     * Retrieves billing calculation data for a specific period using native SQL.
     * <p>
     * Uses a {@code TupleTransformer} to map complex multi-column result sets directly into
     * {@link BillCalculationDTO} records.
     * </p>
     *
     * @param accountNumber target account number.
     * @param billPeriod    the target month for the bill.
     * @param readingDate   the reference date for historical readings.
     * @return a list of calculation results.
     */
    public List<BillCalculationDTO> getBillCalculation(String accountNumber, LocalDate billPeriod, LocalDate readingDate) {
        return executeTransaction(session -> session.createNativeQuery(
                        billCalculationSqlText,
                        Object[].class)
                .setParameter("billPeriod", billPeriod)
                .setParameter("readingDate", readingDate)
                .setParameter("accountNumber", accountNumber)
                .setTupleTransformer((tuple, aliases) -> new BillCalculationDTO(
                        String.valueOf(tuple[0]),
                        (Boolean) tuple[1],
                        (String) tuple[2],
                        (String) tuple[3],
                        (String) tuple[4],
                        (Integer) tuple[5],
                        String.valueOf(tuple[6]),
                        (BigDecimal) tuple[7],
                        String.valueOf(tuple[8]),
                        (BigDecimal) tuple[9],
                        (Long) tuple[10]
                ))
                .getResultList());
    }

    /**
     * Retrieves a list of meter data specifically formatted for billing documents.
     * <p>
     * Executes a native SQL query to fetch meter serials and their associated
     * readings for a given period. Results are transformed into {@link BillMeterDto}
     * using a custom {@code TupleTransformer}.
     * </p>
     *
     * @param accountNumber the utility account number.
     * @param readingDate   the target date for which readings are retrieved.
     * @return a list of {@link BillMeterDto} objects.
     */
    public List<BillMeterDto> getBillMeter(String accountNumber, LocalDate readingDate) {
        return executeTransaction(session -> session.createNativeQuery(
                        billMeterSqlText,
                        Object[].class)
                .setParameter("readingDate", readingDate)
                .setParameter("accountNumber", accountNumber)
                .setTupleTransformer((tuple, aliases) -> new BillMeterDto(
                        (String) tuple[0],
                        (String) tuple[1],
                        String.valueOf(tuple[2]),
                        (BigDecimal) tuple[3],
                        (Date) tuple[4],
                        (BigDecimal) tuple[5]
                ))
                .getResultList());
    }
}

