package com.bugfix.casebook.nplusone.before;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * BEFORE: Simple repository with no optimization
 * Repository ธรรมดาโดยไม่มีการ optimize
 */
@Repository
public interface AuthorBeforeRepository extends JpaRepository<AuthorBefore, Long> {
    // ❌ findAll() will cause N+1 queries when accessing books
}
