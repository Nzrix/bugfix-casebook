package com.bugfix.casebook.timezone;

import com.bugfix.casebook.timezone.after.BookAfter;
import com.bugfix.casebook.timezone.after.BookAfterRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Timezone bug proof without Testcontainers.
 * BEFORE: Instant + JVM default timezone shift (UTC vs Asia/Bangkok).
 * AFTER: LocalDate persists unchanged on H2.
 */
@SpringBootTest
class TimezoneTest {

    private final TimeZone originalTz = TimeZone.getDefault();

    @Autowired
    private BookAfterRepository afterRepo;

    @AfterEach
    void restoreTimezone() {
        TimeZone.setDefault(originalTz);
    }

    @Test
    void beforeVersion_InstantCausesDateShiftAcrossJvmTimezones() {
        System.out.println("=== BEFORE: Instant + JVM timezone shift ===");

        LocalDate intendedDate = LocalDate.of(2024, 1, 15);
        ZoneId bangkok = ZoneId.of("Asia/Bangkok");

        // App treats a calendar date as Instant at Bangkok midnight
        Instant storedAsInstant = intendedDate.atStartOfDay(bangkok).toInstant();
        // Bangkok 2024-01-15 00:00+07:00 == 2024-01-14T17:00:00Z
        assertEquals(Instant.parse("2024-01-14T17:00:00Z"), storedAsInstant);

        // Cloud/server JVM often defaults to UTC
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        LocalDate dateSeenOnUtcServer = LocalDate.ofInstant(storedAsInstant, ZoneId.systemDefault());

        System.out.println("Intended (Bangkok calendar date): " + intendedDate);
        System.out.println("Stored Instant (UTC): " + storedAsInstant);
        System.out.println("Displayed on UTC JVM: " + dateSeenOnUtcServer);

        // ❌ BUG: same Instant shows as previous day when JVM is UTC
        assertEquals(LocalDate.of(2024, 1, 14), dateSeenOnUtcServer);
        assertNotEquals(intendedDate, dateSeenOnUtcServer,
                "BUG: Instant shifts " + intendedDate + " → " + dateSeenOnUtcServer + " under UTC JVM");

        System.out.println("=========================================");
    }

    @Test
    void afterVersion_LocalDatePreservesDateOnH2() {
        System.out.println("=== AFTER: LocalDate preserves date ===");

        LocalDate releaseDate = LocalDate.of(2024, 1, 15);
        BookAfter saved = afterRepo.save(new BookAfter("Spring in Action", "Craig Walls", releaseDate));
        BookAfter retrieved = afterRepo.findById(saved.getId()).orElseThrow();

        assertEquals(releaseDate, retrieved.getReleaseDate());
        System.out.println("Stored and retrieved LocalDate: " + retrieved.getReleaseDate());
        System.out.println("=========================================");
    }

    @Test
    void afterVersion_LocalDateUnaffectedByJvmTimezone() {
        System.out.println("=== AFTER: LocalDate independent of JVM TZ ===");

        LocalDate date = LocalDate.of(2024, 3, 15);
        BookAfter saved = afterRepo.save(new BookAfter("Test Book", "Author", date));

        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        LocalDate underUtc = afterRepo.findById(saved.getId()).orElseThrow().getReleaseDate();

        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Bangkok"));
        LocalDate underBangkok = afterRepo.findById(saved.getId()).orElseThrow().getReleaseDate();

        assertEquals(date, underUtc);
        assertEquals(date, underBangkok);
        assertEquals(underUtc, underBangkok);

        System.out.println("LocalDate " + date + " unchanged under UTC and Asia/Bangkok JVM defaults");
        System.out.println("=========================================");
    }

    @Test
    void demonstrateMidnightBoundaryShiftWithoutDb() {
        System.out.println("=== Midnight boundary: Instant vs LocalDate ===");

        LocalDate[] dates = {
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 6, 15),
                LocalDate.of(2024, 12, 31)
        };

        ZoneId bangkok = ZoneId.of("Asia/Bangkok");

        for (LocalDate date : dates) {
            Instant instant = date.atStartOfDay(bangkok).toInstant();

            TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
            LocalDate shifted = LocalDate.ofInstant(instant, ZoneId.systemDefault());

            // LocalDate never shifts with JVM timezone
            LocalDate preserved = date;

            System.out.println("Original: " + date + " | Instant→UTC date: " + shifted + " | LocalDate: " + preserved);

            assertEquals(date.minusDays(1), shifted,
                    "Bangkok midnight Instant must show previous day under UTC JVM");
            assertEquals(date, preserved);
        }

        System.out.println("=========================================");
    }
}
