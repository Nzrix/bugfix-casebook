package com.bugfix.casebook.nplusone.after;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/after/authors")
public class AuthorAfterController {
    
    private final AuthorAfterRepository repository;
    
    public AuthorAfterController(AuthorAfterRepository repository) {
        this.repository = repository;
    }
    
    @GetMapping("/fetch-join")
    public ResponseEntity<List<AuthorDTO>> getAuthorsWithFetchJoin() {
        // ✅ Solution 1: Fetch Join - single query with JOIN FETCH
        List<AuthorAfter> authors = repository.findAllWithBooksFetchJoin();
        return ResponseEntity.ok(mapToDTO(authors));
    }
    
    @GetMapping("/entity-graph")
    public ResponseEntity<List<AuthorDTO>> getAuthorsWithEntityGraph() {
        // ✅ Solution 2: Entity Graph - declarative fetch strategy
        List<AuthorAfter> authors = repository.findAllWithBooksEntityGraph();
        return ResponseEntity.ok(mapToDTO(authors));
    }
    
    @GetMapping
    public ResponseEntity<List<AuthorDTO>> getAllAuthors() {
        // ✅ Solution 3: Override findAll with Entity Graph
        List<AuthorAfter> authors = repository.findAll();
        return ResponseEntity.ok(mapToDTO(authors));
    }
    
    private List<AuthorDTO> mapToDTO(List<AuthorAfter> authors) {
        return authors.stream()
                .map(author -> new AuthorDTO(
                        author.getId(),
                        author.getName(),
                        author.getCountry(),
                        author.getBooks().stream()
                                .map(book -> new AuthorDTO.BookDTO(
                                        book.getId(),
                                        book.getTitle(),
                                        book.getIsbn()
                                ))
                                .collect(Collectors.toList())
                ))
                .collect(Collectors.toList());
    }
}
