package com.artists_heaven.entities.artist;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/artists")
public class ArtistController {

    private final ArtistService artistService;

    public ArtistController(ArtistService artistService) {
        this.artistService = artistService;
    }

    // Endpoint for registering a new artist.
    @PostMapping("/register")
    public ResponseEntity<Artist> registerArtist(@RequestBody Artist artist) {
        try {
            // Attempt to register the artist using the service
            Artist registeredArtist = artistService.registerArtist(artist);
            
            // Return a response with the registered artist and HTTP status 201 (Created)
            return new ResponseEntity<>(registeredArtist, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            // Return a response with the error message and HTTP status 400 (Bad Request)
            return ResponseEntity.badRequest().body(null);
        }
    }

}
