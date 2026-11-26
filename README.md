# Bug-Fix Casebook Portfolio
# คลังตัวอย่างการแก้ไข Bug สำหรับ Portfolio

A collection of common bug scenarios and their fixes for learning purposes.

คลังรวมตัวอย่างบัค (Bug) ทั่วไปและวิธีการแก้ไข สำหรับการเรียนรู้

## Technology Stack

- **Backend**: Spring Boot 3.2+, Java 17+, Spring Data JPA
- **Frontend**: Angular 17+ (code snippets for Case 05)
- **Database (tests)**: H2 in-memory (no Docker required)
- **Testing**: JUnit 5, Hibernate Statistics (Case 02), concurrent ExecutorService (Case 04)
- **CI/CD**: GitHub Actions

## Cases Overview

| Case | Bug Type | How tests run | Tests |
|------|----------|---------------|-------|
| [01](./case-01-timezone-bug) | Timezone Issues | JVM TimeZone + H2 LocalDate | 4 |
| [02](./case-02-n-plus-one) | N+1 Query Problem | H2 + Hibernate Statistics | 3 |
| [03](./case-03-missing-pagination) | Missing Pagination | H2 | 4 |
| [04](./case-04-race-condition) | Race Condition | H2 + 20-thread concurrency | 3 |
| [05](./case-05-angular-memory-leak) | Memory Leak | Backend API + Angular snippets | 1 |
| [06](./case-06-validation-handling) | Error Handling | H2 / MockMvc | 4 |

## Quick Start

### Run all tests

```bash
./run-all-tests.sh
```

### Run one case

```bash
cd case-01-timezone-bug/backend && mvn test
cd case-02-n-plus-one/backend && mvn test
cd case-03-missing-pagination/backend && mvn test
cd case-04-race-condition/backend && mvn test
cd case-05-angular-memory-leak/backend && mvn test
cd case-06-validation-handling/backend && mvn test
```

No Docker is required for tests. All cases use H2 (Case 01 also proves Instant vs LocalDate with JVM default TimeZone).

## What each case shows

- **Problem**: What the bug looks like
- **Root Cause**: Why it happens
- **Solution**: How to fix it
- **Tests**: BEFORE asserts the bug; AFTER asserts the fix
- **Lessons**: Short takeaways (Thai + English)

## Documentation

Each case folder has a bilingual README (Thai + English) with symptom, reproduce steps, root cause, fix, verification, and lessons.

## CI

GitHub Actions runs `mvn test` for every case on push and pull request.
