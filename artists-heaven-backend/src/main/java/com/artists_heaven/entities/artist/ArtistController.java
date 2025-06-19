package com.artists_heaven.entities.artist;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.artists_heaven.admin.MonthlySalesDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

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

    @GetMapping("/dashboard")
    public ResponseEntity<ArtistDashboardDTO> getArtistDashboard(@RequestParam int year) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Object principalUser = authentication.getPrincipal();
            Artist artist = (Artist) principalUser;
            Long id = artist.getId();

            ArtistDashboardDTO artistDashboardDTO = new ArtistDashboardDTO();
            artistDashboardDTO.setIsVerificated(artistService.isArtistVerificated(id));
            artistDashboardDTO.setFutureEvents(artistService.getFutureEvents(id, year));
            artistDashboardDTO.setPastEvents(artistService.getPastEvents(id, year));
            artistDashboardDTO.setOrderItemCount(artistService.getOrderItemCount(id, year));
            artistDashboardDTO.setMostCountrySold(artistService.getMostCountrySold(id, year));

            return ResponseEntity.ok(artistDashboardDTO);

        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }

    }

    @GetMapping("/sales/monthly")
    public ResponseEntity<List<MonthlySalesDTO>> getMonthlySalesData(
            @Parameter(description = "Year for which to retrieve monthly sales data", example = "2024", required = true) @RequestParam int year) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Object principalUser = authentication.getPrincipal();
            Artist artist = (Artist) principalUser;
            Long id = artist.getId();

            List<MonthlySalesDTO> monthlySalesData = artistService.getMonthlySalesDataPerArtist(id, year);
            return ResponseEntity.ok(monthlySalesData);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

}
