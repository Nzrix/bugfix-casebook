package com.bugfix.casebook.nplusone.before;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * BEFORE: Default lazy loading causes N+1 queries
 * การโหลดแบบ lazy ตามค่าเริ่มต้นทำให้เกิด N+1 queries
 */
@Entity
@Table(name = "authors_before")
public class AuthorBefore {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;
    
    private String country;
    
    // ❌ BUG: Lazy loading causes additional query per author
    @OneToMany(mappedBy = "author", fetch = FetchType.LAZY)
    private List<BookBefore> books = new ArrayList<>();
    
    public AuthorBefore() {}
    
    public AuthorBefore(String name, String country) {
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
    
    public List<BookBefore> getBooks() {
        return books;
    }
    
    public void setBooks(List<BookBefore> books) {
        this.books = books;
    }
    
    public void addBook(BookBefore book) {
        books.add(book);
        book.setAuthor(this);
    }
}
