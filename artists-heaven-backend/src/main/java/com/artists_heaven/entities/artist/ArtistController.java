package com.artists_heaven.entities.artist;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@RestController
@RequestMapping("/api/artists")
public class ArtistController {

    private final ArtistService artistService;

    public ArtistController(ArtistService artistService) {
        this.artistService = artistService;
    }

    @Operation(summary = "Register a new artist", description = "Registers a new artist with the provided artist details.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Artist successfully registered", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Artist.class))),
            @ApiResponse(responseCode = "400", description = "Invalid artist data", content = @Content(mediaType = "application/json"))
    })
    @PostMapping("/register")
    public ResponseEntity<Artist> registerArtist(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Artist object containing the artist's details", required = true, content = @Content(mediaType = "application/json", schema = @Schema(implementation = Artist.class))) @RequestBody Artist artist) {
        try {
            Artist registeredArtist = artistService.registerArtist(artist);
            return new ResponseEntity<>(registeredArtist, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

}
