package com.artists_heaven.entities.artist;

import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;


@Repository
public interface ArtistRepository  extends JpaRepository<Artist, Long> {
    Boolean existsByArtistName(String artistName);

    Artist findByEmail(String email);
    
    
}
