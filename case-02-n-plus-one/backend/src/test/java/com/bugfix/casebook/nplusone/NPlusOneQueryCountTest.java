package com.bugfix.casebook.nplusone;

import com.bugfix.casebook.nplusone.after.AuthorAfter;
import com.bugfix.casebook.nplusone.after.AuthorAfterRepository;
import com.bugfix.casebook.nplusone.after.BookAfter;
import com.bugfix.casebook.nplusone.before.AuthorBefore;
import com.bugfix.casebook.nplusone.before.AuthorBeforeRepository;
import com.bugfix.casebook.nplusone.before.BookBefore;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * N+1 proof with Hibernate Statistics on H2.
 * BEFORE: 1 (authors) + N (lazy books) prepared statements.
 * AFTER fetch-join: exactly 1 prepared statement.
 */
@SpringBootTest
@Transactional
class NPlusOneQueryCountTest {

    private static final int AUTHOR_COUNT = 10;

    @Autowired
    private AuthorBeforeRepository beforeRepo;

    @Autowired
    private AuthorAfterRepository afterRepo;

    @Autowired
    private QueryCounter queryCounter;

    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    void setUp() {
        beforeRepo.deleteAll();
        afterRepo.deleteAll();

        for (int i = 1; i <= AUTHOR_COUNT; i++) {
            AuthorBefore author = new AuthorBefore("Author " + i, "Country " + i);
            for (int j = 1; j <= 3; j++) {
                author.addBook(new BookBefore("Book " + j + " by Author " + i, "ISBN-" + i + "-" + j));
            }
            beforeRepo.save(author);
        }

        for (int i = 1; i <= AUTHOR_COUNT; i++) {
            AuthorAfter author = new AuthorAfter("Author " + i, "Country " + i);
            for (int j = 1; j <= 3; j++) {
                author.addBook(new BookAfter("Book " + j + " by Author " + i, "ISBN-" + i + "-" + j));
            }
            afterRepo.save(author);
        }

        entityManager.flush();
        entityManager.clear();
        queryCounter.clear();
    }

    @Test
    void beforeVersion_CausesNPlusOneQueries() {
        System.out.println("=== BEFORE: N+1 Query Problem ===");

        queryCounter.clear();

        List<AuthorBefore> authors = beforeRepo.findAll();
        long afterFetchAuthors = queryCounter.getSqlCount();

        int totalBooks = 0;
        for (AuthorBefore author : authors) {
            totalBooks += author.getBooks().size();
        }

        long finalSqlCount = queryCounter.getSqlCount();

        System.out.println("After fetching authors: " + afterFetchAuthors + " SQL");
        System.out.println("After accessing books: " + finalSqlCount + " SQL");
        System.out.println("Total books loaded: " + totalBooks);
        queryCounter.printStats();

        // 1 for authors + N for each author's books collection
        assertEquals(AUTHOR_COUNT, authors.size());
        assertEquals(1 + AUTHOR_COUNT, finalSqlCount,
                "N+1: expected 1+" + AUTHOR_COUNT + " SQL statements, got " + finalSqlCount);
        assertEquals(30, totalBooks);

        System.out.println("=========================================");
    }

    @Test
    void afterVersion_FetchJoin_ExecutesOneQuery() {
        System.out.println("=== AFTER: Fetch Join ===");

        queryCounter.clear();

        List<AuthorAfter> authors = afterRepo.findAllWithBooksFetchJoin();

        int totalBooks = 0;
        for (AuthorAfter author : authors) {
            totalBooks += author.getBooks().size();
        }

        long sqlCount = queryCounter.getSqlCount();

        System.out.println("SQL count: " + sqlCount);
        System.out.println("Total books loaded: " + totalBooks);
        queryCounter.printStats();

        assertEquals(1, sqlCount, "Fetch join should execute exactly 1 SQL statement, got " + sqlCount);
        assertEquals(AUTHOR_COUNT, authors.size());
        assertEquals(30, totalBooks);

        System.out.println("=========================================");
    }

    @Test
    void compareQueryCounts() {
        System.out.println("=== Query Count Comparison ===");

        queryCounter.clear();
        List<AuthorBefore> beforeAuthors = beforeRepo.findAll();
        beforeAuthors.forEach(a -> a.getBooks().size());
        long beforeSql = queryCounter.getSqlCount();

        queryCounter.clear();
        List<AuthorAfter> afterAuthors = afterRepo.findAllWithBooksFetchJoin();
        afterAuthors.forEach(a -> a.getBooks().size());
        long afterSql = queryCounter.getSqlCount();

        System.out.println("BEFORE (lazy loading): " + beforeSql + " SQL");
        System.out.println("AFTER (fetch join):   " + afterSql + " SQL");

        assertEquals(1 + AUTHOR_COUNT, beforeSql, "BEFORE should be 1+N");
        assertEquals(1, afterSql, "AFTER should be exactly 1");

        System.out.println("=========================================");
    }
}
