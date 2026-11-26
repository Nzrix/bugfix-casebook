package com.bugfix.casebook.timezone;

import com.bugfix.casebook.timezone.after.BookAfter;
import com.bugfix.casebook.timezone.after.BookAfterRepository;
import com.bugfix.casebook.timezone.before.BookBefore;
import com.bugfix.casebook.timezone.before.BookBeforeRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests demonstrating timezone handling with Instant vs LocalDate
 * Note: Using H2 for simplicity. MySQL with specific timezone settings would show more dramatic differences.
 */
@SpringBootTest
@ActiveProfiles("test")
class TimezoneTest {
    
    @Autowired
    private BookBeforeRepository beforeRepo;
    
    @Autowired
    private BookAfterRepository afterRepo;
    
    @Test
    void beforeVersion_InstantStoresDateTime() {
        System.out.println("=== BEFORE: Using Instant (stores date+time) ===");
        
        // Create a date at midnight Bangkok time
        ZonedDateTime bangkokMidnight = ZonedDateTime.of(
            LocalDate.of(2024, 1, 15),
            LocalTime.MIDNIGHT,
            ZoneId.of("Asia/Bangkok")
        );
        Instant instant = bangkokMidnight.toInstant();
        
        // Save with Instant
        BookBefore book = new BookBefore("Spring in Action", "Craig Walls", instant);
        BookBefore saved = beforeRepo.save(book);
        
        // Retrieve
        BookBefore retrieved = beforeRepo.findById(saved.getId()).orElseThrow();
        
        System.out.println("Original instant: " + instant);
        System.out.println("Retrieved instant: " + retrieved.getReleaseDate());
        
        // The instant is preserved, but includes time component
        assertEquals(instant, retrieved.getReleaseDate());
        
        // Converting back to Bangkok date may differ due to time component
        LocalDate retrievedDate = retrieved.getReleaseDate()
                .atZone(ZoneId.of("Asia/Bangkok"))
                .toLocalDate();
        
        System.out.println("Converted to Bangkok date: " + retrievedDate);
        System.out.println("=========================================");
        
        // Instant works but includes unnecessary time information for date-only fields
        assertNotNull(retrieved.getReleaseDate());
    }
    
    @Test
    void afterVersion_LocalDateStoresOnlyDate() {
        System.out.println("=== AFTER: Using LocalDate (stores date only) ===");
        
        // Store with LocalDate - no time component, no timezone
        LocalDate releaseDate = LocalDate.of(2024, 1, 15);
        
        BookAfter book = new BookAfter("Spring in Action", "Craig Walls", releaseDate);
        BookAfter saved = afterRepo.save(book);
        
        // Retrieve
        BookAfter retrieved = afterRepo.findById(saved.getId()).orElseThrow();
        
        System.out.println("Original date: " + releaseDate);
        System.out.println("Retrieved date: " + retrieved.getReleaseDate());
        
        // ✅ Date is exactly preserved
        assertEquals(releaseDate, retrieved.getReleaseDate());
        assertEquals(LocalDate.of(2024, 1, 15), retrieved.getReleaseDate());
        
        System.out.println("=========================================");
    }
    
    @Test
    void localDateHandlesMultipleDatesCorrectly() {
        // Test that LocalDate consistently handles various dates
        LocalDate[] testDates = {
            LocalDate.of(2024, 1, 1),
            LocalDate.of(2024, 6, 15),
            LocalDate.of(2024, 12, 31)
        };
        
        for (LocalDate date : testDates) {
            BookAfter book = afterRepo.save(
                new BookAfter("Test Book", "Author", date)
            );
            
            BookAfter retrieved = afterRepo.findById(book.getId()).orElseThrow();
            
            assertEquals(date, retrieved.getReleaseDate(),
                    "Date " + date + " should be preserved exactly");
        }
        
        System.out.println("✅ All dates stored and retrieved correctly");
    }
    
    @Test
    void localDateComparisonsWork() {
        // LocalDate makes date comparisons straightforward
        BookAfter book1 = afterRepo.save(
            new BookAfter("Book 1", "Author", LocalDate.of(2024, 1, 15))
        );
        BookAfter book2 = afterRepo.save(
            new BookAfter("Book 2", "Author", LocalDate.of(2024, 1, 16))
        );
        
        BookAfter retrieved1 = afterRepo.findById(book1.getId()).orElseThrow();
        BookAfter retrieved2 = afterRepo.findById(book2.getId()).orElseThrow();
        
        assertTrue(retrieved1.getReleaseDate().isBefore(retrieved2.getReleaseDate()));
        
        System.out.println("✅ Date comparisons work reliably");
    }
}
