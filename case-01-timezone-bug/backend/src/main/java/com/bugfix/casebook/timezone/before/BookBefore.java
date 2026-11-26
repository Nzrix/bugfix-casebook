package com.bugfix.casebook.timezone.before;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * BEFORE: Using Instant causes timezone conversion issues
 * วันที่จะถูกแปลงเป็น UTC และเกิดการเลื่อนวันที่
 */
@Entity
@Table(name = "books_before")
public class BookBefore {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String title;
    
    private String author;
    
    @Column(name = "release_date")
    private Instant releaseDate;  // ❌ BUG: Instant causes UTC conversion
    
    public BookBefore() {}
    
    public BookBefore(String title, String author, Instant releaseDate) {
        this.title = title;
        this.author = author;
        this.releaseDate = releaseDate;
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
    
    public String getAuthor() {
        return author;
    }
    
    public void setAuthor(String author) {
        this.author = author;
    }
    
    public Instant getReleaseDate() {
        return releaseDate;
    }
    
    public void setReleaseDate(Instant releaseDate) {
        this.releaseDate = releaseDate;
    }
}
