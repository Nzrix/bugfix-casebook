# Case 04: Race Condition / Lost Update
# กรณีที่ 04: Race Condition / การสูญเสียการ Update

## 🐛 Problem / ปัญหา

**English**: Two concurrent requests trying to decrement product stock result in lost updates. If stock is 10 and two customers simultaneously buy 1 item each, the final stock might be 9 instead of 8.

**ไทย**: สองคำขอพร้อมกันที่พยายามลดสต็อกสินค้าทำให้การอัพเดทสูญหาย ถ้าสต็อกเป็น 10 และลูกค้าสองคนซื้อพร้อมกันคนละ 1 ชิ้น สต็อกสุดท้ายอาจเป็น 9 แทนที่จะเป็น 8

## 📋 Scenario / สถานการณ์

An e-commerce site where multiple users can purchase products simultaneously. Stock must be accurately tracked.

เว็บอีคอมเมิร์ซที่ผู้ใช้หลายคนสามารถซื้อสินค้าพร้อมกัน สต็อกต้องถูกติดตามอย่างแม่นยำ

## 🔍 Root Cause / สาเหตุ

**English**:
1. **Read-Modify-Write Pattern**: 
   - Thread A reads stock = 10
   - Thread B reads stock = 10 (before A commits)
   - Thread A writes stock = 9
   - Thread B writes stock = 9 (overwriting A's update!)
2. **No Locking**: No mechanism to prevent concurrent modifications

**ไทย**:
1. **รูปแบบ Read-Modify-Write**:
   - Thread A อ่านสต็อก = 10
   - Thread B อ่านสต็อก = 10 (ก่อน A commit)
   - Thread A เขียนสต็อก = 9
   - Thread B เขียนสต็อก = 9 (เขียนทับการอัพเดทของ A!)
2. **ไม่มี Locking**: ไม่มีกลไกป้องกันการแก้ไขพร้อมกัน

## ✅ Solution / วิธีแก้

**English**:
Two approaches (both demonstrated):

1. **Optimistic Locking**: Use `@Version` annotation for version-based concurrency control
2. **Atomic Updates**: Use direct UPDATE queries with WHERE conditions

**ไทย**:
สองวิธีแก้ (แสดงทั้งสองแบบ):

1. **Optimistic Locking**: ใช้ annotation `@Version` สำหรับควบคุม concurrency แบบใช้ version
2. **Atomic Updates**: ใช้ UPDATE query โดยตรงพร้อมเงื่อนไข WHERE

## 🏃 How to Run / วิธีรัน

```bash
cd backend
mvn test

# Tests will simulate concurrent updates
mvn test -Dtest=StockTest
```

## 🧪 Test Results / ผลการทดสอบ

**BEFORE**:
- ❌ 10 concurrent decrements → stock might be 1-9 (unpredictable)
- ❌ Lost updates occur frequently

**AFTER (Optimistic Locking)**:
- ✅ 10 concurrent decrements → stock = 0
- ✅ `OptimisticLockException` on conflicts, retry succeeds

**AFTER (Atomic Update)**:
- ✅ 10 concurrent decrements → stock = 0
- ✅ Database handles atomicity

## 📚 Key Lessons / บทเรียน

**English**:
1. **Expect Concurrency**: Any multi-user system will have concurrent access
2. **Use Optimistic Locking**: For most business entities with occasional conflicts
3. **Use Atomic Operations**: For high-contention fields (counters, stock)
4. **Handle Conflicts**: Implement retry logic for optimistic lock failures

**ไทย**:
1. **คาดหวัง Concurrency**: ระบบหลายผู้ใช้จะมีการเข้าถึงพร้อมกัน
2. **ใช้ Optimistic Locking**: สำหรับ business entities ส่วนใหญ่ที่มีความขัดแย้งเป็นครั้งคราว
3. **ใช้ Atomic Operations**: สำหรับฟิลด์ที่มีการแข่งขันสูง (ตัวนับ, สต็อก)
4. **จัดการความขัดแย้ง**: ใช้ retry logic สำหรับความล้มเหลวของ optimistic lock
