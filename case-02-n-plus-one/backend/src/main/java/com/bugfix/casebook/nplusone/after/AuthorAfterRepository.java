package com.bugfix.casebook.nplusone.after;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * AFTER: Repository with multiple optimization strategies
 * Repository พร้อมกลยุทธ์การ optimize หลายแบบ
 */
@Repository
public interface AuthorAfterRepository extends JpaRepository<AuthorAfter, Long> {
    
    // ✅ Solution 1: Fetch Join with JPQL
    @Query("SELECT DISTINCT a FROM AuthorAfter a LEFT JOIN FETCH a.books")
    List<AuthorAfter> findAllWithBooksFetchJoin();
    
    // ✅ Solution 2: Entity Graph (declarative)
    @EntityGraph(value = "Author.books")
    @Query("SELECT a FROM AuthorAfter a")
    List<AuthorAfter> findAllWithBooksEntityGraph();
    
    // ✅ Solution 3: Entity Graph with attribute paths (alternative)
    @EntityGraph(attributePaths = {"books"})
    List<AuthorAfter> findAll();
}
