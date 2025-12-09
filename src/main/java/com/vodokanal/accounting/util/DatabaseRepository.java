package com.vodokanal.accounting.util;

import com.vodokanal.accounting.entity.AccountEntity;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Component
public class DatabaseRepository {
    private final SessionFactory sessionFactory;

    public DatabaseRepository(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    private <T> T executeTransaction(Function<Session, T> executable) {
        Transaction transaction = null;

        try (Session session = sessionFactory.openSession()) {
            transaction = session.getTransaction();
            transaction.begin();

            var accountEntity = executable.apply(session);
            session.getTransaction().commit();

            return accountEntity;
        } catch (ConstraintViolationException cve) {
            throw cve;

        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }

            throw e;
        }
    }

    public AccountEntity addAccount(AccountEntity accountEntity) {
        return executeTransaction(session -> {
            session.persist(accountEntity);

            return accountEntity;
        });
    }
}
