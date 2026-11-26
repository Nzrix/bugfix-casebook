package com.bugfix.casebook.timezone.after;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookAfterRepository extends JpaRepository<BookAfter, Long> {
}
