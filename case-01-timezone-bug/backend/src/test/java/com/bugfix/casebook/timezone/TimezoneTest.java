package com.bugfix.casebook.timezone;

import com.bugfix.casebook.timezone.after.BookAfter;
import com.bugfix.casebook.timezone.after.BookAfterRepository;
import com.bugfix.casebook.timezone.before.BookBefore;
import com.bugfix.casebook.timezone.before.BookBeforeRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.time.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests demonstrating timezone bug with Instant vs LocalDate
 * Uses MySQL via Testcontainers to show real timezone behavior
 */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
class TimezoneTest {
    
    @Autowired
    private BookBeforeRepository beforeRepo;
    
    @Autowired
    private BookAfterRepository afterRepo;
    
    @Test
    void beforeVersion_InstantCausesDateShift() {
        System.out.println("=== BEFORE: Instant Causes Date Shift ===");
        
        // User in Bangkok (UTC+7) selects date: 2024-01-15
        // Frontend sends midnight Bangkok time
        LocalDate intendedDate = LocalDate.of(2024, 1, 15);
        ZonedDateTime bangkokMidnight = intendedDate.atStartOfDay(ZoneId.of("Asia/Bangkok"));
        Instant instant = bangkokMidnight.toInstant();
        
        System.out.println("Intended date: " + intendedDate);
        System.out.println("Bangkok midnight: " + bangkokMidnight);
        System.out.println("As Instant (UTC): " + instant);
        
        // Save with Instant
        BookBefore book = new BookBefore("Spring in Action", "Craig Walls", instant);
        BookBefore saved = beforeRepo.save(book);
        
        // Retrieve
        BookBefore retrieved = beforeRepo.findById(saved.getId()).orElseThrow();
        
        // Convert back to Bangkok timezone to see what user sees
        ZonedDateTime retrievedInBangkok = retrieved.getReleaseDate()
                .atZone(ZoneId.of("Asia/Bangkok"));
        LocalDate dateSeenByUser = retrievedInBangkok.toLocalDate();
        
        System.out.println("Retrieved Instant: " + retrieved.getReleaseDate());
        System.out.println("Converted to Bangkok: " + retrievedInBangkok);
        System.out.println("Date seen by user: " + dateSeenByUser);
        
        // ❌ BUG: The date shifts!
        // Bangkok midnight (00:00+07:00) = UTC 17:00 previous day
        // When converted back to Bangkok date, it may show as previous day
        assertNotEquals(intendedDate, dateSeenByUser,
                "BUG: Date shifted from " + intendedDate + " to " + dateSeenByUser + " due to timezone conversion");
        
        System.out.println("=========================================");
    }
    
    @Test
    void afterVersion_LocalDatePreservesDate() {
        System.out.println("=== AFTER: LocalDate Preserves Date ===");
        
        // Store with LocalDate - no time, no timezone
        LocalDate releaseDate = LocalDate.of(2024, 1, 15);
        
        System.out.println("Original date: " + releaseDate);
        
        BookAfter book = new BookAfter("Spring in Action", "Craig Walls", releaseDate);
        BookAfter saved = afterRepo.save(book);
        
        // Retrieve
        BookAfter retrieved = afterRepo.findById(saved.getId()).orElseThrow();
        
        System.out.println("Retrieved date: " + retrieved.getReleaseDate());
        
        // ✅ FIX: Date is exactly preserved
        assertEquals(releaseDate, retrieved.getReleaseDate(),
                "LocalDate should preserve the date exactly");
        assertEquals(LocalDate.of(2024, 1, 15), retrieved.getReleaseDate());
        
        System.out.println("=========================================");
    }
    
    @Test
    void demonstrateMidnightShift() {
        System.out.println("=== Demonstrating Midnight Boundary Issue ===");
        
        // Test several dates around midnight Bangkok time
        LocalDate[] testDates = {
            LocalDate.of(2024, 1, 1),
            LocalDate.of(2024, 6, 15),
            LocalDate.of(2024, 12, 31)
        };
        
        for (LocalDate date : testDates) {
            // BEFORE: Instant causes shift
            ZonedDateTime bangkokMidnight = date.atStartOfDay(ZoneId.of("Asia/Bangkok"));
            Instant instant = bangkokMidnight.toInstant();
            
            BookBefore bookBefore = beforeRepo.save(
                new BookBefore("Test", "Author", instant)
            );
            
            LocalDate retrievedDateBefore = bookBefore.getReleaseDate()
                    .atZone(ZoneId.of("Asia/Bangkok"))
                    .toLocalDate();
            
            // AFTER: LocalDate preserves
            BookAfter bookAfter = afterRepo.save(
                new BookAfter("Test", "Author", date)
            );
            
            LocalDate retrievedDateAfter = bookAfter.getReleaseDate();
            
            System.out.println("Original: " + date + 
                               " | With Instant: " + retrievedDateBefore + 
                               " | With LocalDate: " + retrievedDateAfter);
            
            // Assert the difference
            assertEquals(date, retrievedDateAfter, "LocalDate should preserve date");
        }
        
        System.out.println("=========================================");
    }
    
    @Test
    void localDateHandlesAllTimezones() {
        System.out.println("=== LocalDate Works Across Timezones ===");
        
        LocalDate date = LocalDate.of(2024, 3, 15);
        
        // Store
        BookAfter book = afterRepo.save(new BookAfter("Test Book", "Author", date));
        
        // Retrieve
        BookAfter retrieved = afterRepo.findById(book.getId()).orElseThrow();
        
        // The date is the same regardless of timezone
        assertEquals(date, retrieved.getReleaseDate());
        
        System.out.println("LocalDate " + date + " remains " + retrieved.getReleaseDate() + 
                           " regardless of timezone");
        System.out.println("=========================================");
    }
}
