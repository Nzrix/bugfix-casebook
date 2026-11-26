package com.bugfix.casebook.pagination.after;

import com.bugfix.casebook.pagination.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/after/products")
public class ProductAfterController {
    
    private final ProductAfterRepository repository;
    
    public ProductAfterController(ProductAfterRepository repository) {
        this.repository = repository;
    }
    
    @GetMapping
    public Page<Product> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy) {
        
        // ✅ FIX: Use Pageable to load only requested page
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));
        return repository.findAll(pageable);
    }
    
    @GetMapping("/by-category")
    public Page<Product> getByCategory(
            @RequestParam String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        return repository.findByCategory(category, pageable);
    }
}
