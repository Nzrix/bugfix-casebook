package com.bugfix.casebook.pagination;

import com.bugfix.casebook.pagination.after.ProductAfterRepository;
import com.bugfix.casebook.pagination.before.ProductBeforeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.data.domain.Page;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PaginationTest {
    
    @Autowired
    private ProductBeforeRepository beforeRepo;
    
    @Autowired
    private ProductAfterRepository afterRepo;
    
    @BeforeEach
    void setUp() {
        // Create 1000 test products
        for (int i = 1; i <= 1000; i++) {
            Product product = new Product(
                    "Product " + i,
                    "Category " + (i % 10),
                    BigDecimal.valueOf(10 + i),
                    100 + i
            );
            beforeRepo.save(product);
        }
    }
    
    @Test
    void beforeVersion_LoadsAllProducts() {
        System.out.println("=== BEFORE: Loading all products ===");
        
        long startTime = System.currentTimeMillis();
        
        // ❌ Loads ALL 1000 products
        List<Product> products = beforeRepo.findAll();
        
        long duration = System.currentTimeMillis() - startTime;
        
        System.out.println("Products loaded: " + products.size());
        System.out.println("Time taken: " + duration + "ms");
        
        assertEquals(1000, products.size());
        System.out.println("=========================================");
    }
    
    @Test
    void afterVersion_LoadsOnlyRequestedPage() {
        System.out.println("=== AFTER: Loading paginated products ===");
        
        long startTime = System.currentTimeMillis();
        
        // ✅ Loads only 20 products (page 0, size 20)
        Page<Product> page = afterRepo.findAll(PageRequest.of(0, 20));
        
        long duration = System.currentTimeMillis() - startTime;
        
        System.out.println("Products on page: " + page.getNumberOfElements());
        System.out.println("Total pages: " + page.getTotalPages());
        System.out.println("Total elements: " + page.getTotalElements());
        System.out.println("Time taken: " + duration + "ms");
        
        assertEquals(20, page.getNumberOfElements());
        assertEquals(1000, page.getTotalElements());
        assertEquals(50, page.getTotalPages()); // 1000/20 = 50
        
        System.out.println("=========================================");
    }
    
    @Test
    void pagination_NavigatesThroughPages() {
        // Page 0
        Page<Product> page0 = afterRepo.findAll(PageRequest.of(0, 20));
        assertEquals(20, page0.getContent().size());
        assertEquals("Product 1", page0.getContent().get(0).getName());
        
        // Page 1
        Page<Product> page1 = afterRepo.findAll(PageRequest.of(1, 20));
        assertEquals(20, page1.getContent().size());
        assertEquals("Product 21", page1.getContent().get(0).getName());
        
        // Last page
        Page<Product> lastPage = afterRepo.findAll(PageRequest.of(49, 20));
        assertEquals(20, lastPage.getContent().size());
        assertTrue(lastPage.isLast());
        
        System.out.println("✅ Successfully navigated through pages");
    }
    
    @Test
    void pagination_WithFiltering() {
        // Category 0 has products: 10, 20, 30, ... 1000 (100 products)
        Page<Product> page = afterRepo.findByCategory("Category 0", PageRequest.of(0, 10));
        
        assertEquals(10, page.getNumberOfElements());
        assertEquals(100, page.getTotalElements());
        
        System.out.println("✅ Pagination with filtering works");
    }
}
