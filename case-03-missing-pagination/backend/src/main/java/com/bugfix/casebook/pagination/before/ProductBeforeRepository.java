package com.bugfix.casebook.pagination.before;

import com.bugfix.casebook.pagination.Product;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * BEFORE: No pagination support
 * ไม่มี pagination
 */
public interface ProductBeforeRepository extends JpaRepository<Product, Long> {
    // ❌ Only findAll() available - loads everything!
}
