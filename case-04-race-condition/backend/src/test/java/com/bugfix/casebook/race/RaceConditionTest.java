package com.bugfix.casebook.race;

import com.bugfix.casebook.race.after.StockAfterRepository;
import com.bugfix.casebook.race.after.StockAfterService;
import com.bugfix.casebook.race.after.StockItemAfter;
import com.bugfix.casebook.race.before.StockBeforeRepository;
import com.bugfix.casebook.race.before.StockBeforeService;
import com.bugfix.casebook.race.before.StockItemBefore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Concurrency tests on H2: lost updates BEFORE, @Version + atomic AFTER.
 */
@SpringBootTest
class RaceConditionTest {

    private static final int THREAD_COUNT = 20;
    private static final int DECREMENT = 5;
    private static final int INITIAL_STOCK = 100;

    @Autowired
    private StockBeforeRepository beforeRepo;

    @Autowired
    private StockBeforeService beforeService;

    @Autowired
    private StockAfterRepository afterRepo;

    @Autowired
    private StockAfterService afterService;

    @BeforeEach
    void clean() {
        beforeRepo.deleteAll();
        afterRepo.deleteAll();
    }

    @Test
    void beforeVersion_LostUpdatesWithConcurrency() throws InterruptedException {
        System.out.println("=== BEFORE: Demonstrating Lost Updates ===");

        StockItemBefore item = beforeRepo.save(new StockItemBefore("Laptop", INITIAL_STOCK));
        Long itemId = item.getId();
        int expectedFinalStock = INITIAL_STOCK - (THREAD_COUNT * DECREMENT);

        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(THREAD_COUNT);

        for (int i = 0; i < THREAD_COUNT; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    beforeService.purchaseItem(itemId, DECREMENT);
                } catch (Exception ignored) {
                    // Race may cause failures; lost updates are the main bug signal
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        assertTrue(doneLatch.await(30, TimeUnit.SECONDS));
        executor.shutdown();
        assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS));

        StockItemBefore finalItem = beforeRepo.findById(itemId).orElseThrow();
        int actualStock = finalItem.getQuantity();

        System.out.println("Expected stock: " + expectedFinalStock);
        System.out.println("Actual stock: " + actualStock);
        System.out.println("Lost updates (extra stock): " + (actualStock - expectedFinalStock));

        assertNotEquals(expectedFinalStock, actualStock,
                "BEFORE should lose updates (actual=" + actualStock + ", expected=" + expectedFinalStock + ")");
        assertTrue(actualStock > expectedFinalStock,
                "Stock should be higher than expected due to lost updates, got " + actualStock);

        System.out.println("=========================================");
    }

    @Test
    void afterVersion_OptimisticLocking_NoLostUpdates() throws InterruptedException {
        System.out.println("=== AFTER: Optimistic Locking Prevents Lost Updates ===");

        StockItemAfter item = afterRepo.save(new StockItemAfter("Laptop", INITIAL_STOCK));
        Long itemId = item.getId();
        int expectedFinalStock = INITIAL_STOCK - (THREAD_COUNT * DECREMENT);

        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(THREAD_COUNT);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger optimisticFailuresSeen = new AtomicInteger(0);

        for (int i = 0; i < THREAD_COUNT; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    try {
                        afterService.purchaseItemWithOptimisticLock(itemId, DECREMENT);
                        successCount.incrementAndGet();
                    } catch (ObjectOptimisticLockingFailureException e) {
                        optimisticFailuresSeen.incrementAndGet();
                        throw e;
                    }
                } catch (Exception e) {
                    System.err.println("Purchase failed: " + e.getClass().getSimpleName() + " - " + e.getMessage());
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        assertTrue(doneLatch.await(60, TimeUnit.SECONDS));
        executor.shutdown();
        assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS));

        StockItemAfter finalItem = afterRepo.findById(itemId).orElseThrow();
        int actualStock = finalItem.getQuantity();

        System.out.println("Successful purchases: " + successCount.get() + "/" + THREAD_COUNT);
        System.out.println("Expected stock: " + expectedFinalStock);
        System.out.println("Actual stock: " + actualStock);

        assertEquals(expectedFinalStock, actualStock,
                "AFTER @Version + retry should leave exact expected stock");
        assertEquals(THREAD_COUNT, successCount.get(),
                "All purchases should succeed with retry");

        System.out.println("=========================================");
    }

    @Test
    void afterVersion_AtomicUpdate_NoLostUpdates() throws InterruptedException {
        System.out.println("=== AFTER: Atomic SQL Update ===");

        StockItemAfter item = afterRepo.save(new StockItemAfter("Laptop", INITIAL_STOCK));
        Long itemId = item.getId();
        int expectedFinalStock = INITIAL_STOCK - (THREAD_COUNT * DECREMENT);

        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(THREAD_COUNT);
        AtomicInteger successCount = new AtomicInteger(0);

        for (int i = 0; i < THREAD_COUNT; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    afterService.purchaseItemAtomic(itemId, DECREMENT);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    System.err.println("Atomic purchase failed: " + e.getMessage());
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        assertTrue(doneLatch.await(30, TimeUnit.SECONDS));
        executor.shutdown();
        assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS));

        StockItemAfter finalItem = afterRepo.findById(itemId).orElseThrow();
        int actualStock = finalItem.getQuantity();

        System.out.println("Successful purchases: " + successCount.get() + "/" + THREAD_COUNT);
        System.out.println("Expected stock: " + expectedFinalStock);
        System.out.println("Actual stock: " + actualStock);

        assertEquals(THREAD_COUNT, successCount.get());
        assertEquals(expectedFinalStock, actualStock,
                "Atomic UPDATE ... WHERE quantity >= ? should yield exact expected stock");

        System.out.println("=========================================");
    }
}
