package com.bugfix.casebook.pagination.after;

import com.bugfix.casebook.pagination.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * AFTER: Repository with pagination support
 * Repository พร้อม pagination
 */
public interface ProductAfterRepository extends JpaRepository<Product, Long> {
    
    // ✅ FIX: Return Page<Product> with Pageable parameter
    Page<Product> findAll(Pageable pageable);
    
    // ✅ Can also filter with pagination
    Page<Product> findByCategory(String category, Pageable pageable);
}
