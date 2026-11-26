package com.bugfix.casebook.timezone.before;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.*;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests demonstrating the BEFORE state - the timezone bug
 * This test documents the problem: dates shift when using Instant
 */
@SpringBootTest
@ActiveProfiles("test")
class BookBeforeTest {
    
    @Autowired
    private BookBeforeRepository repository;
    
    @Test
    void demonstrateTimezoneBug() {
        // Scenario: User in Bangkok (UTC+7) creates a book with release date 2024-01-15
        // Frontend sends: 2024-01-15T00:00:00+07:00
        
        // This is what frontend sends (midnight in Bangkok)
        ZonedDateTime bangkokMidnight = ZonedDateTime.of(
            LocalDate.of(2024, 1, 15),
            LocalTime.MIDNIGHT,
            ZoneId.of("Asia/Bangkok")
        );
        Instant instant = bangkokMidnight.toInstant();
        
        System.out.println("=== BEFORE: Demonstrating Timezone Bug ===");
        System.out.println("User's intended date: 2024-01-15");
        System.out.println("Bangkok time sent: " + bangkokMidnight);
        System.out.println("As Instant (UTC): " + instant);
        
        // Save to database
        BookBefore book = new BookBefore("Spring in Action", "Craig Walls", instant);
        BookBefore saved = repository.save(book);
        
        // Retrieve from database
        BookBefore retrieved = repository.findById(saved.getId()).orElseThrow();
        
        // Convert back to Bangkok timezone to see what user sees
        ZonedDateTime retrievedInBangkok = retrieved.getReleaseDate()
                .atZone(ZoneId.of("Asia/Bangkok"));
        LocalDate dateSeenByUser = retrievedInBangkok.toLocalDate();
        
        System.out.println("Retrieved Instant: " + retrieved.getReleaseDate());
        System.out.println("Converted to Bangkok: " + retrievedInBangkok);
        System.out.println("Date seen by user: " + dateSeenByUser);
        System.out.println("=========================================");
        
        // ❌ BUG: The date has shifted!
        // Expected: 2024-01-15
        // Actual: 2024-01-14 (because UTC conversion happened)
        
        // This assertion documents the bug - it will fail in the expected way
        assertNotEquals(LocalDate.of(2024, 1, 15), dateSeenByUser,
                "BUG DEMONSTRATED: Date shifted from 2024-01-15 to " + dateSeenByUser);
        
        // Show the actual wrong date
        assertEquals(LocalDate.of(2024, 1, 14), dateSeenByUser,
                "Date shifted back by one day due to timezone conversion");
    }
    
    @Test
    void showHowTimezoneConversionCausesDateShift() {
        // The problem in detail:
        // 2024-01-15T00:00:00+07:00 (Bangkok) = 2024-01-14T17:00:00Z (UTC)
        // When stored and retrieved, the date component changes!
        
        LocalDate intendedDate = LocalDate.of(2024, 3, 10);
        
        // User in Bangkok selects date: 2024-03-10
        ZonedDateTime bangkokDate = intendedDate.atStartOfDay(ZoneId.of("Asia/Bangkok"));
        Instant utcInstant = bangkokDate.toInstant();
        
        // Save
        BookBefore book = repository.save(
            new BookBefore("Effective Java", "Joshua Bloch", utcInstant)
        );
        
        // Retrieve
        BookBefore retrieved = repository.findById(book.getId()).orElseThrow();
        
        // What the user sees after round-trip
        LocalDate resultDate = retrieved.getReleaseDate()
                .atZone(ZoneId.of("Asia/Bangkok"))
                .toLocalDate();
        
        System.out.println("Intended: " + intendedDate);
        System.out.println("After round-trip: " + resultDate);
        
        // ❌ BUG: One day earlier
        assertEquals(LocalDate.of(2024, 3, 9), resultDate);
    }
}
