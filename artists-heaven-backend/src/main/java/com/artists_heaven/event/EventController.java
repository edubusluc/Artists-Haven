package com.artists_heaven.event;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.artists_heaven.entities.artist.Artist;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
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

@RestController
@RequestMapping("/api/event")
public class EventController {

    private final EventService eventService;

    private static final String ERROR_MESSAGE = "User is not an artist";

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    // Endpoint to retrieve all events
    @GetMapping("/allEvents")
    public List<Event> getAllEvents() {
        // Calling the service method to fetch all events from the database
        // and returning the list of events
        return eventService.getAllEvents();
    }

    // Endpoint to create a new event
    @PostMapping("/new")
    public ResponseEntity<Event> newEvent(
            @RequestPart("event") EventDTO eventDTO, // Event data from the request
            @RequestPart("images") MultipartFile image) { // Image for the event

        try {
            // Getting the authenticated user (current logged-in user)
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Object principalUser = authentication.getPrincipal();

            // Checking if the authenticated user is an Artist
            if (principalUser instanceof Artist) {
                Artist artist = (Artist) principalUser;
                // Setting the artist's ID to the event data
                eventDTO.setArtistId(artist.getId());

                // Saving the event image and getting the image URL
                String imageUrls = eventService.saveImages(image);
                eventDTO.setImage(imageUrls);

                // Creating a new event using the event data and returning the created event
                Event newEvent = eventService.newEvent(eventDTO);
                return new ResponseEntity<>(newEvent, HttpStatus.CREATED);
            } else {
                // Throwing an exception if the user is not an Artist
                throw new IllegalArgumentException(ERROR_MESSAGE);
            }

        } catch (IllegalArgumentException e) {
            // Returning a bad request response if there's an error
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    // Endpoint to retrieve all events created by the authenticated artist
    @GetMapping("/allMyEvents")
    public ResponseEntity<List<Event>> getAllMyEvents() {
        // Getting the authenticated user (current logged-in user)
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principalUser = authentication.getPrincipal();

        // Checking if the authenticated user is an Artist
        if (eventService.isArtist()) {
            Artist artist = (Artist) principalUser;
            // Fetching all events created by the authenticated artist using their ID
            List<Event> events = eventService.getAllMyEvents(artist.getId());
            // Returning the list of events with a successful response
            return ResponseEntity.ok(events);
        } else {
            // Returning a bad request response if the user is not an artist
            return ResponseEntity.badRequest().body(null);
        }
    }

    // Endpoint to delete an event by its ID
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteEvent(@PathVariable Long id) {
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

            // Deleting associated images for the event
            eventService.deleteImages(event.getImage());

            // Deleting the event from the database
            eventService.deleteEvent(id);

            // Returning a success message after the event is deleted
            return ResponseEntity.ok("Event deleted successfully");
        } catch (IllegalArgumentException e) {
            // Returning a bad request response if there is an error
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Endpoint to get the details of an event by its ID
    @GetMapping("details/{id}")
    public ResponseEntity<Event> eventDetails(@PathVariable Long id) {
        try {
            // Fetching the event details by its ID
            Event event = eventService.getEventById(id);

            // Returning the event details with a successful response
            return ResponseEntity.ok(event);
        } catch (IllegalArgumentException e) {
            // Returning a "not found" response if the event does not exist
            return ResponseEntity.notFound().build();
        }
    }

    // Endpoint to update an existing event by its ID
    @PutMapping("/edit/{id}")
    public ResponseEntity<String> updateEvent(@PathVariable Long id,
            @RequestPart("event") EventDTO eventDTO, // Event data to update
            @RequestPart(value = "image", required = false) MultipartFile newImage) { // Optional new image

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
                String imageUrl = eventService.saveImages(newImage); // Saving new image
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

    // Endpoint to retrieve an event media (image) by its file name
    @GetMapping("/event_media/{fileName:.+}")
    public ResponseEntity<Resource> getProductImage(@PathVariable String fileName) {
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
