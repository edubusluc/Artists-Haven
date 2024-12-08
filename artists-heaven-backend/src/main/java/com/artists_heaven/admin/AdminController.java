package com.artists_heaven.admin;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.artists_heaven.entities.artist.Artist;
import com.artists_heaven.entities.artist.ArtistRepository;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    ArtistRepository artistRepository;

    @PostMapping("/validate_artist")
    public ResponseEntity<?> validateArtist(@RequestBody Map<String, Long> payload) {
        Long artistId = payload.get("id");

        Artist artist = artistRepository.findById(artistId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Artista no encontrado"));

        artist.setIsvalid(true);
        artistRepository.save(artist);

        return ResponseEntity.ok(Map.of("message", "Artista verificado de forma correcta"));
        
    }
}
