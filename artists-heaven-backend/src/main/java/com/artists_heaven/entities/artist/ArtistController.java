package com.artists_heaven.entities.artist;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.MediaType;

import com.artists_heaven.admin.MonthlySalesDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/api/artists")
public class ArtistController {

    private final ArtistService artistService;

    public ArtistController(ArtistService artistService) {
        this.artistService = artistService;
    }

    @PostMapping(value = "/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Artist> registerArtist(@ModelAttribute ArtistRegisterDTO request) {

        try {
            String imageUrl = artistService.saveImages(request.getImage());
            Artist registeredArtist = new Artist();
            registeredArtist.setMainViewPhoto(imageUrl);

            registeredArtist = artistService.registerArtist(request, registeredArtist);
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

    @GetMapping("/main")
    public ResponseEntity<List<ArtistMainViewDTO>> getArtistMainView() {
        try {
            List<Artist> artists = artistService.getValidArtists();
            List<ArtistMainViewDTO> artistiMainViewDTO = artists.stream()
                    .map(this::mapToArtistMainViewDTO)
                    .toList();

            return ResponseEntity.ok(artistiMainViewDTO);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    private ArtistMainViewDTO mapToArtistMainViewDTO(Artist artist) {
        return new ArtistMainViewDTO(artist);
    }

    @GetMapping("/mainArtist_media/{fileName:.+}")
    @Operation(summary = "Retrieve artist main image by file name", description = "Returns the artist image file corresponding to the given file name. "
            +
            "Supports serving PNG images stored in the product_media directory.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Image file successfully retrieved", content = @Content(mediaType = "image/png")),
            @ApiResponse(responseCode = "404", description = "Image file not found", content = @Content)
    })
    public ResponseEntity<Resource> getProductImage(
            @Parameter(description = "Name of the image file to retrieve, including extension (e.g., 'artist.png')", required = true) @PathVariable String fileName) {

        String basePath = System.getProperty("user.dir")
                + "/artists-heaven-backend/src/main/resources/mainArtist_media/";
        Path filePath = Paths.get(basePath, fileName);
        Resource resource = new FileSystemResource(filePath.toFile());

        if (!resource.exists()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "image/png")
                .body(resource);
    }

}
