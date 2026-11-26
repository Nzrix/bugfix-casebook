# Bug-Fix Casebook Portfolio
# คลังตัวอย่างการแก้ไข Bug สำหรับ Portfolio

A collection of common bug scenarios and their fixes for learning purposes.

คลังรวมตัวอย่างบัค (Bug) ทั่วไปและวิธีการแก้ไข สำหรับการเรียนรู้

## 🛠 Technology Stack

- **Backend**: Spring Boot 3.2+, Java 17+, Spring Data JPA
- **Frontend**: Angular 17+
- **Database**: MySQL 8.0 (Testcontainers for tests - requires Docker)
- **Testing**: JUnit 5, Mockito, Testcontainers
- **CI/CD**: GitHub Actions

## ⚠️ Testing Requirements

**Cases 1, 2, and 4 require Docker** to run tests locally (they use Testcontainers with MySQL).  
**GitHub Actions CI has Docker** and runs all tests automatically.

For local development without Docker:
- Cases 3, 5, 6 work without Docker
- Cases 1, 2, 4 demonstrate the patterns in code but tests need Docker

## 📚 Cases Overview

| Case | Bug Type | Technologies | Docker Required |
|------|----------|--------------|-----------------|
| [01](./case-01-timezone-bug) | Timezone Issues | Spring Boot, MySQL | Yes (Test containers) |
| [02](./case-02-n-plus-one) | N+1 Query Problem | Spring Data JPA, MySQL | Yes (Testcontainers) |
| [03](./case-03-missing-pagination) | Missing Pagination | Spring Boot, JPA | No |
| [04](./case-04-race-condition) | Race Condition | JPA, Transactions, MySQL | Yes (Testcontainers) |
| [05](./case-05-angular-memory-leak) | Memory Leak | Angular, RxJS | No |
| [06](./case-06-validation-handling) | Error Handling | Spring Boot | No |

## 🚀 Quick Start

### Run All Tests (Requires Docker for cases 1,2,4)

```bash
# Ensure Docker is running
docker version

# Run all tests
./run-all-tests.sh
```

### Run Individual Cases

```bash
# Cases WITHOUT Docker requirement (3, 5, 6)
cd case-03-missing-pagination/backend && mvn test
cd case-05-angular-memory-leak/backend && mvn test
cd case-06-validation-handling/backend && mvn test

# Cases WITH Docker requirement (1, 2, 4)
# Docker must be running
cd case-01-timezone-bug/backend && mvn test
cd case-02-n-plus-one/backend && mvn test
cd case-04-race-condition/backend && mvn test
```

## 📖 Learning Objectives

Each case shows:
- **Problem**: What the bug looks like
- **Root Cause**: Why it happens
- **Solution**: How to fix it
- **Tests**: Verification that the fix works
- **Lessons**: What to remember

## 🎯 Learning Goals

This portfolio shows:
- Identifying and fixing common bugs
- Understanding Spring Boot and Angular issues
- Writing tests to verify fixes
- Documenting code in both Thai and English

## 📝 Documentation Structure

Each case includes:
- **README.md**: Bilingual (Thai/English) explanation
- **BEFORE version**: Demonstrates the bug
- **AFTER version**: Shows the fix
- **Tests**: Automated verification
- **Lessons Learned**: Key takeaways

## 🔍 CI/CD Status

![CI](https://github.com/Nzrix/bugfix-casebook/workflows/Bug-Fix%20Casebook%20CI/badge.svg)

All tests run automatically on every push via GitHub Actions (Docker available in CI).

## 📧 Contact

This portfolio is maintained for demonstration purposes. All examples use toy domains (bookstore, todo app, etc.) and are entirely original work.

---

**Note**: This is a learning portfolio. Cases 1, 2, and 4 require Docker (Testcontainers) for running tests. GitHub Actions CI has Docker and validates all tests automatically.
