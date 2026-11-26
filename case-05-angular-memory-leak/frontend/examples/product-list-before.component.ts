import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';

interface Product {
  id: number;
  name: string;
  price: number;
}

/**
 * BEFORE: Memory leak demonstration
 * การสาธิตปัญหาหน่วยความจำรั่ว
 */
@Component({
  selector: 'app-product-list-before',
  template: `
    <div class="container">
      <h2>Product List (BEFORE - with memory leak)</h2>
      <p>Navigate away and back to see memory leak</p>
      
      <div *ngFor="let product of products" class="product-card">
        <h3>{{ product.name }}</h3>
        <p>Price: {{ product.price | currency }}</p>
      </div>
    </div>
  `,
  standalone: true
})
export class ProductListBeforeComponent implements OnInit {
  products: Product[] = [];
  
  constructor(private http: HttpClient) {}
  
  ngOnInit() {
    // ❌ BUG 1: Subscription never cleaned up
    this.http.get<Product[]>('http://localhost:8085/api/products')
      .subscribe(data => {
        this.products = data;
        console.log('BEFORE: HTTP call completed');
      });
    
    // ❌ BUG 2: Multiple subscriptions = multiple HTTP calls
    this.http.get<Product[]>('http://localhost:8085/api/products')
      .subscribe(data => {
        console.log('BEFORE: Duplicate HTTP call!');
      });
    
    // ❌ BUG 3: Event listener not cleaned up
    window.addEventListener('resize', this.onResize);
  }
  
  private onResize = () => {
    console.log('Window resized');
  };
  
  // ❌ BUG: No cleanup! Subscription and event listener leak memory
  // When component is destroyed, the subscription and listener remain active
}
