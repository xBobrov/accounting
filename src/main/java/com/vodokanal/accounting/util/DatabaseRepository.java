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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

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
            transaction = session.getTransaction();
            transaction.begin();

            var entityList = executable.apply(session);
            transaction.commit();

            log.info("В базу данных добавленна/обновлена запись: {}", executable); // переделать!

            return entityList;
        } catch (ConstraintViolationException cve) {
            throw cve;

        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }

            throw e;
        }
    }

    public List<AccountEntity> addAccount(List<AccountEntity> accountEntityList) {
        return executeTransaction(session -> {
            accountEntityList.forEach(session::persist);

            return accountEntityList;
        });
    }

    public AccountUpdateDto updateAccount(long id, AccountUpdateDto accountUpdateDto) {
        return executeTransaction(session -> {
            AccountEntity accountEntity = session.get(AccountEntity.class, id);

            if (accountEntity == null) {
                throw new NoSuchElementException("Лицевой счет не найден");
            }

            AccountEntity updatedAccountEntity = mappingUtil.mapAccountUpdate(accountUpdateDto,
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

            TariffEntity tariffEntity = mappingUtil.mapTariffDtoEntity(tariffDto, serviceEntity);

            session.persist(tariffEntity);

            return mappingUtil.mapTariffEntityDto(tariffEntity);
        });
    }

    //Добавление услуг по умолчанию при запуске программы
    public void addService(List<ServiceEntity> serviceEntityList) {
        executeTransaction(session -> {
            serviceEntityList.forEach(session::persist);

            return "";
        });
    }
}
