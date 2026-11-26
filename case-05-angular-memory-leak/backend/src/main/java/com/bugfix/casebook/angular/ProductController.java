package com.bugfix.casebook.angular;

import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    
    @GetMapping
    public List<Product> getAllProducts() throws InterruptedException {
        // Simulate slow API
        Thread.sleep(500);
        
        List<Product> products = new ArrayList<>();
        for (int i = 1; i <= 20; i++) {
            products.add(new Product((long) i, "Product " + i, 100.0 + i));
        }
        
        System.out.println("API called: getAllProducts");
        return products;
    }
}

record Product(Long id, String name, Double price) {}
