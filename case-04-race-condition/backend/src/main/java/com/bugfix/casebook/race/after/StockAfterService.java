package com.bugfix.casebook.race.after;

import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

@Service
public class StockAfterService {

    private final StockAfterRepository repository;
    private final TransactionTemplate transactionTemplate;

    public StockAfterService(StockAfterRepository repository,
                             PlatformTransactionManager transactionManager) {
        this.repository = repository;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    /**
     * ✅ Solution 1: @Version optimistic locking with retry.
     * Each attempt runs in its own transaction so a failed flush can be retried.
     * ObjectOptimisticLockingFailureException is raised on version conflict.
     */
    public void purchaseItemWithOptimisticLock(Long itemId, int quantity) {
        int maxRetries = 50;
        for (int attempt = 0; attempt < maxRetries; attempt++) {
            try {
                transactionTemplate.executeWithoutResult(status -> {
                    StockItemAfter item = repository.findById(itemId)
                            .orElseThrow(() -> new IllegalArgumentException("Item not found"));
                    item.decrementStock(quantity);
                    repository.saveAndFlush(item);
                });
                return;
            } catch (ObjectOptimisticLockingFailureException e) {
                if (attempt == maxRetries - 1) {
                    throw e;
                }
            }
        }
    }

    /**
     * ✅ Solution 2: Atomic UPDATE ... WHERE quantity >= ?
     */
    @Transactional
    public void purchaseItemAtomic(Long itemId, int quantity) {
        int updated = repository.decrementStockAtomic(itemId, quantity);
        if (updated == 0) {
            throw new IllegalStateException("Insufficient stock or item not found");
        }
    }
}
