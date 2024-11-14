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

    @PostMapping("/register")
    public ResponseEntity<?> registerArtist(@RequestBody Artist artist) {
        try {
            Artist registeredArtist = artistService.registerArtist(artist);
            return new ResponseEntity<>(registeredArtist, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

}
