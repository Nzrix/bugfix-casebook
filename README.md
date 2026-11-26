# Bug-Fix Casebook Portfolio
# คลังตัวอย่างการแก้ไข Bug สำหรับ Portfolio

A collection of real-world bug scenarios and their fixes, demonstrating debugging skills and best practices for junior full-stack developers.

คลังรวมตัวอย่างบัค (Bug) ที่พบได้จริงและวิธีการแก้ไข เพื่อแสดงทักษะการแก้ปัญหาและแนวปฏิบัติที่ดีสำหรับนักพัฒนา Full-Stack มือใหม่

## 🛠 Technology Stack

- **Backend**: Spring Boot 3.2+, Java 17+, Spring Data JPA
- **Frontend**: Angular 17+
- **Database**: MySQL 8.0 (with Testcontainers for integration tests)
- **Testing**: JUnit 5, Mockito, Angular Testing Library
- **CI/CD**: GitHub Actions

## 📚 Cases Overview

| Case | Bug Type | Technologies | Description |
|------|----------|--------------|-------------|
| [01](./case-01-timezone-bug) | Timezone Issues | Spring Boot, Angular, MySQL | Date shifting between Asia/Bangkok and UTC zones |
| [02](./case-02-n-plus-one) | N+1 Query Problem | Spring Data JPA | Performance issue with lazy loading |
| [03](./case-03-missing-pagination) | Missing Pagination | Spring Boot, JPA | Loading huge datasets without pagination |
| [04](./case-04-race-condition) | Race Condition | JPA, Transactions | Lost updates in concurrent stock operations |
| [05](./case-05-angular-memory-leak) | Memory Leak | Angular, RxJS | Unmanaged subscriptions causing memory leaks |
| [06](./case-06-validation-handling) | Error Handling | Spring Boot | Converting 500 errors to proper 400 responses |

## 🚀 Quick Start

### Prerequisites

- Java 17 or higher
- Node.js 18+ and npm
- Docker and Docker Compose
- Maven 3.8+

### Run All Tests

```bash
# Run all backend tests (includes Testcontainers for MySQL)
./run-all-tests.sh

# Or run individually:
cd case-01-timezone-bug/backend && mvn test
cd case-02-n-plus-one/backend && mvn test
# ... etc
```

### Run Individual Cases

Each case folder contains its own README with specific instructions. Generally:

```bash
# Backend
cd case-XX-name/backend
mvn spring-boot:run

# Frontend (if applicable)
cd case-XX-name/frontend
npm install
npm start
```

## 📖 Learning Objectives

Each case demonstrates:
- ✅ **Problem Recognition**: How to identify the bug
- ✅ **Root Cause Analysis**: Why the bug occurs
- ✅ **Solution Implementation**: How to fix it properly
- ✅ **Verification**: Testing that proves the fix works
- ✅ **Best Practices**: Lessons learned and prevention tips

## 🎯 For Employers & Clients

This portfolio demonstrates:
- Strong debugging methodology
- Understanding of common pitfalls in Spring Boot and Angular
- Ability to write clean, testable code
- Clear communication (bilingual documentation)
- Production-ready practices (CI/CD, automated testing)

## 📝 Documentation Structure

Each case includes:
- **README.md**: Bilingual (Thai/English) explanation
- **BEFORE version**: Demonstrates the bug
- **AFTER version**: Shows the fix
- **Tests**: Automated verification
- **Lessons Learned**: Key takeaways

## 🔍 CI/CD Status

![CI](https://github.com/YOUR_USERNAME/bugfix-casebook/workflows/CI/badge.svg)

All tests run automatically on every push via GitHub Actions.

## 📧 Contact

This portfolio is maintained for demonstration purposes. All examples use toy domains (bookstore, todo app, etc.) and are entirely original work.

---

**Note**: This is a learning portfolio. All code is simplified for clarity and interview discussion. Real production code may require additional considerations for security, scalability, and maintainability.
