package com.bugfix.casebook.nplusone;

import jakarta.persistence.EntityManager;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.springframework.stereotype.Component;

/**
 * Utility to count queries executed during a test
 * เครื่องมือสำหรับนับจำนวน query ที่ทำงานระหว่างการทดสอบ
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
    
    public long getQueryCount() {
        return getStatistics().getQueryExecutionCount();
    }
    
    public void printStats() {
        Statistics stats = getStatistics();
        System.out.println("=== Query Statistics ===");
        System.out.println("Query count: " + stats.getQueryExecutionCount());
        System.out.println("Entity loads: " + stats.getEntityLoadCount());
        System.out.println("Collection loads: " + stats.getCollectionLoadCount());
        System.out.println("========================");
    }
}
