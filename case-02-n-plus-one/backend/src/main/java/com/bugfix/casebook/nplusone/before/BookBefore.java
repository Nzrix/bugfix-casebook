package com.bugfix.casebook.nplusone.before;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "books_before")
public class BookBefore {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String title;
    
    private String isbn;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    @JsonIgnore
    private AuthorBefore author;
    
    public BookBefore() {}
    
    public BookBefore(String title, String isbn) {
        this.title = title;
        this.isbn = isbn;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getIsbn() {
        return isbn;
    }
    
    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }
    
    public AuthorBefore getAuthor() {
        return author;
    }
    
    public void setAuthor(AuthorBefore author) {
        this.author = author;
    }
}
