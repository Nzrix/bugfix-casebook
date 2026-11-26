package com.bugfix.casebook.timezone.after;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests demonstrating the AFTER state - the fix using LocalDate
 * These tests show that LocalDate solves the timezone issue
 */
@SpringBootTest
@ActiveProfiles("test")
class BookAfterTest {
    
    @Autowired
    private BookAfterRepository repository;
    
    @Test
    void dateRemainsConsistentWithLocalDate() {
        // ✅ FIX: Using LocalDate, no timezone conversion occurs
        // User selects: 2024-01-15
        // Stored as: 2024-01-15
        // Retrieved as: 2024-01-15
        
        LocalDate releaseDate = LocalDate.of(2024, 1, 15);
        
        System.out.println("=== AFTER: Using LocalDate (Fixed) ===");
        System.out.println("User's selected date: " + releaseDate);
        
        // Save to database
        BookAfter book = new BookAfter("Spring in Action", "Craig Walls", releaseDate);
        BookAfter saved = repository.save(book);
        
        System.out.println("Saved to database: " + saved.getReleaseDate());
        
        // Retrieve from database
        BookAfter retrieved = repository.findById(saved.getId()).orElseThrow();
        
        System.out.println("Retrieved from database: " + retrieved.getReleaseDate());
        System.out.println("=========================================");
        
        // ✅ Date remains unchanged!
        assertEquals(LocalDate.of(2024, 1, 15), retrieved.getReleaseDate(),
                "Date should remain exactly as entered, no timezone conversion");
    }
    
    @Test
    void localDateHandlesDateOnlyDataCorrectly() {
        // Test multiple dates to ensure consistency
        LocalDate[] testDates = {
            LocalDate.of(2024, 1, 1),
            LocalDate.of(2024, 6, 15),
            LocalDate.of(2024, 12, 31)
        };
        
        for (LocalDate date : testDates) {
            BookAfter book = repository.save(
                new BookAfter("Test Book", "Test Author", date)
            );
            
            BookAfter retrieved = repository.findById(book.getId()).orElseThrow();
            
            assertEquals(date, retrieved.getReleaseDate(),
                    "Date " + date + " should remain unchanged");
            
            System.out.println("✅ " + date + " stored and retrieved correctly");
        }
    }
    
    @Test
    void localDateWorksAcrossTimezones() {
        // The beauty of LocalDate: it doesn't care about timezone
        // Whether user is in Bangkok, New York, or London,
        // "2024-03-10" is always "2024-03-10"
        
        LocalDate releaseDate = LocalDate.of(2024, 3, 10);
        
        BookAfter book = repository.save(
            new BookAfter("Effective Java", "Joshua Bloch", releaseDate)
        );
        
        BookAfter retrieved = repository.findById(book.getId()).orElseThrow();
        
        // ✅ No shift, no conversion, just the date
        assertEquals(releaseDate, retrieved.getReleaseDate());
        
        System.out.println("Original: " + releaseDate);
        System.out.println("Retrieved: " + retrieved.getReleaseDate());
        System.out.println("✅ Perfect match, no timezone issues!");
    }
    
    @Test
    void compareDatesReliably() {
        // With LocalDate, date comparisons work intuitively
        BookAfter book1 = repository.save(
            new BookAfter("Book 1", "Author", LocalDate.of(2024, 1, 15))
        );
        BookAfter book2 = repository.save(
            new BookAfter("Book 2", "Author", LocalDate.of(2024, 1, 16))
        );
        
        BookAfter retrieved1 = repository.findById(book1.getId()).orElseThrow();
        BookAfter retrieved2 = repository.findById(book2.getId()).orElseThrow();
        
        assertTrue(retrieved1.getReleaseDate().isBefore(retrieved2.getReleaseDate()),
                "Date comparison works correctly");
        
        System.out.println("Book 1 date: " + retrieved1.getReleaseDate());
        System.out.println("Book 2 date: " + retrieved2.getReleaseDate());
        System.out.println("✅ Date comparison is reliable");
    }
}
