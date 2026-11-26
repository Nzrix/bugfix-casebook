# Case 02: N+1 Query Problem
# กรณีที่ 02: ปัญหา N+1 Query

## 🐛 Problem / ปัญหา

**English**: A simple endpoint to list authors and their books executes 1 query to fetch authors, then N additional queries (one per author) to fetch their books. For 100 authors, this means 101 database queries instead of 1-2.

**ไทย**: Endpoint ธรรมดาที่แสดงรายการนักเขียนและหนังสือของพวกเขา ทำงานโดยใช้ 1 query เพื่อดึงนักเขียน แล้วต่อด้วย N queries เพิ่มเติม (หนึ่ง query ต่อนักเขียนหนึ่งคน) เพื่อดึงหนังสือ สำหรับนักเขียน 100 คน หมายความว่าใช้ 101 queries แทนที่จะเป็น 1-2 queries

## 📋 Scenario / สถานการณ์

A bookstore API that shows authors with their published books. Each author can have multiple books (one-to-many relationship).

API ร้านหนังสือที่แสดงนักเขียนพร้อมหนังสือที่พวกเขาเขียน นักเขียนแต่ละคนสามารถมีหลายเล่มได้ (ความสัมพันธ์แบบ one-to-many)

## 🔍 Root Cause / สาเหตุ

**English**:
1. **Lazy Loading Default**: JPA relationships are lazy by default
2. **N+1 Pattern**: 
   - 1 query fetches all authors: `SELECT * FROM authors`
   - For each author, another query fetches books: `SELECT * FROM books WHERE author_id = ?`
3. **Hidden Cost**: The queries happen transparently when accessing `author.getBooks()`

**ไทย**:
1. **Lazy Loading เป็นค่าเริ่มต้น**: ความสัมพันธ์ใน JPA เป็น lazy loading ตามค่าเริ่มต้น
2. **รูปแบบ N+1**:
   - 1 query ดึงนักเขียนทั้งหมด: `SELECT * FROM authors`
   - สำหรับแต่ละนักเขียน มี query แยกดึงหนังสือ: `SELECT * FROM books WHERE author_id = ?`
3. **ค่าใช้จ่ายที่ซ่อนอยู่**: Query เกิดขึ้นโดยอัตโนมัติเมื่อเข้าถึง `author.getBooks()`

## ✅ Solution / วิธีแก้

**English**:
Three approaches (all demonstrated):

1. **Fetch Join**: Use JPQL `JOIN FETCH` to load associations in one query
2. **Entity Graph**: Use `@EntityGraph` annotation for declarative fetch strategy
3. **DTO Projection**: Project only needed fields with constructor expression

**ไทย**:
สามวิธีแก้ (แสดงทั้งหมด):

1. **Fetch Join**: ใช้ JPQL `JOIN FETCH` เพื่อโหลดความสัมพันธ์ใน query เดียว
2. **Entity Graph**: ใช้ annotation `@EntityGraph` เพื่อกำหนดกลยุทธ์การ fetch
3. **DTO Projection**: ดึงเฉพาะฟิลด์ที่ต้องการด้วย constructor expression

## 🏃 How to Run / วิธีรัน

### BEFORE Version (N+1 Problem)

```bash
cd backend
mvn test -Dtest=AuthorControllerBeforeTest

# See the N+1 queries in action
mvn spring-boot:run -Dspring-boot.run.profiles=before
# Then: curl http://localhost:8082/api/before/authors
```

### AFTER Version (Fixed)

```bash
cd backend
mvn test -Dtest=AuthorControllerAfterTest

# See efficient queries
mvn spring-boot:run
# Then: curl http://localhost:8082/api/after/authors
```

## 🧪 Test Results / ผลการทดสอบ

**BEFORE**:
- ❌ 10 authors → 11 queries (1 + 10)
- ❌ Query time increases linearly with number of authors
- ❌ Performance degrades severely with large datasets

**AFTER (Fetch Join)**:
- ✅ 10 authors → 1 query
- ✅ Constant query time regardless of author count
- ✅ Performance improvement: ~90% reduction in query count

**AFTER (Entity Graph)**:
- ✅ 10 authors → 1 query
- ✅ Declarative, cleaner code

**AFTER (DTO Projection)**:
- ✅ 10 authors → 1 query
- ✅ Less memory usage, optimal for read-only operations

## 📚 Key Lessons / บทเรียน

**English**:
1. **Monitor Queries**: Always check how many queries your code generates
2. **Understand Lazy Loading**: Know when lazy loading triggers additional queries
3. **Choose Wisely**: 
   - Fetch Join for most cases
   - Entity Graph for complex scenarios
   - DTO Projection for read-only, performance-critical endpoints
4. **Test with Data**: N+1 problems only appear with real data

**ไทย**:
1. **ตรวจสอบ Queries**: ตรวจสอบเสมอว่าโค้ดของคุณสร้าง query กี่ตัว
2. **เข้าใจ Lazy Loading**: รู้ว่าเมื่อไหร่ที่ lazy loading จะทำให้เกิด query เพิ่ม
3. **เลือกอย่างชาญฉลาด**:
   - Fetch Join สำหรับส่วนใหญ่
   - Entity Graph สำหรับสถานการณ์ซับซ้อน
   - DTO Projection สำหรับ endpoint ที่อ่านอย่างเดียวและต้องการ performance สูง
4. **ทดสอบกับข้อมูลจริง**: ปัญหา N+1 จะปรากฏเฉพาะเมื่อมีข้อมูลจริง

## 🔗 Related Resources

- [Hibernate N+1 Problem](https://www.baeldung.com/hibernate-common-performance-problems-in-logs)
- [Entity Graph Documentation](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#jpa.entity-graph)
- [DTO Projections](https://www.baeldung.com/jpa-queries-custom-result-with-aggregation-functions)
