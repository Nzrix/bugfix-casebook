package com.bugfix.casebook.nplusone.before;

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
 * Tests demonstrating the N+1 query problem
 * การทดสอบที่แสดงปัญหา N+1 query
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AuthorBeforeTest {
    
    @Autowired
    private AuthorBeforeRepository authorRepository;
    
    @Autowired
    private QueryCounter queryCounter;
    
    @BeforeEach
    void setUp() {
        // Create test data: 10 authors, each with 3 books
        for (int i = 1; i <= 10; i++) {
            AuthorBefore author = new AuthorBefore("Author " + i, "Country " + i);
            
            for (int j = 1; j <= 3; j++) {
                BookBefore book = new BookBefore(
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
    void demonstrateNPlusOneProblem() {
        System.out.println("=== BEFORE: Demonstrating N+1 Query Problem ===");
        
        // Clear and start counting queries
        queryCounter.clear();
        
        // Fetch all authors
        List<AuthorBefore> authors = authorRepository.findAll();
        long afterFindAll = queryCounter.getQueryCount();
        
        System.out.println("After findAll(): " + afterFindAll + " queries");
        assertEquals(1, afterFindAll, "findAll() should execute 1 query");
        
        // Access books for each author - this triggers lazy loading
        int totalBooks = 0;
        for (AuthorBefore author : authors) {
            totalBooks += author.getBooks().size(); // ❌ Each access triggers a query!
        }
        
        long finalQueryCount = queryCounter.getQueryCount();
        System.out.println("After accessing all books: " + finalQueryCount + " queries");
        System.out.println("Total books loaded: " + totalBooks);
        
        queryCounter.printStats();
        
        // ❌ BUG: We have 11 queries (1 for authors + 10 for books)
        assertEquals(11, finalQueryCount, 
                "N+1 Problem: 1 query for authors + 10 queries for books");
        
        System.out.println("=========================================");
    }
    
    @Test
    void showPerformanceDegradationWithMoreAuthors() {
        // Add more authors to show how the problem scales
        for (int i = 11; i <= 50; i++) {
            AuthorBefore author = new AuthorBefore("Author " + i, "Country " + i);
            author.addBook(new BookBefore("Book by Author " + i, "ISBN-" + i));
            authorRepository.save(author);
        }
        
        queryCounter.clear();
        
        List<AuthorBefore> authors = authorRepository.findAll();
        
        // Access all books
        authors.forEach(author -> author.getBooks().size());
        
        long queryCount = queryCounter.getQueryCount();
        
        System.out.println("With 50 authors: " + queryCount + " queries");
        
        // ❌ 51 queries! (1 + 50)
        assertEquals(51, queryCount, "Query count scales with number of authors");
    }
}
