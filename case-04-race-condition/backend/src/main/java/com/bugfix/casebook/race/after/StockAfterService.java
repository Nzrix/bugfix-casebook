package com.bugfix.casebook.race.after;

import jakarta.persistence.OptimisticLockException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StockAfterService {
    
    private final StockAfterRepository repository;
    
    public StockAfterService(StockAfterRepository repository) {
        this.repository = repository;
    }
    
    // ✅ Solution 1: Optimistic locking with retry
    @Transactional
    public void purchaseItemWithOptimisticLock(Long itemId, int quantity) {
        int maxRetries = 3;
        int attempts = 0;
        
        while (attempts < maxRetries) {
            try {
                StockItemAfter item = repository.findById(itemId)
                        .orElseThrow(() -> new IllegalArgumentException("Item not found"));
                
                item.decrementStock(quantity);
                repository.save(item);
                return; // Success
                
            } catch (OptimisticLockException e) {
                attempts++;
                if (attempts >= maxRetries) {
                    throw new RuntimeException("Failed after " + maxRetries + " retries", e);
                }
                // Retry
            }
        }
    }
    
    // ✅ Solution 2: Atomic update
    @Transactional
    public void purchaseItemAtomic(Long itemId, int quantity) {
        int updated = repository.decrementStockAtomic(itemId, quantity);
        if (updated == 0) {
            throw new IllegalStateException("Insufficient stock or item not found");
        }
    }
}
