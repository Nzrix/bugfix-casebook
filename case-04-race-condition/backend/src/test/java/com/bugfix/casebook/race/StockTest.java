package com.bugfix.casebook.race;

import com.bugfix.casebook.race.after.StockAfterRepository;
import com.bugfix.casebook.race.after.StockAfterService;
import com.bugfix.casebook.race.after.StockItemAfter;
import com.bugfix.casebook.race.before.StockBeforeRepository;
import com.bugfix.casebook.race.before.StockBeforeService;
import com.bugfix.casebook.race.before.StockItemBefore;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class StockTest {
    
    @Autowired
    private StockBeforeService beforeService;
    
    @Autowired
    private StockBeforeRepository beforeRepo;
    
    @Autowired
    private StockAfterService afterService;
    
    @Autowired
    private StockAfterRepository afterRepo;
    
    @Test
    void beforeVersion_LostUpdates() throws InterruptedException {
        System.out.println("=== BEFORE: Demonstrating Race Condition ===");
        
        // Create item with stock = 100
        StockItemBefore item = beforeRepo.save(new StockItemBefore("Laptop", 100));
        Long itemId = item.getId();
        
        // Simulate 10 concurrent purchases of 10 items each
        ExecutorService executor = Executors.newFixedThreadPool(10);
        
        for (int i = 0; i < 10; i++) {
            executor.submit(() -> {
                try {
                    beforeService.purchaseItem(itemId, 10);
                } catch (Exception e) {
                    // May fail
                }
            });
        }
        
        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);
        
        // Check final stock
        StockItemBefore finalItem = beforeRepo.findById(itemId).orElseThrow();
        System.out.println("Expected stock: 0");
        System.out.println("Actual stock: " + finalItem.getQuantity());
        
        // ❌ BUG: Stock might be > 0 due to lost updates
        // The test documents the bug - actual value is unpredictable
        System.out.println("=========================================");
    }
    
    @Test
    void afterVersion_OptimisticLocking() throws InterruptedException {
        System.out.println("=== AFTER: Using Optimistic Locking ===");
        
        // Create item with stock = 100
        StockItemAfter item = afterRepo.save(new StockItemAfter("Laptop", 100));
        Long itemId = item.getId();
        
        // Simulate 10 concurrent purchases of 10 items each
        ExecutorService executor = Executors.newFixedThreadPool(10);
        
        for (int i = 0; i < 10; i++) {
            executor.submit(() -> {
                try {
                    afterService.purchaseItemWithOptimisticLock(itemId, 10);
                } catch (Exception e) {
                    System.err.println("Purchase failed: " + e.getMessage());
                }
            });
        }
        
        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);
        
        // Check final stock
        StockItemAfter finalItem = afterRepo.findById(itemId).orElseThrow();
        System.out.println("Expected stock: 0");
        System.out.println("Actual stock: " + finalItem.getQuantity());
        
        // ✅ FIX: Stock should be exactly 0
        assertEquals(0, finalItem.getQuantity());
        System.out.println("=========================================");
    }
    
    @Test
    void afterVersion_AtomicUpdate() throws InterruptedException {
        System.out.println("=== AFTER: Using Atomic Update ===");
        
        // Create item with stock = 100
        StockItemAfter item = afterRepo.save(new StockItemAfter("Laptop", 100));
        Long itemId = item.getId();
        
        // Simulate 10 concurrent purchases of 10 items each
        ExecutorService executor = Executors.newFixedThreadPool(10);
        
        for (int i = 0; i < 10; i++) {
            executor.submit(() -> {
                try {
                    afterService.purchaseItemAtomic(itemId, 10);
                } catch (Exception e) {
                    System.err.println("Purchase failed: " + e.getMessage());
                }
            });
        }
        
        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);
        
        // Check final stock
        StockItemAfter finalItem = afterRepo.findById(itemId).orElseThrow();
        System.out.println("Expected stock: 0");
        System.out.println("Actual stock: " + finalItem.getQuantity());
        
        // ✅ FIX: Stock should be exactly 0
        assertEquals(0, finalItem.getQuantity());
        System.out.println("=========================================");
    }
}
