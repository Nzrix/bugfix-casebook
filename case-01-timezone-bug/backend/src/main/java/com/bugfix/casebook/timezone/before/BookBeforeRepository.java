package com.bugfix.casebook.timezone.before;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookBeforeRepository extends JpaRepository<BookBefore, Long> {
}
