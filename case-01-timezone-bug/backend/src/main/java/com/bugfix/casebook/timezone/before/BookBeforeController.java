package com.bugfix.casebook.timezone.before;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/before/books")
public class BookBeforeController {
    
    private final BookBeforeRepository repository;
    
    public BookBeforeController(BookBeforeRepository repository) {
        this.repository = repository;
    }
    
    @PostMapping
    public ResponseEntity<BookBefore> createBook(@RequestBody BookBeforeDTO dto) {
        BookBefore book = new BookBefore(dto.title(), dto.author(), dto.releaseDate());
        BookBefore saved = repository.save(book);
        return ResponseEntity.ok(saved);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<BookBefore> getBook(@PathVariable Long id) {
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping
    public ResponseEntity<List<BookBefore>> getAllBooks() {
        return ResponseEntity.ok(repository.findAll());
    }
}

record BookBeforeDTO(String title, String author, Instant releaseDate) {}
