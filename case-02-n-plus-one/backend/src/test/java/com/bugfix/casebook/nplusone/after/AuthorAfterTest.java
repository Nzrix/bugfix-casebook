package com.bugfix.casebook.nplusone.after;

import com.bugfix.casebook.nplusone.QueryCounter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests demonstrating solutions to N+1 problem
 * การทดสอบที่แสดงวิธีแก้ปัญหา N+1
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AuthorAfterTest {
    
    @Autowired
    private AuthorAfterRepository authorRepository;
    
    @Autowired
    private QueryCounter queryCounter;
    
    @BeforeEach
    void setUp() {
        // Create test data: 10 authors, each with 3 books
        for (int i = 1; i <= 10; i++) {
            AuthorAfter author = new AuthorAfter("Author " + i, "Country " + i);
            
            for (int j = 1; j <= 3; j++) {
                BookAfter book = new BookAfter(
                        "Book " + j + " by Author " + i,
                        "ISBN-" + i + "-" + j
                );
                author.addBook(book);
            }
            
            authorRepository.save(author);
        }
        
        // Clear query statistics after setup
        queryCounter.clear();
    }
    
    @Test
    void solution1_FetchJoin_ExecutesOneQuery() {
        System.out.println("=== AFTER: Solution 1 - Fetch Join ===");
        
        queryCounter.clear();
        
        // ✅ FIX: Use JPQL with JOIN FETCH
        List<AuthorAfter> authors = authorRepository.findAllWithBooksFetchJoin();
        
        // Access books
        int totalBooks = 0;
        for (AuthorAfter author : authors) {
            totalBooks += author.getBooks().size();
        }
        
        long queryCount = queryCounter.getQueryCount();
        
        System.out.println("Query count: " + queryCount);
        System.out.println("Total books loaded: " + totalBooks);
        queryCounter.printStats();
        
        // ✅ Only 1 query!
        assertEquals(1, queryCount, "Fetch join should execute only 1 query");
        assertEquals(30, totalBooks, "Should load all 30 books");
        
        System.out.println("=========================================");
    }
    
    @Test
    void solution2_EntityGraph_ExecutesOneQuery() {
        System.out.println("=== AFTER: Solution 2 - Entity Graph ===");
        
        queryCounter.clear();
        
        // ✅ FIX: Use @EntityGraph annotation
        List<AuthorAfter> authors = authorRepository.findAllWithBooksEntityGraph();
        
        // Access books
        int totalBooks = 0;
        for (AuthorAfter author : authors) {
            totalBooks += author.getBooks().size();
        }
        
        long queryCount = queryCounter.getQueryCount();
        
        System.out.println("Query count: " + queryCount);
        System.out.println("Total books loaded: " + totalBooks);
        queryCounter.printStats();
        
        // ✅ Only 1 query!
        assertEquals(1, queryCount, "Entity graph should execute only 1 query");
        assertEquals(30, totalBooks);
        
        System.out.println("=========================================");
    }
    
    @Test
    void solution3_EntityGraphOverride_ExecutesOneQuery() {
        System.out.println("=== AFTER: Solution 3 - Override findAll ===");
        
        queryCounter.clear();
        
        // ✅ FIX: Override default findAll() with Entity Graph
        List<AuthorAfter> authors = authorRepository.findAll();
        
        // Access books
        int totalBooks = 0;
        for (AuthorAfter author : authors) {
            totalBooks += author.getBooks().size();
        }
        
        long queryCount = queryCounter.getQueryCount();
        
        System.out.println("Query count: " + queryCount);
        System.out.println("Total books loaded: " + totalBooks);
        queryCounter.printStats();
        
        // ✅ Only 1 query!
        assertEquals(1, queryCount, "Overridden findAll should execute only 1 query");
        assertEquals(30, totalBooks);
        
        System.out.println("=========================================");
    }
    
    @Test
    void performanceWithManyAuthors() {
        // Add more authors
        for (int i = 11; i <= 100; i++) {
            AuthorAfter author = new AuthorAfter("Author " + i, "Country " + i);
            author.addBook(new BookAfter("Book by Author " + i, "ISBN-" + i));
            authorRepository.save(author);
        }
        
        queryCounter.clear();
        
        // ✅ Even with 100 authors, still only 1 query
        List<AuthorAfter> authors = authorRepository.findAllWithBooksFetchJoin();
        authors.forEach(author -> author.getBooks().size());
        
        long queryCount = queryCounter.getQueryCount();
        
        System.out.println("With 100 authors: " + queryCount + " queries");
        
        // ✅ Still just 1 query!
        assertEquals(1, queryCount, "Performance doesn't degrade with more authors");
    }
    
    @Test
    void compareBeforeAndAfter() {
        System.out.println("=== Performance Comparison ===");
        
        // Simulate BEFORE (naive approach)
        queryCounter.clear();
        // If we had used regular findAll + accessing books:
        // Expected: 11 queries (1 + 10)
        
        // AFTER with fetch join
        queryCounter.clear();
        List<AuthorAfter> authors = authorRepository.findAllWithBooksFetchJoin();
        authors.forEach(author -> author.getBooks().size());
        
        long optimizedQueryCount = queryCounter.getQueryCount();
        
        System.out.println("BEFORE approach: ~11 queries");
        System.out.println("AFTER approach: " + optimizedQueryCount + " query");
        System.out.println("Improvement: ~91% reduction in queries");
        
        assertEquals(1, optimizedQueryCount);
    }
}
