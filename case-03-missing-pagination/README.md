# Case 03: Missing Pagination
# กรณีที่ 03: ขาด Pagination

## 🐛 Problem / ปัญหา

**English**: An endpoint that lists all products loads the entire table into memory. With 10,000+ products, this causes slow response times, high memory usage, and potential OutOfMemory errors.

**ไทย**: Endpoint ที่แสดงรายการสินค้าทั้งหมดโหลดข้อมูลทั้งตารางเข้าหน่วยความจำ เมื่อมีสินค้ามากกว่า 10,000 รายการ ทำให้ระยะเวลาตอบกลับช้า ใช้หน่วยความจำสูง และอาจเกิดข้อผิดพลาด OutOfMemory

## 📋 Scenario / สถานการณ์

A product catalog API that returns all products. Works fine in development (100 products), but crashes in production (100,000+ products).

API แค็ตตาล็อกสินค้าที่ส่งกลับสินค้าทั้งหมด ทำงานได้ดีใน development (100 สินค้า) แต่ล่มใน production (100,000+ สินค้า)

## 🔍 Root Cause / สาเหตุ

**English**:
1. **No Pagination**: `findAll()` loads everything
2. **Memory Overhead**: Each entity consumes memory
3. **Slow Queries**: Database must scan entire table
4. **Network Bandwidth**: Large JSON payloads

**ไทย**:
1. **ไม่มี Pagination**: `findAll()` โหลดทุกอย่าง
2. **หน่วยความจำสูง**: แต่ละ entity กินหน่วยความจำ
3. **Query ช้า**: ฐานข้อมูลต้องสแกนทั้งตาราง
4. **แบนด์วิดท์เครือข่าย**: JSON payload ขนาดใหญ่

## ✅ Solution / วิธีแก้

**English**:
- Implement server-side pagination with Spring Data JPA `Pageable`
- Return `Page<T>` with metadata (total pages, current page, etc.)
- Add query parameters: `?page=0&size=20&sort=name,asc`

**ไทย**:
- ใช้ pagination ฝั่ง server ด้วย Spring Data JPA `Pageable`
- ส่งกลับ `Page<T>` พร้อม metadata (จำนวนหน้าทั้งหมด หน้าปัจจุบัน ฯลฯ)
- เพิ่ม query parameters: `?page=0&size=20&sort=name,asc`

## 🏃 How to Run / วิธีรัน

```bash
cd backend
mvn test

# BEFORE: Try loading 10,000 products (slow!)
mvn spring-boot:run
curl http://localhost:8083/api/before/products

# AFTER: Paginated results
curl "http://localhost:8083/api/after/products?page=0&size=20"
```

## 🧪 Test Results / ผลการทดสอบ

**BEFORE**:
- ❌ 10,000 products → ~500ms response time
- ❌ ~20MB memory usage
- ❌ All data loaded even if only viewing first page

**AFTER**:
- ✅ 20 products per page → ~50ms response time
- ✅ ~2MB memory usage
- ✅ Efficient `LIMIT` and `OFFSET` queries

## 📚 Key Lessons / บทเรียน

**English**:
1. **Never Load Everything**: Always paginate lists
2. **Default Limits**: Set reasonable default page sizes (20-50)
3. **Client Cooperation**: Frontend must handle pagination
4. **Metadata**: Include total count for UI (page indicators)

**ไทย**:
1. **อย่าโหลดทั้งหมด**: ใช้ pagination กับลิสต์เสมอ
2. **ค่า Default**: ตั้งขนาดหน้าที่เหมาะสม (20-50)
3. **ความร่วมมือของ Client**: Frontend ต้องรองรับ pagination
4. **Metadata**: รวมจำนวนทั้งหมดสำหรับ UI (ตัวบ่งชี้หน้า)
