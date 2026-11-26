# Case 01: Timezone Bug
# กรณีที่ 01: บัคเรื่อง Timezone

## 🐛 Problem / ปัญหา

**English**: Dates are shifting by one day when traveling between Angular frontend, Spring Boot API, and MySQL database. Users in Thailand (Asia/Bangkok, UTC+7) see incorrect dates - events scheduled for 2024-01-15 appear as 2024-01-14.

**ไทย**: วันที่เปลี่ยนไปหนึ่งวันเมื่อข้อมูลเดินทางระหว่าง Angular frontend, Spring Boot API และ MySQL database ผู้ใช้ในประเทศไทย (Asia/Bangkok, UTC+7) เห็นวันที่ผิด - กิจกรรมที่กำหนดไว้วันที่ 15 มกราคม 2024 แสดงเป็นวันที่ 14 มกราคม 2024

## 📋 Scenario / สถานการณ์

A simple bookstore app that tracks book release dates. Users report that release dates are off by one day.

แอปพลิเคชันร้านหนังสือที่เก็บวันที่วางจำหน่ายหนังสือ ผู้ใช้รายงานว่าวันที่วางจำหน่ายผิดไปหนึ่งวัน

## 🔍 Root Cause / สาเหตุ

**English**:
1. **Frontend**: Angular sends dates as ISO 8601 strings with local timezone (`2024-01-15T00:00:00+07:00`)
2. **Backend**: Spring Boot parses this to `Instant`/`ZonedDateTime` and converts to UTC (`2024-01-14T17:00:00Z`)
3. **Database**: MySQL stores as `DATETIME` without timezone info, losing context
4. **Return path**: Backend sends UTC time, frontend interprets in local timezone, shifting date again

**ไทย**:
1. **Frontend**: Angular ส่งวันที่เป็น ISO 8601 string พร้อม timezone ท้องถิ่น (`2024-01-15T00:00:00+07:00`)
2. **Backend**: Spring Boot แปลงเป็น `Instant`/`ZonedDateTime` และเปลี่ยนเป็น UTC (`2024-01-14T17:00:00Z`)
3. **Database**: MySQL เก็บเป็น `DATETIME` โดยไม่มีข้อมูล timezone ทำให้ข้อมูล context หายไป
4. **ทางกลับ**: Backend ส่ง UTC time กลับมา frontend แปลงเป็น timezone ท้องถิ่น ทำให้วันที่เปลี่ยนอีกครั้ง

## ✅ Solution / วิธีแก้

**English**:
- Use `LocalDate` for date-only fields (no time component)
- Configure Jackson to serialize dates properly
- Set application timezone consistently
- Use `DATE` column type in MySQL (not `DATETIME`)

**ไทย**:
- ใช้ `LocalDate` สำหรับฟิลด์ที่เป็นวันที่อย่างเดียว (ไม่มีเวลา)
- ตั้งค่า Jackson ให้ serialize วันที่อย่างถูกต้อง
- ตั้งค่า timezone ของแอปพลิเคชันให้สอดคล้องกัน
- ใช้ชนิดคอลัมน์ `DATE` ใน MySQL (ไม่ใช่ `DATETIME`)

## 🏃 How to Run / วิธีรัน

### Running Tests

```bash
cd backend
mvn test
```

**Note**: Tests need no Docker. The BEFORE Instant shift is asserted by switching the JVM default TimeZone between Asia/Bangkok and UTC. LocalDate round-trips use H2.

## 🧪 Test Results / ผลการทดสอบ

**Database**: H2 for LocalDate persistence; BEFORE Instant shift is proven with JVM default TimeZone (UTC vs Asia/Bangkok), no Docker/Testcontainers.

**BEFORE** (using Instant):
- ❌ Bangkok calendar date stored as Instant at local midnight becomes previous day under a UTC JVM
- ❌ Asserted: 2024-01-15 → Instant `2024-01-14T17:00:00Z` → LocalDate 2024-01-14 on UTC JVM

**AFTER** (using LocalDate):
- ✅ Date 2024-01-15 stays 2024-01-15 in H2
- ✅ Unchanged when JVM default switches between UTC and Asia/Bangkok


## 📚 Key Lessons / บทเรียน

**English**:
1. **Use the right type**: `LocalDate` for dates, `LocalDateTime` for timestamps, `Instant` for UTC timestamps
2. **Be explicit**: Always specify timezone handling in configurations
3. **Test thoroughly**: Include timezone-sensitive test cases
4. **Document**: Make timezone assumptions clear in API documentation

**ไทย**:
1. **ใช้ type ที่เหมาะสม**: `LocalDate` สำหรับวันที่, `LocalDateTime` สำหรับวันที่และเวลา, `Instant` สำหรับ UTC timestamp
2. **ระบุให้ชัดเจน**: ระบุการจัดการ timezone ใน configuration เสมอ
3. **ทดสอบอย่างละเอียด**: รวมกรณีทดสอบที่เกี่ยวกับ timezone
4. **จดบันทึก**: ระบุสมมติฐานเรื่อง timezone ใน API documentation ให้ชัดเจน

## 🔗 Related Resources

- [Spring Boot Date/Time Documentation](https://docs.spring.io/spring-framework/docs/current/reference/html/core.html#format-datetime)
- [Java Time API Best Practices](https://www.baeldung.com/java-8-date-time-intro)
- [MySQL Timezone Handling](https://dev.mysql.com/doc/refman/8.0/en/time-zone-support.html)
