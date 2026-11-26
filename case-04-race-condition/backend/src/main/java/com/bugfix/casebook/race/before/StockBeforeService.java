package com.bugfix.casebook.race.before;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StockBeforeService {

    private final StockBeforeRepository repository;

    public StockBeforeService(StockBeforeRepository repository) {
        this.repository = repository;
    }

    /**
     * ❌ BUG: Classic read-modify-write without locking.
     * Sleep widens the race window so lost updates are deterministic in tests.
     */
    @Transactional
    public void purchaseItem(Long itemId, int quantity) {
        StockItemBefore item = repository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Item not found"));

        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }

        item.decrementStock(quantity);
        repository.saveAndFlush(item);
    }
}
