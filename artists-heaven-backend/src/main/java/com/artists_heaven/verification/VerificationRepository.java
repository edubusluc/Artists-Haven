package com.artists_heaven.verification;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VerificationRepository extends JpaRepository<Verification,Long> {

    List<Verification> findByArtistId(Long id);

}
