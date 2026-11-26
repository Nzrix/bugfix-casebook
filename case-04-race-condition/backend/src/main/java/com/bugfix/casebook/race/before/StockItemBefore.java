package com.bugfix.casebook.race.before;

import jakarta.persistence.*;

/**
 * BEFORE: No concurrency control
 * ไม่มีการควบคุม concurrency
 */
@Entity
@Table(name = "stock_items_before")
public class StockItemBefore {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String productName;
    
    private Integer quantity; // ❌ BUG: No protection against concurrent updates
    
    public StockItemBefore() {}
    
    public StockItemBefore(String productName, Integer quantity) {
        this.productName = productName;
        this.quantity = quantity;
    }
    
    public void decrementStock(int amount) {
        // ❌ BUG: Read-modify-write without synchronization
        this.quantity -= amount;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
}
