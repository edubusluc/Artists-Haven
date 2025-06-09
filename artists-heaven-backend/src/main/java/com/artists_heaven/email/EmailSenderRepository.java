package com.artists_heaven.email;

import java.util.List;
import java.util.Map;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface EmailSenderRepository extends JpaRepository<Email, Long> {

    @Query("SELECT e.type, COUNT(e) FROM Email e WHERE EXTRACT(YEAR FROM e.createdAt) = :year GROUP BY e.type")
    List<Object[]> countEmailsByType(@Param("year") int year);

}
