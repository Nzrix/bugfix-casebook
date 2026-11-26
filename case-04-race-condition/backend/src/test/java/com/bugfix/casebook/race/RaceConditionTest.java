package com.bugfix.casebook.race;

import com.bugfix.casebook.race.after.StockAfterRepository;
import com.bugfix.casebook.race.after.StockAfterService;
import com.bugfix.casebook.race.after.StockItemAfter;
import com.bugfix.casebook.race.before.StockBeforeRepository;
import com.bugfix.casebook.race.before.StockBeforeService;
import com.bugfix.casebook.race.before.StockItemBefore;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests demonstrating real race conditions with concurrent threads
 */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
class RaceConditionTest {
    
    @Autowired
    private StockBeforeRepository beforeRepo;
    
    @Autowired
    private StockBeforeService beforeService;
    
    @Autowired
    private StockAfterRepository afterRepo;
    
    @Autowired
    private StockAfterService afterService;
    
    @Test
    void beforeVersion_LostUpdatesWithConcurrency() throws InterruptedException {
        System.out.println("=== BEFORE: Demonstrating Lost Updates ===");
        
        // Initial stock: 100
        StockItemBefore item = beforeRepo.save(new StockItemBefore("Laptop", 100));
        Long itemId = item.getId();
        
        // 20 threads, each decrements by 5
        // Expected final stock: 100 - (20 * 5) = 0
        int threadCount = 20;
        int decrementAmount = 5;
        int expectedFinalStock = 100 - (threadCount * decrementAmount);
        
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);
        
        // Launch all threads
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await(); // Wait for all threads to be ready
                    beforeService.purchaseItem(itemId, decrementAmount);
                } catch (Exception e) {
                    // Some may fail, that's part of the race condition
                } finally {
                    doneLatch.countDown();
                }
            });
        }
        
        // Start all threads simultaneously
        startLatch.countDown();
        
        // Wait for completion
        doneLatch.await(10, TimeUnit.SECONDS);
        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);
        
        // Check final stock
        StockItemBefore finalItem = beforeRepo.findById(itemId).orElseThrow();
        int actualStock = finalItem.getQuantity();
        
        System.out.println("Expected stock: " + expectedFinalStock);
        System.out.println("Actual stock: " + actualStock);
        System.out.println("Lost updates: " + (actualStock - expectedFinalStock));
        
        // ❌ BUG: Due to race conditions, some updates are lost
        assertNotEquals(expectedFinalStock, actualStock, 
                "BEFORE version should have lost updates (actual: " + actualStock + ", expected: " + expectedFinalStock + ")");
        assertTrue(actualStock > expectedFinalStock, 
                "Stock should be higher than expected due to lost updates");
        
        System.out.println("=========================================");
    }
    
    @Test
    void afterVersion_OptimisticLocking_NoLostUpdates() throws InterruptedException {
        System.out.println("=== AFTER: Optimistic Locking Prevents Lost Updates ===");
        
        // Initial stock: 100
        StockItemAfter item = afterRepo.save(new StockItemAfter("Laptop", 100));
        Long itemId = item.getId();
        
        int threadCount = 20;
        int decrementAmount = 5;
        int expectedFinalStock = 100 - (threadCount * decrementAmount);
        
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    // Service has retry logic for OptimisticLockException
                    afterService.purchaseItemWithOptimisticLock(itemId, decrementAmount);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    System.err.println("Purchase failed after retries: " + e.getMessage());
                } finally {
                    doneLatch.countDown();
                }
            });
        }
        
        startLatch.countDown();
        doneLatch.await(15, TimeUnit.SECONDS);
        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);
        
        StockItemAfter finalItem = afterRepo.findById(itemId).orElseThrow();
        int actualStock = finalItem.getQuantity();
        
        System.out.println("Successful purchases: " + successCount.get() + "/" + threadCount);
        System.out.println("Expected stock: " + expectedFinalStock);
        System.out.println("Actual stock: " + actualStock);
        
        // ✅ FIX: With optimistic locking and retry, all updates succeed
        assertEquals(expectedFinalStock, actualStock, 
                "AFTER version with optimistic locking should have no lost updates");
        assertEquals(threadCount, successCount.get(), 
                "All purchases should succeed with retry logic");
        
        System.out.println("=========================================");
    }
    
    @Test
    void afterVersion_AtomicUpdate_NoLostUpdates() throws InterruptedException {
        System.out.println("=== AFTER: Atomic SQL Update ===");
        
        StockItemAfter item = afterRepo.save(new StockItemAfter("Laptop", 100));
        Long itemId = item.getId();
        
        int threadCount = 20;
        int decrementAmount = 5;
        int expectedFinalStock = 100 - (threadCount * decrementAmount);
        
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    afterService.purchaseItemAtomic(itemId, decrementAmount);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    System.err.println("Atomic purchase failed: " + e.getMessage());
                } finally {
                    doneLatch.countDown();
                }
            });
        }
        
        startLatch.countDown();
        doneLatch.await(10, TimeUnit.SECONDS);
        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);
        
        StockItemAfter finalItem = afterRepo.findById(itemId).orElseThrow();
        int actualStock = finalItem.getQuantity();
        
        System.out.println("Successful purchases: " + successCount.get() + "/" + threadCount);
        System.out.println("Expected stock: " + expectedFinalStock);
        System.out.println("Actual stock: " + actualStock);
        
        // ✅ FIX: Atomic SQL ensures correctness
        assertEquals(expectedFinalStock, actualStock, 
                "Atomic update should prevent any lost updates");
        
        System.out.println("=========================================");
    }
}
