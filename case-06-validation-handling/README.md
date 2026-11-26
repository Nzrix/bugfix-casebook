# Case 06: Validation & Error Handling
# กรณีที่ 06: การตรวจสอบความถูกต้องและการจัดการ Error

## 🐛 Problem / ปัญหา

**English**: API returns generic 500 Internal Server Error for validation failures and business logic errors. Clients can't distinguish between server crashes and invalid input. Stack traces leak to production responses.

**ไทย**: API ส่งกลับ 500 Internal Server Error ทั่วไปสำหรับความผิดพลาดในการตรวจสอบความถูกต้องและ business logic ไคลเอนต์ไม่สามารถแยกแยะระหว่างเซิร์ฟเวอร์ล่มกับข้อมูลที่ไม่ถูกต้อง Stack traces รั่วไปยังการตอบกลับใน production

## 📋 Scenario / สถานการณ์

A user registration API that should return 400 Bad Request with clear error messages, but instead returns 500 errors with stack traces.

API สมัครสมาชิกที่ควรส่งกลับ 400 Bad Request พร้อมข้อความ error ที่ชัดเจน แต่กลับส่ง 500 errors พร้อม stack traces

## 🔍 Root Cause / สาเหตุ

**English**:
1. **No Input Validation**: Missing `@Valid` annotations
2. **Uncaught Exceptions**: Business logic throws exceptions that aren't caught
3. **No Exception Handler**: No `@ControllerAdvice` to transform exceptions
4. **Leaking Details**: Stack traces exposed to clients

**ไทย**:
1. **ไม่มีการตรวจสอบ Input**: ขาด annotations `@Valid`
2. **Exceptions ที่ไม่ถูก Catch**: Business logic โยน exceptions ที่ไม่ถูกจับ
3. **ไม่มี Exception Handler**: ไม่มี `@ControllerAdvice` เพื่อแปลง exceptions
4. **รั่วรายละเอียด**: Stack traces ถูกเปิดเผยให้กับไคลเอนต์

## ✅ Solution / วิธีแก้

**English**:
1. **Add Validation**: Use Bean Validation (`@NotNull`, `@Email`, `@Size`, etc.)
2. **Global Exception Handler**: Create `@ControllerAdvice` to handle all exceptions
3. **Proper HTTP Status**: Return 400 for client errors, 500 only for server errors
4. **Clean Error Response**: Return structured error messages without stack traces

**ไทย**:
1. **เพิ่ม Validation**: ใช้ Bean Validation (`@NotNull`, `@Email`, `@Size`, ฯลฯ)
2. **Global Exception Handler**: สร้าง `@ControllerAdvice` เพื่อจัดการ exceptions ทั้งหมด
3. **HTTP Status ที่ถูกต้อง**: ส่งกลับ 400 สำหรับ client errors, 500 เฉพาะ server errors
4. **Error Response ที่สะอาด**: ส่งกลับข้อความ error ที่มีโครงสร้างโดยไม่มี stack traces

## 🏃 How to Run / วิธีรัน

```bash
cd backend
mvn test

# BEFORE: Returns 500 with stack trace
curl -X POST http://localhost:8086/api/before/users \
  -H "Content-Type: application/json" \
  -d '{"email":"invalid"}'

# AFTER: Returns 400 with clear message
curl -X POST http://localhost:8086/api/after/users \
  -H "Content-Type: application/json" \
  -d '{"email":"invalid"}'
```

## 🧪 Test Results / ผลการทดสอบ

**BEFORE**:
```json
{
  "timestamp": "2024-01-15T10:00:00.000+00:00",
  "status": 500,
  "error": "Internal Server Error",
  "trace": "java.lang.IllegalArgumentException: Invalid email\n\tat com.example...",
  "path": "/api/before/users"
}
```

**AFTER**:
```json
{
  "timestamp": "2024-01-15T10:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "errors": {
    "email": "must be a well-formed email address",
    "name": "must not be blank"
  },
  "path": "/api/after/users"
}
```

## 📚 Key Lessons / บทเรียน

**English**:
1. **Validate Input**: Always validate at controller level with `@Valid`
2. **Use Proper Status Codes**: 400 for client errors, 500 for server crashes
3. **Hide Implementation**: Never expose stack traces in production
4. **Consistent Format**: Return errors in consistent structure
5. **Global Handler**: Use `@ControllerAdvice` for centralized error handling

**ไทย**:
1. **ตรวจสอบ Input**: ตรวจสอบที่ระดับ controller เสมอด้วย `@Valid`
2. **ใช้ Status Codes ที่ถูกต้อง**: 400 สำหรับ client errors, 500 สำหรับเซิร์ฟเวอร์ล่ม
3. **ซ่อนรายละเอียดการทำงาน**: อย่าเปิดเผย stack traces ใน production
4. **รูปแบบที่สอดคล้อง**: ส่งกลับ errors ในรูปแบบที่สอดคล้องกัน
5. **Global Handler**: ใช้ `@ControllerAdvice` สำหรับการจัดการ error แบบรวมศูนย์
