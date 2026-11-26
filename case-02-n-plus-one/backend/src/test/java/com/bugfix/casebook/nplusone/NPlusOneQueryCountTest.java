package com.bugfix.casebook.nplusone;

import com.bugfix.casebook.nplusone.after.AuthorAfter;
import com.bugfix.casebook.nplusone.after.AuthorAfterRepository;
import com.bugfix.casebook.nplusone.after.BookAfter;
import com.bugfix.casebook.nplusone.before.AuthorBefore;
import com.bugfix.casebook.nplusone.before.AuthorBeforeRepository;
import com.bugfix.casebook.nplusone.before.BookBefore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests demonstrating N+1 query problem with actual query counting
 * Uses Hibernate Statistics to count SQL queries
 */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
@Transactional
class NPlusOneQueryCountTest {
    
    @Autowired
    private AuthorBeforeRepository beforeRepo;
    
    @Autowired
    private AuthorAfterRepository afterRepo;
    
    @Autowired
    private QueryCounter queryCounter;
    
    @BeforeEach
    void setUp() {
        // Create 10 authors, each with 3 books
        for (int i = 1; i <= 10; i++) {
            AuthorBefore author = new AuthorBefore("Author " + i, "Country " + i);
            for (int j = 1; j <= 3; j++) {
                BookBefore book = new BookBefore("Book " + j + " by Author " + i, "ISBN-" + i + "-" + j);
                author.addBook(book);
            }
            beforeRepo.save(author);
        }
        
        for (int i = 1; i <= 10; i++) {
            AuthorAfter author = new AuthorAfter("Author " + i, "Country " + i);
            for (int j = 1; j <= 3; j++) {
                BookAfter book = new BookAfter("Book " + j + " by Author " + i, "ISBN-" + i + "-" + j);
                author.addBook(book);
            }
            afterRepo.save(author);
        }
        
        queryCounter.clear();
    }
    
    @Test
    void beforeVersion_CausesNPlusOneQueries() {
        System.out.println("=== BEFORE: N+1 Query Problem ===");
        
        queryCounter.clear();
        
        // Fetch all authors
        List<AuthorBefore> authors = beforeRepo.findAll();
        long afterFetchAuthors = queryCounter.getQueryCount();
        
        System.out.println("After fetching authors: " + afterFetchAuthors + " queries");
        
        // Access books - triggers lazy loading
        int totalBooks = 0;
        for (AuthorBefore author : authors) {
            totalBooks += author.getBooks().size();
        }
        
        long finalQueryCount = queryCounter.getQueryCount();
        
        System.out.println("After accessing books: " + finalQueryCount + " queries");
        System.out.println("Total books loaded: " + totalBooks);
        queryCounter.printStats();
        
        // ❌ BUG: 1 query for authors + N queries for books = 11 queries total
        assertTrue(finalQueryCount >= 11, 
                "N+1 Problem: Should execute at least 11 queries (1 for authors + 10 for books), got " + finalQueryCount);
        assertEquals(30, totalBooks);
        
        System.out.println("=========================================");
    }
    
    @Test
    void afterVersion_FetchJoin_ExecutesOneQuery() {
        System.out.println("=== AFTER: Fetch Join ===");
        
        queryCounter.clear();
        
        // Use fetch join
        List<AuthorAfter> authors = afterRepo.findAllWithBooksFetchJoin();
        
        // Access books
        int totalBooks = 0;
        for (AuthorAfter author : authors) {
            totalBooks += author.getBooks().size();
        }
        
        long queryCount = queryCounter.getQueryCount();
        
        System.out.println("Query count: " + queryCount);
        System.out.println("Total books loaded: " + totalBooks);
        queryCounter.printStats();
        
        // ✅ FIX: Only 1 query with JOIN
        assertEquals(1, queryCount, 
                "Fetch join should execute only 1 query, got " + queryCount);
        assertEquals(30, totalBooks);
        
        System.out.println("=========================================");
    }
    
    @Test
    void compareQueryCounts() {
        System.out.println("=== Query Count Comparison ===");
        
        // BEFORE: N+1 queries
        queryCounter.clear();
        List<AuthorBefore> beforeAuthors = beforeRepo.findAll();
        beforeAuthors.forEach(a -> a.getBooks().size());
        long beforeQueries = queryCounter.getQueryCount();
        
        // AFTER: 1 query
        queryCounter.clear();
        List<AuthorAfter> afterAuthors = afterRepo.findAllWithBooksFetchJoin();
        afterAuthors.forEach(a -> a.getBooks().size());
        long afterQueries = queryCounter.getQueryCount();
        
        System.out.println("BEFORE (lazy loading): " + beforeQueries + " queries");
        System.out.println("AFTER (fetch join):   " + afterQueries + " query");
        System.out.println("Reduction: " + (beforeQueries - afterQueries) + " queries saved");
        
        // Assert the improvement
        assertTrue(beforeQueries >= 11, "BEFORE should use 11+ queries");
        assertEquals(1, afterQueries, "AFTER should use exactly 1 query");
        
        System.out.println("=========================================");
    }
}
