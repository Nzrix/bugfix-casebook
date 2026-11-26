package com.bugfix.casebook.pagination.before;

import com.bugfix.casebook.pagination.Product;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/before/products")
public class ProductBeforeController {
    
    private final ProductBeforeRepository repository;
    
    public ProductBeforeController(ProductBeforeRepository repository) {
        this.repository = repository;
    }
    
    @GetMapping
    public List<Product> getAllProducts() {
        // ❌ BUG: Loads ALL products into memory!
        // With 10,000+ products, this is slow and memory-intensive
        return repository.findAll();
    }
}
