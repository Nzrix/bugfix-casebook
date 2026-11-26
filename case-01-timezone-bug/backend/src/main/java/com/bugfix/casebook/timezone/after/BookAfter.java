package com.bugfix.casebook.timezone.after;

import jakarta.persistence.*;
import java.time.LocalDate;

/**
 * AFTER: Using LocalDate avoids timezone conversion
 * LocalDate ไม่มีข้อมูล timezone จึงไม่เกิดการแปลง
 */
@Entity
@Table(name = "books_after")
public class BookAfter {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String title;
    
    private String author;
    
    @Column(name = "release_date")
    private LocalDate releaseDate;  // ✅ FIX: LocalDate for date-only fields
    
    public BookAfter() {}
    
    public BookAfter(String title, String author, LocalDate releaseDate) {
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
    
    public LocalDate getReleaseDate() {
        return releaseDate;
    }
    
    public void setReleaseDate(LocalDate releaseDate) {
        this.releaseDate = releaseDate;
    }
}
