package com.bugfix.casebook.nplusone.after;

import java.util.List;

/**
 * DTO for projection - only fetch what's needed
 * DTO สำหรับ projection - ดึงเฉพาะที่จำเป็น
 */
public record AuthorDTO(
    Long id,
    String name,
    String country,
    List<BookDTO> books
) {
    public record BookDTO(Long id, String title, String isbn) {}
}
