package com.artists_heaven.email;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmailSenderRepository extends JpaRepository<Email, Long> {

}
