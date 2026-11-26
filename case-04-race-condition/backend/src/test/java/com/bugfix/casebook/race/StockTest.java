package com.bugfix.casebook.race;

import com.bugfix.casebook.race.after.StockAfterRepository;
import com.bugfix.casebook.race.after.StockAfterService;
import com.bugfix.casebook.race.after.StockItemAfter;
import com.bugfix.casebook.race.before.StockBeforeRepository;
import com.bugfix.casebook.race.before.StockItemBefore;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class StockTest {
    
    @Autowired
    private StockBeforeRepository beforeRepo;
    
    @Autowired
    private StockAfterService afterService;
    
    @Autowired
    private StockAfterRepository afterRepo;
    
    @Test
    void beforeVersion_BasicOperation() {
        StockItemBefore item = beforeRepo.save(new StockItemBefore("Laptop", 100));
        item.decrementStock(10);
        beforeRepo.save(item);
        
        StockItemBefore updated = beforeRepo.findById(item.getId()).orElseThrow();
        assertEquals(90, updated.getQuantity());
    }
    
    @Test
    void afterVersion_OptimisticLocking() {
        StockItemAfter item = afterRepo.save(new StockItemAfter("Laptop", 100));
        assertNotNull(item.getVersion());
        
        item.decrementStock(10);
        afterRepo.save(item);
        
        StockItemAfter updated = afterRepo.findById(item.getId()).orElseThrow();
        assertEquals(90, updated.getQuantity());
        assertTrue(updated.getVersion() > 0);
    }
    
    @Test
    void afterVersion_AtomicUpdate() {
        StockItemAfter item = afterRepo.save(new StockItemAfter("Laptop", 100));
        afterService.purchaseItemAtomic(item.getId(), 10);
        
        StockItemAfter updated = afterRepo.findById(item.getId()).orElseThrow();
        assertEquals(90, updated.getQuantity());
    }
}
