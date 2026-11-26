# Case 05: Angular Memory Leak / Duplicate HTTP Calls
# กรณีที่ 05: หน่วยความจำรั่วใน Angular / HTTP Calls ซ้ำซ้อน

## 🐛 Problem / ปัญหา

**English**: An Angular component that subscribes to Observables without unsubscribing causes memory leaks. HTTP calls may also execute multiple times if not properly managed with operators like `shareReplay`.

**ไทย**: Angular component ที่ subscribe Observables โดยไม่ unsubscribe ทำให้หน่วยความจำรั่ว การเรียก HTTP อาจเกิดขึ้นหลายครั้งถ้าไม่จัดการอย่างถูกต้องด้วย operators เช่น `shareReplay`

## 📋 Scenario / สถานการณ์

A product list component that fetches data from API. After navigating away and back multiple times, memory usage increases and multiple HTTP requests are fired unnecessarily.

Component แสดงรายการสินค้าที่ดึงข้อมูลจาก API หลังจากสลับหน้าไปมาหลายครั้ง การใช้หน่วยความจำเพิ่มขึ้นและมีการเรียก HTTP requests ที่ไม่จำเป็นหลายครั้ง

## 🔍 Root Cause / สาเหตุ

**English**:
1. **Unmanaged Subscriptions**: Subscriptions not cleaned up when component is destroyed
2. **Multiple Subscriptions**: Each `subscribe()` on the same Observable triggers a new HTTP call
3. **Event Listener Leaks**: DOM event listeners not removed

**ไทย**:
1. **Subscriptions ที่ไม่ได้จัดการ**: Subscriptions ไม่ถูกทำความสะอาดเมื่อ component ถูก destroy
2. **การ Subscribe หลายครั้ง**: แต่ละ `subscribe()` บน Observable เดียวกันทำให้เกิดการเรียก HTTP ใหม่
3. **Event Listener รั่ว**: DOM event listeners ไม่ถูกลบออก

## ✅ Solution / วิธีแก้

**English**:
Three approaches (all demonstrated):

1. **Async Pipe**: Let Angular manage subscription lifecycle automatically
2. **takeUntilDestroyed**: Automatically unsubscribe when component is destroyed
3. **shareReplay**: Share Observable execution among multiple subscribers

**ไทย**:
สามวิธีแก้ (แสดงทั้งหมด):

1. **Async Pipe**: ให้ Angular จัดการ lifecycle ของ subscription อัตโนมัติ
2. **takeUntilDestroyed**: Unsubscribe อัตโนมัติเมื่อ component ถูก destroy
3. **shareReplay**: แชร์การทำงานของ Observable กับผู้ subscribe หลายคน

## 🏃 How to Run / วิธีรัน

```bash
cd frontend
npm install
npm start

# Navigate to http://localhost:4200
# Open DevTools → Memory/Network tabs
# Navigate between pages to see the difference
```

## 🧪 Test Results / ผลการทดสอบ

**BEFORE**:
- ❌ Memory usage increases after each navigation
- ❌ Multiple HTTP calls for the same data
- ❌ Subscriptions remain active after component destruction

**AFTER**:
- ✅ Constant memory usage
- ✅ Single HTTP call, cached and shared
- ✅ Clean subscription management

## 📚 Key Lessons / บทเรียน

**English**:
1. **Prefer Async Pipe**: Simplest solution, no manual cleanup needed
2. **Use takeUntilDestroyed**: For imperative subscriptions that need cleanup
3. **Share Expensive Operations**: Use `shareReplay` for HTTP calls
4. **Monitor Memory**: Use Chrome DevTools to detect leaks

**ไทย**:
1. **ชอบ Async Pipe**: วิธีที่ง่ายที่สุด ไม่ต้องทำความสะอาดเอง
2. **ใช้ takeUntilDestroyed**: สำหรับ subscriptions แบบ imperative ที่ต้องการทำความสะอาด
3. **แชร์ Operations ที่หนัก**: ใช้ `shareReplay` สำหรับการเรียก HTTP
4. **ติดตามหน่วยความจำ**: ใช้ Chrome DevTools เพื่อตรวจจับการรั่ว

## 🔗 Code Examples

### BEFORE (Bad)

```typescript
export class ProductListBefore implements OnInit {
  products: Product[] = [];

  ngOnInit() {
    // ❌ BUG: Subscription never cleaned up
    this.http.get<Product[]>('/api/products')
      .subscribe(data => this.products = data);
  }
}
```

### AFTER (Good - Async Pipe)

```typescript
export class ProductListAfter implements OnInit {
  products$ = this.http.get<Product[]>('/api/products');
  
  // In template: *ngFor="let product of products$ | async"
  // ✅ FIX: Angular handles subscription lifecycle
}
```

### AFTER (Good - takeUntilDestroyed)

```typescript
export class ProductListAfter implements OnInit {
  private destroyRef = inject(DestroyRef);
  products: Product[] = [];

  ngOnInit() {
    this.http.get<Product[]>('/api/products')
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(data => this.products = data);
    // ✅ FIX: Automatically unsubscribes on destroy
  }
}
```

### AFTER (Good - shareReplay)

```typescript
@Injectable()
export class ProductService {
  products$ = this.http.get<Product[]>('/api/products')
    .pipe(shareReplay(1));
  // ✅ FIX: Multiple subscriptions share one HTTP call
}
```
