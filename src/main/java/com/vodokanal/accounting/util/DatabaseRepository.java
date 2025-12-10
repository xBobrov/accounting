package com.vodokanal.accounting.util;

import com.vodokanal.accounting.dto.AccountDto;
import com.vodokanal.accounting.dto.AccountUpdateDto;
import com.vodokanal.accounting.entity.AccountEntity;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Function;

@Component
public class DatabaseRepository {
    private final SessionFactory sessionFactory;
    private final MappingUtil mappingUtil;

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

    public AccountDto updateAccount(long id, AccountUpdateDto accountUpdateDto) {
        return executeTransaction(session -> {
            AccountEntity accountEntity = session.get(AccountEntity.class, id);

            if (accountEntity == null) {
                throw new NoSuchElementException("Лицевой счет не найден");
            }

            AccountEntity updatedAccountEntity = mappingUtil.mapAccountUpdate(accountUpdateDto,
                    accountEntity);

            session.merge(updatedAccountEntity);

            return mappingUtil.mapAccountEntityDto(updatedAccountEntity);
        });
    }
}
