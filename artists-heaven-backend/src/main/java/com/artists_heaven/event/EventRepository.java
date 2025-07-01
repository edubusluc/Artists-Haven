package com.artists_heaven.event;


import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EventRepository extends JpaRepository<Event, Long> {

    @Query("SELECT e FROM Event e WHERE e.artist.id = ?1")
    Page<Event> findByArtistId(Long id, Pageable pageable);

    @Query("SELECT e FROM Event e WHERE e.artist.id = :artistId AND YEAR(e.date) = :year")
    List<Event> findArtistEventThisYear(@Param("artistId") Long artistId, @Param("year") int year);

}
