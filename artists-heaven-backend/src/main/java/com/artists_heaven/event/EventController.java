package com.artists_heaven.event;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.artists_heaven.entities.artist.Artist;
import com.artists_heaven.images.ImageServingUtil;
import com.artists_heaven.page.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
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

    private final ImageServingUtil imageServingUtil;

    private static final String ERROR_MESSAGE = "User is not an artist";

    public EventController(EventService eventService, ImageServingUtil imageServingUtil) {
        this.eventService = eventService;
        this.imageServingUtil = imageServingUtil;
    }

    @Operation(summary = "Retrieve all events", description = "Fetches a list of all events.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of events retrieved successfully", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = Event.class))))
    })
    @GetMapping("/allEvents")
    public List<Event> getAllEvents() {
        return eventService.getAllEvents();
    }

    @Operation(summary = "Create a new event", description = "Creates a new event associated with the authenticated artist and uploads an event image.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Event created successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Event.class))),
            @ApiResponse(responseCode = "400", description = "Bad request - invalid data or user is not an artist", content = @Content(mediaType = "application/json"))
    })
    @PostMapping("/new")
    public ResponseEntity<Event> newEvent(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Event data and event image", required = true, content = {
                    @Content(mediaType = "multipart/form-data")
            }) @RequestPart("event") EventDTO eventDTO,
            @RequestPart("images") MultipartFile image) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Object principalUser = authentication.getPrincipal();

            if (principalUser instanceof Artist) {
                Artist artist = (Artist) principalUser;
                eventDTO.setArtistId(artist.getId());

                String imageUrls = imageServingUtil.saveImages(image, UPLOAD_DIR, "/event_media/", false);
                eventDTO.setImage(imageUrls);

                Event newEvent = eventService.newEvent(eventDTO);
                return new ResponseEntity<>(newEvent, HttpStatus.CREATED);
            } else {
                throw new IllegalArgumentException("User is not an artist");
            }
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/allMyEvents")
    @Operation(summary = "Get paginated events created by the authenticated artist", description = "Retrieves a paginated list of events created by the currently authenticated artist.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Paginated list of events retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = PageResponse.class))),
            @ApiResponse(responseCode = "400", description = "Bad request - user is not an artist", content = @Content(mediaType = "application/json"))
    })
    public ResponseEntity<?> getAllMyEvents(
            @Parameter(description = "Page number to retrieve (0-based)", example = "0") @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Number of events per page", example = "6") @RequestParam(defaultValue = "6") int size) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principalUser = authentication.getPrincipal();

        if (eventService.isArtist()) {
            Artist artist = (Artist) principalUser;
            PageRequest pageRequest = PageRequest.of(page, size);
            Page<Event> eventsPage = eventService.getAllMyEvents(artist.getId(), pageRequest);
            return ResponseEntity.ok(new PageResponse<>(eventsPage));
        } else {
            return ResponseEntity.badRequest().body("User is not an artist");
        }
    }

    @Operation(summary = "Delete an event by ID", description = "Deletes an event if it belongs to the authenticated artist. Also deletes associated images.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Event deleted successfully"),
            @ApiResponse(responseCode = "400", description = "Bad request - user is not an artist or invalid request"),
            @ApiResponse(responseCode = "404", description = "Event not found or does not belong to the authenticated artist")
    })
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteEvent(
            @Parameter(description = "ID of the event to delete") @PathVariable Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principalUser = authentication.getPrincipal();

        if (!eventService.isArtist()) {
            return ResponseEntity.badRequest().body(ERROR_MESSAGE);
        }

        Artist artist = (Artist) principalUser;

        try {
            Event event = eventService.getEventById(id);

            if (!event.getArtist().getId().equals(artist.getId())) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("This event does not belong to you");
            }

            eventService.deleteImages(event.getImage());
            eventService.deleteEvent(id);

            return ResponseEntity.ok("Event deleted successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(summary = "Get event details by ID", description = "Retrieves detailed information about an event specified by its ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Event details retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Event.class))),
            @ApiResponse(responseCode = "404", description = "Event not found", content = @Content(mediaType = "application/json"))
    })
    @GetMapping("details/{id}")
    public ResponseEntity<Event> eventDetails(
            @Parameter(description = "ID of the event to retrieve", required = true) @PathVariable Long id) {
        try {
            Event event = eventService.getEventById(id);
            return ResponseEntity.ok(event);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Update an existing event", description = "Updates the details of an existing event by its ID. "
            +
            "Only the artist who created the event can update it. " +
            "An optional new image can be provided to replace the current one.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Event updated successfully"),
            @ApiResponse(responseCode = "400", description = "Bad request, e.g. validation errors or user not an artist"),
            @ApiResponse(responseCode = "404", description = "Event not found or does not belong to the authenticated artist")
    })
    @PutMapping("/edit/{id}")
    public ResponseEntity<String> updateEvent(
            @Parameter(description = "ID of the event to update", required = true) @PathVariable Long id,
            @Parameter(description = "Event data to update", required = true, content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE)) @RequestPart("event") EventDTO eventDTO,
            @Parameter(description = "Optional new image file to replace the current event image") @RequestPart(value = "image", required = false) MultipartFile newImage) { // Optional
                                                                                                                                                                             // new
                                                                                                                                                                             // image

        // Getting the authenticated user (current logged-in user)
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principalUser = authentication.getPrincipal();

        // Checking if the authenticated user is an Artist
        if (!eventService.isArtist()) {
            // Returning a bad request response if the user is not an artist
            return ResponseEntity.badRequest().body(ERROR_MESSAGE);
        }

        Artist artist = (Artist) principalUser;

        try {
            // Fetching the event by its ID
            Event event = eventService.getEventById(id);

            // Verifying that the event belongs to the authenticated artist
            if (!event.getArtist().getId().equals(artist.getId())) {
                // Returning a "not found" response if the event does not belong to the artist
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("This event does not belong to you");
            }

            // If a new image is provided, delete the old image and save the new one
            if (newImage != null) {
                eventService.deleteImages(event.getImage()); // Deleting old image
                String imageUrl = imageServingUtil.saveImages(newImage, UPLOAD_DIR, "/event_media/", false); // Saving new image
                eventDTO.setImage(imageUrl); // Setting the new image URL in eventDTO
            }

            // Updating the event with the provided data
            eventService.updateEvent(event, eventDTO);

            // Returning a success message after the event is updated
            return ResponseEntity.ok("Event updated successfully");
        } catch (IllegalArgumentException e) {
            // Returning a bad request response if there's an error
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(summary = "Retrieve event media by file name", description = "Returns the image file associated with an event by its file name. "
            +
            "The image is served with a PNG content type.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Image retrieved successfully", content = @Content(mediaType = "image/png")),
            @ApiResponse(responseCode = "404", description = "Image file not found")
    })
    @GetMapping("/event_media/{fileName:.+}")
    public ResponseEntity<Resource> getProductImage(
            @Parameter(description = "Name of the image file to retrieve", required = true, example = "event_image.png") @PathVariable String fileName) {
        // Constructing the base path to the directory where event media is stored
        String basePath = System.getProperty("user.dir") + "/artists-heaven-backend/src/main/resources/event_media/";

        // Resolving the full file path using the base path and file name
        Path filePath = Paths.get(basePath, fileName);

        // Creating a Resource object to represent the file
        Resource resource = new FileSystemResource(filePath.toFile());

        // Checking if the requested file exists
        if (!resource.exists()) {
            // Returning a "not found" response if the file does not exist
            return ResponseEntity.notFound().build();
        }

        // Returning the image as a response with the correct content type header
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "image/png") // Setting the content type to PNG
                .body(resource); // Returning the file content
    }
}
