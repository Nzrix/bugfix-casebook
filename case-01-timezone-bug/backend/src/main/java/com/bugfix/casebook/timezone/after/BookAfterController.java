package com.bugfix.casebook.timezone.after;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/after/books")
public class BookAfterController {
    
    private final BookAfterRepository repository;
    
    public BookAfterController(BookAfterRepository repository) {
        this.repository = repository;
    }
    
    @PostMapping
    public ResponseEntity<BookAfter> createBook(@RequestBody BookAfterDTO dto) {
        BookAfter book = new BookAfter(dto.title(), dto.author(), dto.releaseDate());
        BookAfter saved = repository.save(book);
        return ResponseEntity.ok(saved);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<BookAfter> getBook(@PathVariable Long id) {
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping
    public ResponseEntity<List<BookAfter>> getAllBooks() {
        return ResponseEntity.ok(repository.findAll());
    }
}

record BookAfterDTO(String title, String author, LocalDate releaseDate) {}
