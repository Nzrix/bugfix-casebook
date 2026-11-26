package com.bugfix.casebook.race.before;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StockBeforeService {
    
    private final StockBeforeRepository repository;
    
    public StockBeforeService(StockBeforeRepository repository) {
        this.repository = repository;
    }
    
    @Transactional
    public void purchaseItem(Long itemId, int quantity) {
        // ❌ BUG: Race condition possible here
        StockItemBefore item = repository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Item not found"));
        
        item.decrementStock(quantity);
        repository.save(item);
        // If two threads execute this simultaneously, one update may be lost
    }
}
