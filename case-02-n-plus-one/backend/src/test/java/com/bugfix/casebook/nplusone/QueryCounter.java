package com.bugfix.casebook.nplusone;

import jakarta.persistence.EntityManager;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.springframework.stereotype.Component;

/**
 * Counts SQL statements via Hibernate Statistics (generate_statistics=true).
 * Uses prepareStatementCount so lazy collection loads are included
 * (getQueryExecutionCount only tracks HQL/JPQL, not collection fetches).
 */
@Component
public class QueryCounter {

    private final EntityManager entityManager;

    public QueryCounter(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public Statistics getStatistics() {
        Session session = entityManager.unwrap(Session.class);
        SessionFactory sessionFactory = session.getSessionFactory();
        return sessionFactory.getStatistics();
    }

    public void clear() {
        getStatistics().clear();
    }

    /** Total JDBC prepared statements — closest to "SQL statement count". */
    public long getSqlCount() {
        return getStatistics().getPrepareStatementCount();
    }

    public void printStats() {
        Statistics stats = getStatistics();
        System.out.println("=== Query Statistics ===");
        System.out.println("Prepared statements: " + stats.getPrepareStatementCount());
        System.out.println("HQL/JPQL queries: " + stats.getQueryExecutionCount());
        System.out.println("Collection fetches: " + stats.getCollectionFetchCount());
        System.out.println("Entity loads: " + stats.getEntityLoadCount());
        System.out.println("========================");
    }
}
