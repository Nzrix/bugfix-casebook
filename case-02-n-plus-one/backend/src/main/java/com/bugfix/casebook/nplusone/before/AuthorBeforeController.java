package com.bugfix.casebook.nplusone.before;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/before/authors")
public class AuthorBeforeController {
    
    private final AuthorBeforeRepository repository;
    
    public AuthorBeforeController(AuthorBeforeRepository repository) {
        this.repository = repository;
    }
    
    @GetMapping
    public ResponseEntity<List<AuthorBeforeDTO>> getAllAuthors() {
        // ❌ BUG: This will cause N+1 queries
        // 1 query to fetch authors
        // N queries to fetch books for each author (when getBooks() is called)
        List<AuthorBefore> authors = repository.findAll();
        
        List<AuthorBeforeDTO> dtos = authors.stream()
                .map(author -> new AuthorBeforeDTO(
                        author.getId(),
                        author.getName(),
                        author.getCountry(),
                        author.getBooks().size() // This triggers lazy loading!
                ))
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(dtos);
    }
}

record AuthorBeforeDTO(Long id, String name, String country, int bookCount) {}
