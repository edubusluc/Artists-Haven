package com.artists_heaven.event;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.artists_heaven.entities.artist.Artist;
import com.artists_heaven.exception.AppExceptions;
import com.artists_heaven.images.ImageServingUtil;
import com.artists_heaven.page.PageResponse;
import com.artists_heaven.standardResponse.StandardResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import java.util.List;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.http.MediaType;
import org.springframework.data.domain.Page;

@RestController
@RequestMapping("/api/event")
public class EventController {

    private final EventService eventService;

    private static final String UPLOAD_DIR = "artists-heaven-backend/src/main/resources/event_media/";

    private final ResourceLoader resourceLoader;

    private final ImageServingUtil imageServingUtil;

    public EventController(EventService eventService, ResourceLoader resourceLoader, ImageServingUtil imageServingUtil) {
        this.eventService = eventService;
        this.resourceLoader = resourceLoader;
        this.imageServingUtil = imageServingUtil;
    }

    @Operation(summary = "Get all future events", description = "Returns a list of all future events. If there are no future events, returns 204 No Content.")

    @ApiResponse(responseCode = "200", description = "List of future events retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class), examples = @ExampleObject(value = "{\"message\": \"Future events retrieved successfully\", \"data\": [{\"id\": 1, \"name\": \"Event 1\"}], \"status\": 200}")))
    @ApiResponse(responseCode = "204", description = "No future events found", content = @Content)
    @ApiResponse(responseCode = "404", description = "Events not found", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"No events found\", \"data\": null, \"status\": 404}")))
    @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"Unexpected error occurred\", \"data\": null, \"status\": 500}")))
    @GetMapping("/allFutureEvents")
    public ResponseEntity<StandardResponse<List<EventDTO>>> getAllFutureEvents() {
        List<EventDTO> eventsDTO = eventService.findFutureEvents().stream()
                .map(EventDTO::new)
                .toList();

        if (eventsDTO.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT)
                    .body(new StandardResponse<>("No future events found", eventsDTO, HttpStatus.NO_CONTENT.value()));
        }

        return ResponseEntity
                .ok(new StandardResponse<>("Future events retrieved successfully", eventsDTO, HttpStatus.OK.value()));
    }

    @Operation(summary = "Get future events by artist", description = "Returns a list of all future events for a given artist ID. If there are no future events, returns 204 No Content.")
    @ApiResponse(responseCode = "200", description = "List of future events retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class), examples = @ExampleObject(value = "{\"message\": \"Future events retrieved successfully\", \"data\": [{\"id\": 1, \"name\": \"Event 1\"}], \"status\": 200}")))
    @ApiResponse(responseCode = "204", description = "No future events found for the artist", content = @Content)
    @ApiResponse(responseCode = "404", description = "Artist not found", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"Artist not found\", \"data\": null, \"status\": 404}")))
    @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"Unexpected error occurred\", \"data\": null, \"status\": 500}")))
    @GetMapping("/futureEvents/{id}")
    public ResponseEntity<StandardResponse<List<EventDTO>>> getFutureEventsByArtist(@PathVariable Long id) {
        List<EventDTO> eventsDTO = eventService.findFutureEventsByArtist(id).stream()
                .map(EventDTO::new)
                .toList();

        if (eventsDTO.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT)
                    .body(new StandardResponse<>("No future events found for the artist", eventsDTO,
                            HttpStatus.NO_CONTENT.value()));
        }

        return ResponseEntity
                .ok(new StandardResponse<>("Future events retrieved successfully", eventsDTO, HttpStatus.OK.value()));
    }

    @Operation(summary = "Check if authenticated artist is verified", description = "Returns true if the authenticated user is an artist and is verified, false otherwise.")
    @ApiResponse(responseCode = "200", description = "Verification status retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class), examples = @ExampleObject(value = "{\"message\": \"Verification status retrieved successfully\", \"data\": true, \"status\": 200}")))
    @ApiResponse(responseCode = "400", description = "Authenticated user is not an artist", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"User is not an artist\", \"data\": false, \"status\": 400}")))
    @ApiResponse(responseCode = "401", description = "User is not authenticated", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"User is not authenticated\", \"data\": null, \"status\": 401}")))
    @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"Unexpected error occurred\", \"data\": null, \"status\": 500}")))
    @GetMapping("/isVerified")
    public ResponseEntity<StandardResponse<Boolean>> isVerified() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new StandardResponse<>(
                            "User is not authenticated",
                            false,
                            HttpStatus.UNAUTHORIZED.value()));
        }

        Object principalUser = authentication.getPrincipal();

        if (principalUser instanceof Artist artist) {
            return ResponseEntity.ok(new StandardResponse<>(
                    "Verification status retrieved successfully",
                    artist.getIsVerificated(),
                    HttpStatus.OK.value()));
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new StandardResponse<>(
                            "User is not an artist",
                            false,
                            HttpStatus.FORBIDDEN.value()));
        }
    }

    @Operation(summary = "Create a new event", description = "Creates a new event associated with the authenticated artist and uploads an event image.")
    @ApiResponse(responseCode = "201", description = "Event created successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class), examples = @ExampleObject(value = "{\"message\": \"Event created successfully\", \"data\": {\"id\": 10, \"name\": \"New Event\"}, \"status\": 201}")))
    @ApiResponse(responseCode = "400", description = "Invalid data or user is not an artist", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"User is not an artist\", \"data\": null, \"status\": 400}")))
    @ApiResponse(responseCode = "401", description = "User is not authenticated", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"User is not authenticated\", \"data\": null, \"status\": 401}")))
    @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"Unexpected error occurred\", \"data\": null, \"status\": 500}")))
    @PostMapping("/new")
    public ResponseEntity<StandardResponse<Event>> newEvent(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Event data and event image", required = true, content = @Content(mediaType = "multipart/form-data")) @RequestPart("event") EventDTO eventDTO,
            @RequestPart("images") MultipartFile image,
            String lang) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AppExceptions.UnauthorizedActionException("User is not authenticated");
        }

        Object principalUser = authentication.getPrincipal();

        if (!(principalUser instanceof Artist artist)) {
            throw new AppExceptions.ForbiddenActionException("User is not an artist");
        }

        eventDTO.setArtistId(artist.getId());
        String imageUrls = imageServingUtil.saveImages(image, UPLOAD_DIR, "/event_media/", false);
        eventDTO.setImage(imageUrls);

        Event createdEvent = eventService.newEvent(eventDTO,lang);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new StandardResponse<>("Event created successfully", createdEvent, HttpStatus.CREATED.value()));
    }

    @Operation(summary = "Get paginated events created by the authenticated artist", description = "Retrieves a paginated list of events created by the currently authenticated artist.")
    @ApiResponse(responseCode = "200", description = "Paginated list of events retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class), examples = @ExampleObject(value = "{\"message\": \"Events retrieved successfully\", \"data\": {\"content\": [{\"id\": 1, \"name\": \"Event 1\"}], \"totalPages\": 5, \"currentPage\": 0, \"totalElements\": 30}, \"status\": 200}")))
    @ApiResponse(responseCode = "400", description = "User is not an artist", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"User is not an artist\", \"data\": null, \"status\": 400}")))
    @ApiResponse(responseCode = "401", description = "User is not authenticated", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"User is not authenticated\", \"data\": null, \"status\": 401}")))
    @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"Unexpected error occurred\", \"data\": null, \"status\": 500}")))
    @GetMapping("/allMyEvents")
    public ResponseEntity<StandardResponse<PageResponse<EventDTO>>> getAllMyEvents(
            @Parameter(description = "Page number to retrieve (0-based)", example = "0") @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Number of events per page", example = "6") @RequestParam(defaultValue = "6") int size) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AppExceptions.UnauthorizedActionException("User is not authenticated");
        }

        Object principalUser = authentication.getPrincipal();

        if (!(principalUser instanceof Artist artist)) {
            throw new AppExceptions.ForbiddenActionException("User is not an artist");
        }

        PageRequest pageRequest = PageRequest.of(page, size);
        Page<Event> eventsPage = eventService.getAllMyEvents(artist.getId(), pageRequest);

        PageResponse<EventDTO> pageResponse = new PageResponse<>(eventsPage.map(EventDTO::new));

        return ResponseEntity.ok(new StandardResponse<>(
                "Events retrieved successfully",
                pageResponse,
                HttpStatus.OK.value()));
    }

    @Operation(summary = "Delete an event by ID", description = "Deletes an event if it belongs to the authenticated artist and removes associated images.")
    @ApiResponse(responseCode = "200", description = "Event deleted successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class), examples = @ExampleObject(value = "{\"message\": \"Event deleted successfully\", \"data\": null, \"status\": 200}")))
    @ApiResponse(responseCode = "400", description = "User is not an artist", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"User is not an artist\", \"data\": null, \"status\": 400}")))
    @ApiResponse(responseCode = "401", description = "User is not authenticated", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"User is not authenticated\", \"data\": null, \"status\": 401}")))
    @ApiResponse(responseCode = "404", description = "Event not found or does not belong to the artist", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"Event not found or does not belong to you\", \"data\": null, \"status\": 404}")))
    @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"Unexpected error occurred\", \"data\": null, \"status\": 500}")))
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<StandardResponse<Void>> deleteEvent(
            @Parameter(description = "ID of the event to delete", example = "123") @PathVariable Long id) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AppExceptions.UnauthorizedActionException("User is not authenticated");
        }

        Object principalUser = authentication.getPrincipal();

        if (!(principalUser instanceof Artist artist)) {
            throw new AppExceptions.ForbiddenActionException("User is not an artist");
        }

        Event event = eventService.getEventById(id);

        if (event == null) {
            throw new AppExceptions.ResourceNotFoundException("Event not found");
        }

        if (!event.getArtist().getId().equals(artist.getId())) {
            throw new AppExceptions.ResourceNotFoundException("Event does not belong to you");
        }

        eventService.deleteImages(event.getImage());
        eventService.deleteEvent(id);

        return ResponseEntity.ok(new StandardResponse<>(
                "Event deleted successfully",
                null,
                HttpStatus.OK.value()));
    }

    @Operation(summary = "Get event details by ID", description = "Retrieves detailed information about an event specified by its ID.")
    @ApiResponse(responseCode = "200", description = "Event details retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class), examples = @ExampleObject(value = "{\"message\": \"Event retrieved successfully\", \"data\": {\"id\": 123, \"name\": \"Concert\"}, \"status\": 200}")))
    @ApiResponse(responseCode = "404", description = "Event not found", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"Event not found\", \"data\": null, \"status\": 404}")))
    @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"Unexpected error occurred\", \"data\": null, \"status\": 500}")))
    @GetMapping("/details/{id}")
    public ResponseEntity<StandardResponse<Event>> eventDetails(
            @Parameter(description = "ID of the event to retrieve", example = "123", required = true) @PathVariable Long id) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AppExceptions.UnauthorizedActionException("User is not authenticated");
        }

        Object principalUser = authentication.getPrincipal();

        if (!(principalUser instanceof Artist artist)) {
            throw new AppExceptions.ForbiddenActionException("User is not an artist");
        }

        Event event = eventService.getEventById(id);
        if (event == null) {
            throw new AppExceptions.ResourceNotFoundException("Event not found");
        }

        if (!event.getArtist().getId().equals(artist.getId())) {
            throw new AppExceptions.ResourceNotFoundException("Event does not belong to you");
        }

        return ResponseEntity.ok(new StandardResponse<>(
                "Event retrieved successfully",
                event,
                HttpStatus.OK.value()));
    }

    @Operation(summary = "Update an existing event", description = "Updates the details of an existing event by its ID. "
            +
            "Only the artist who created the event can update it. " +
            "An optional new image can be provided to replace the current one.")
    @ApiResponse(responseCode = "200", description = "Event updated successfully")
    @ApiResponse(responseCode = "400", description = "Bad request, e.g. validation errors or user not an artist")
    @ApiResponse(responseCode = "404", description = "Event not found or does not belong to the authenticated artist")
    @PutMapping("/edit/{id}")
    public ResponseEntity<StandardResponse<String>> updateEvent(
            @Parameter(description = "ID of the event to update", required = true) @PathVariable Long id,
            @Parameter(description = "Event data to update", required = true, content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE)) @RequestPart("event") EventDTO eventDTO,
            @Parameter(description = "Optional new image file to replace the current event image") @RequestPart(value = "image", required = false) MultipartFile newImage,
            String lang) {

        // Getting the authenticated user (current logged-in user)
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principalUser = authentication.getPrincipal();

        // Checking if the authenticated user is an Artist
        if (!eventService.isArtist()) {
            throw new AppExceptions.UnauthorizedActionException("Only artists can update events");
        }

        Artist artist = (Artist) principalUser;

        // Fetching the event by its ID
        Event event = eventService.getEventById(id);
        if (event == null) {
            throw new AppExceptions.ResourceNotFoundException("Event not found with ID: " + id);
        }

        // Verifying that the event belongs to the authenticated artist
        if (!event.getArtist().getId().equals(artist.getId())) {
            throw new AppExceptions.ResourceNotFoundException("This event does not belong to you");
        }

        // If a new image is provided, delete the old image and save the new one
        if (newImage != null) {
            eventService.deleteImages(event.getImage()); // Deleting old image
            String imageUrl = imageServingUtil.saveImages(newImage, UPLOAD_DIR, "/event_media/", false); // Saving new
                                                                                                         // image
            eventDTO.setImage(imageUrl); // Setting the new image URL in eventDTO
        }

        // Updating the event with the provided data
        eventService.updateEvent(event, eventDTO,lang);

        // Returning a success message after the event is updated
        StandardResponse<String> response = new StandardResponse<>("Event updated successfully", HttpStatus.OK.value());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Retrieve event media by file name", description = "Returns the image file associated with an event by its file name. "
            +
            "The image is served with a PNG content type.")
    @ApiResponse(responseCode = "200", description = "Image retrieved successfully", content = @Content(mediaType = "image/png"))
    @ApiResponse(responseCode = "404", description = "Image file not found")
    @GetMapping("/event_media/{fileName:.+}")
    public ResponseEntity<Resource> getProductImage(
            @Parameter(description = "Name of the image file to retrieve", required = true, example = "event_image.png") @PathVariable String fileName) {
        
        try {
                        Resource resource = resourceLoader.getResource("classpath:event_media/" + fileName);
                        if (resource.exists()) {
                                return ResponseEntity.ok()
                                                .contentType(MediaType.IMAGE_PNG)
                                                .body(resource);
                        } else {
                                return ResponseEntity.notFound().build();
                        }
                } catch (Exception e) {
                        return ResponseEntity.internalServerError().build();
                }
    }
}
