package com.artists_heaven.entities.artist;

import java.util.List;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;

import com.artists_heaven.admin.MonthlySalesDTO;
import com.artists_heaven.standardResponse.StandardResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/artists")
public class ArtistController {

        private final ArtistService artistService;

        private final ResourceLoader resourceLoader;

        public ArtistController(ArtistService artistService, ResourceLoader resourceLoader) {
                this.artistService = artistService;
                this.resourceLoader = resourceLoader;
        }

        @Operation(summary = "Get artist by ID", description = "Retrieve an artist along with their products and events for the current year.")
        @ApiResponse(responseCode = "200", description = "Artist found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class)))
        @ApiResponse(responseCode = "404", description = "Artist not found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class)))
        @GetMapping("/{artistId}")
        public ResponseEntity<StandardResponse<ArtistDTO>> getArtistById(@PathVariable final Long artistId) {
                ArtistDTO artistDTO = artistService.getArtistWithDetails(artistId);
                StandardResponse<ArtistDTO> response = new StandardResponse<>("Artist retrieved successfully",
                                artistDTO,
                                HttpStatus.OK.value());
                return ResponseEntity.ok(response);
        }

        @Operation(summary = "Register a new artist", description = "Registers a new artist with images, email, password, and personal details.")
        @ApiResponse(responseCode = "201", description = "Artist successfully registered", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class)))
        @ApiResponse(responseCode = "400", description = "Invalid request or artist already exists", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class)))
        @PostMapping(value = "/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        public ResponseEntity<StandardResponse<Artist>> registerArtist(@ModelAttribute ArtistRegisterDTO request,
                        @RequestParam(name = "lang", defaultValue = "en") String lang) {

                Artist registeredArtist = artistService.registerArtist(request, lang);

                StandardResponse<Artist> response = new StandardResponse<>("Artist registered successfully",
                                registeredArtist, HttpStatus.CREATED.value());
                return ResponseEntity.status(HttpStatus.CREATED).body(response);
        }

        @Operation(summary = "Get artist dashboard", description = "Retrieve the dashboard data for the authenticated artist for a specific year. "
                        +
                        "Includes verification status, past and future events, order item counts, and top countries sold.")
        @ApiResponse(responseCode = "200", description = "Dashboard retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class)))
        @ApiResponse(responseCode = "404", description = "Artist not found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class)))
        @ApiResponse(responseCode = "401", description = "Unauthorized, the user is not authenticated", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class)))
        @GetMapping("/dashboard")
        public ResponseEntity<StandardResponse<ArtistDashboardDTO>> getArtistDashboard(@RequestParam int year) {
                Artist artist = (Artist) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
                Long artistId = artist.getId();

                ArtistDashboardDTO dashboardDTO = artistService.getArtistDashboard(artistId, year);

                StandardResponse<ArtistDashboardDTO> response = new StandardResponse<>(
                                "Dashboard retrieved successfully",
                                dashboardDTO,
                                HttpStatus.OK.value());

                return ResponseEntity.ok(response);
        }

        @Operation(summary = "Get monthly sales data", description = "Retrieve the monthly sales data for the authenticated artist for a specific year, "
                        +
                        "excluding orders with status RETURN_ACCEPTED.")
        @ApiResponse(responseCode = "200", description = "Monthly sales data retrieved successfully", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = MonthlySalesDTO.class))))
        @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class)))
        @ApiResponse(responseCode = "401", description = "Unauthorized, user not authenticated", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class)))
        @ApiResponse(responseCode = "404", description = "Artist not found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class)))
        @GetMapping("/sales/monthly")
        public ResponseEntity<StandardResponse<List<MonthlySalesDTO>>> getMonthlySalesData(
                        @Parameter(description = "Year for which to retrieve monthly sales data", example = "2024", required = true) @RequestParam int year) {

                Artist artist = (Artist) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
                Long artistId = artist.getId();

                List<MonthlySalesDTO> monthlySales = artistService.getMonthlySalesDataPerArtist(artistId, year);

                StandardResponse<List<MonthlySalesDTO>> response = new StandardResponse<>(
                                "Monthly sales data retrieved successfully",
                                monthlySales,
                                HttpStatus.OK.value());

                return ResponseEntity.ok(response);
        }

        @Operation(summary = "Get main view of artists", description = "Retrieve a list of all valid artists for displaying in the main view.")
        @ApiResponse(responseCode = "200", description = "Artists retrieved successfully", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = ArtistMainViewDTO.class))))
        @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class)))
        @GetMapping("/main")
        public ResponseEntity<StandardResponse<List<ArtistMainViewDTO>>> getArtistMainView() {
                List<Artist> artists = artistService.getValidArtists();
                List<ArtistMainViewDTO> artistMainViewDTOs = artists.stream()
                                .map(this::mapToArtistMainViewDTO)
                                .toList();

                StandardResponse<List<ArtistMainViewDTO>> response = new StandardResponse<>(
                                "Artists retrieved successfully",
                                artistMainViewDTOs,
                                HttpStatus.OK.value());

                return ResponseEntity.ok(response);
        }

        private ArtistMainViewDTO mapToArtistMainViewDTO(Artist artist) {
                return new ArtistMainViewDTO(artist);
        }

        @GetMapping("/mainArtist_media/{fileName:.+}")
        @Operation(summary = "Get main artist image", description = "Retrieve a specific main image file for an artist by providing the file name, including its extension.")
        @ApiResponse(responseCode = "200", description = "Image retrieved successfully", content = @Content(mediaType = "image/jpeg"))
        @ApiResponse(responseCode = "400", description = "Invalid file name or request error", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class)))
        @ApiResponse(responseCode = "404", description = "Image not found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class)))
        public ResponseEntity<Resource> getArtistMainImage(
                        @Parameter(description = "File name including extension", required = true) @PathVariable String fileName) {

                try {
                        // 1️⃣ Intentar cargar desde classpath (recursos embebidos)
                        Resource resource = resourceLoader.getResource("classpath:mainArtist_media/" + fileName);
                        if (resource.exists() && resource.isReadable()) {
                                return ResponseEntity.ok()
                                                .contentType(MediaTypeFactory.getMediaType(fileName)
                                                                .orElse(MediaType.APPLICATION_OCTET_STREAM))
                                                .body(resource);
                        }

                        // 2️⃣ Intentar cargar desde filesystem (ruta física)
                        Path filePath = Paths.get(System.getProperty("user.dir"), "mainArtist_media", fileName)
                                        .normalize();
                        Resource fileResource = new UrlResource(filePath.toUri());
                        if (fileResource.exists() && fileResource.isReadable()) {
                                return ResponseEntity.ok()
                                                .contentType(MediaTypeFactory.getMediaType(fileName)
                                                                .orElse(MediaType.APPLICATION_OCTET_STREAM))
                                                .body(fileResource);
                        }

                        // 3️⃣ No se encontró en ninguna ruta
                        return ResponseEntity.notFound().build();

                } catch (Exception e) {

                        return ResponseEntity.internalServerError().build();
                }
        }

}
