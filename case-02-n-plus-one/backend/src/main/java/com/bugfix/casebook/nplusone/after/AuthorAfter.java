package com.bugfix.casebook.nplusone.after;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * AFTER: Same entity structure, but fixed with repository-level fetch strategies
 * โครงสร้าง entity เหมือนเดิม แต่แก้ไขด้วยกลยุทธ์การ fetch ใน repository
 */
@Entity
@Table(name = "authors_after")
@NamedEntityGraph(
    name = "Author.books",
    attributeNodes = @NamedAttributeNode("books")
)
public class AuthorAfter {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;
    
    private String country;
    
    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<BookAfter> books = new ArrayList<>();
    
    public AuthorAfter() {}
    
    public AuthorAfter(String name, String country) {
        this.name = name;
        this.country = country;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getCountry() {
        return country;
    }
    
    public void setCountry(String country) {
        this.country = country;
    }
    
    public List<BookAfter> getBooks() {
        return books;
    }
    
    public void setBooks(List<BookAfter> books) {
        this.books = books;
    }
    
    public void addBook(BookAfter book) {
        books.add(book);
        book.setAuthor(this);
    }
}
