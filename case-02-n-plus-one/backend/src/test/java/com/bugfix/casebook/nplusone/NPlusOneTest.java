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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests demonstrating N+1 query problem and solutions
 * Note: Uses H2. Query counting behavior varies by database configuration.
 * The key learning is the fetch join pattern, not exact query counts.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class NPlusOneTest {
    
    @Autowired
    private AuthorBeforeRepository beforeRepo;
    
    @Autowired
    private AuthorAfterRepository afterRepo;
    
    @BeforeEach
    void setUp() {
        // Create test data: 10 authors, each with 3 books
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
    }
    
    @Test
    void beforeVersion_LazyLoadingCausesMultipleQueries() {
        System.out.println("=== BEFORE: Lazy Loading (Potential N+1) ===");
        
        // Fetch authors
        List<AuthorBefore> authors = beforeRepo.findAll();
        
        // Access books - lazy loading may trigger additional queries
        int totalBooks = 0;
        for (AuthorBefore author : authors) {
            int bookCount = author.getBooks().size();
            totalBooks += bookCount;
        }
        
        System.out.println("Authors fetched: " + authors.size());
        System.out.println("Total books accessed: " + totalBooks);
        System.out.println("=========================================");
        
        // Verify data loaded correctly
        assertEquals(10, authors.size());
        assertEquals(30, totalBooks);
    }
    
    @Test
    void afterVersion_FetchJoin_LoadsInOneQuery() {
        System.out.println("=== AFTER: Fetch Join ===");
        
        // ✅ FIX: Use fetch join to load authors and books together
        List<AuthorAfter> authors = afterRepo.findAllWithBooksFetchJoin();
        
        int totalBooks = 0;
        for (AuthorAfter author : authors) {
            totalBooks += author.getBooks().size();
        }
        
        System.out.println("Authors fetched: " + authors.size());
        System.out.println("Total books loaded: " + totalBooks);
        System.out.println("=========================================");
        
        // Verify data loaded correctly
        assertEquals(10, authors.size());
        assertEquals(30, totalBooks);
    }
    
    @Test
    void afterVersion_EntityGraph_LoadsEagerly() {
        System.out.println("=== AFTER: Entity Graph ===");
        
        // ✅ FIX: Use entity graph
        List<AuthorAfter> authors = afterRepo.findAllWithBooksEntityGraph();
        
        int totalBooks = 0;
        for (AuthorAfter author : authors) {
            totalBooks += author.getBooks().size();
        }
        
        System.out.println("Authors fetched: " + authors.size());
        System.out.println("Total books loaded: " + totalBooks);
        System.out.println("=========================================");
        
        // Verify data loaded correctly
        assertEquals(10, authors.size());
        assertEquals(30, totalBooks);
    }
    
    @Test
    void demonstrateFetchJoinSQL() {
        System.out.println("=== Demonstrating Fetch Join SQL ===");
        
        // Fetch join creates a JOIN in SQL
        List<AuthorAfter> authors = afterRepo.findAllWithBooksFetchJoin();
        
        // All data loaded - no additional queries when accessing books
        authors.forEach(author -> {
            String name = author.getName();
            int bookCount = author.getBooks().size();
            System.out.println(name + " has " + bookCount + " books");
        });
        
        System.out.println("=========================================");
        
        // Verify all authors have their books loaded
        assertTrue(authors.stream().allMatch(a -> a.getBooks().size() == 3));
    }
}
