import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { shareReplay } from 'rxjs/operators';

interface Product {
  id: number;
  name: string;
  price: number;
}

/**
 * AFTER: Service with shareReplay
 * Service พร้อม shareReplay
 */
@Injectable({ providedIn: 'root' })
export class ProductService {
  private http = inject(HttpClient);
  
  // ✅ FIX: Use shareReplay to cache and share result
  // Multiple subscriptions will share the same HTTP call
  products$: Observable<Product[]> = this.http
    .get<Product[]>('http://localhost:8085/api/products')
    .pipe(
      shareReplay({ bufferSize: 1, refCount: true })
    );
  
  // ✅ FIX: Only ONE HTTP call, shared among all subscribers
  // The result is cached and replayed to new subscribers
}

// Usage in component:
// constructor(private productService: ProductService) {}
// ngOnInit() {
//   this.products$ = this.productService.products$;
// }
