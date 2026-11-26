package com.bugfix.casebook.race.after;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StockAfterRepository extends JpaRepository<StockItemAfter, Long> {
    
    // ✅ Solution 2: Atomic update query
    @Modifying
    @Query("UPDATE StockItemAfter s SET s.quantity = s.quantity - :amount WHERE s.id = :id AND s.quantity >= :amount")
    int decrementStockAtomic(@Param("id") Long id, @Param("amount") int amount);
}
