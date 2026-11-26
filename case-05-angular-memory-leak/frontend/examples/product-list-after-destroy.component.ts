import { Component, OnInit, inject, DestroyRef } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

interface Product {
  id: number;
  name: string;
  price: number;
}

/**
 * AFTER: Using takeUntilDestroyed
 * ใช้ takeUntilDestroyed
 */
@Component({
  selector: 'app-product-list-after-destroy',
  template: `
    <div class="container">
      <h2>Product List (AFTER - with takeUntilDestroyed)</h2>
      <p>Subscription cleaned up automatically</p>
      
      <div *ngFor="let product of products" class="product-card">
        <h3>{{ product.name }}</h3>
        <p>Price: {{ product.price | currency }}</p>
      </div>
    </div>
  `,
  standalone: true
})
export class ProductListAfterDestroyComponent implements OnInit {
  products: Product[] = [];
  
  // ✅ FIX: Inject DestroyRef for automatic cleanup
  private destroyRef = inject(DestroyRef);
  private http = inject(HttpClient);
  
  ngOnInit() {
    // ✅ FIX: Use takeUntilDestroyed to auto-unsubscribe
    this.http.get<Product[]>('http://localhost:8085/api/products')
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(data => {
        this.products = data;
        console.log('AFTER (takeUntilDestroyed): HTTP call completed');
      });
    
    // ✅ FIX: Clean up event listener
    window.addEventListener('resize', this.onResize);
    this.destroyRef.onDestroy(() => {
      window.removeEventListener('resize', this.onResize);
      console.log('AFTER: Event listener cleaned up');
    });
  }
  
  private onResize = () => {
    console.log('Window resized');
  };
  
  // ✅ FIX: Automatic cleanup when component is destroyed
}
