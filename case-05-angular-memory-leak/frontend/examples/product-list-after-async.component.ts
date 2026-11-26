import { Component } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

interface Product {
  id: number;
  name: string;
  price: number;
}

/**
 * AFTER: Using async pipe (recommended)
 * ใช้ async pipe (แนะนำ)
 */
@Component({
  selector: 'app-product-list-after-async',
  template: `
    <div class="container">
      <h2>Product List (AFTER - with async pipe)</h2>
      <p>Memory managed automatically by Angular</p>
      
      <div *ngFor="let product of products$ | async" class="product-card">
        <h3>{{ product.name }}</h3>
        <p>Price: {{ product.price | currency }}</p>
      </div>
    </div>
  `,
  standalone: true
})
export class ProductListAfterAsyncComponent {
  // ✅ FIX: Return Observable and use async pipe in template
  products$: Observable<Product[]>;
  
  constructor(private http: HttpClient) {
    this.products$ = this.http.get<Product[]>('http://localhost:8085/api/products');
    console.log('AFTER (async): Component initialized');
  }
  
  // ✅ FIX: No manual subscription, Angular handles cleanup automatically
  // When component is destroyed, async pipe unsubscribes
}
