package com.bugfix.casebook.race.after;

import jakarta.persistence.*;

/**
 * AFTER: Using optimistic locking with @Version
 * ใช้ optimistic locking ด้วย @Version
 */
@Entity
@Table(name = "stock_items_after")
public class StockItemAfter {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String productName;
    
    private Integer quantity;
    
    @Version // ✅ FIX: Optimistic locking version field
    private Long version;
    
    public StockItemAfter() {}
    
    public StockItemAfter(String productName, Integer quantity) {
        this.productName = productName;
        this.quantity = quantity;
    }
    
    public void decrementStock(int amount) {
        if (this.quantity < amount) {
            throw new IllegalStateException("Insufficient stock");
        }
        this.quantity -= amount;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
}
