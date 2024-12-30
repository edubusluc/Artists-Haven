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
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/api/event")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping("/allEvents")
    public List<Event> getAllEvents() {
        return eventService.getAllEvents();
    }

    @PostMapping("/new")
    public ResponseEntity<Event> newEvent(
            @RequestPart("event") EventDTO eventDTO,
            @RequestPart("images") MultipartFile image) {

        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Object principalUser = authentication.getPrincipal();

            if (principalUser instanceof Artist) {
                Artist artist = (Artist) principalUser;
                eventDTO.setArtistId(artist.getId());

                String imageUrls = eventService.saveImages(image);
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
    public ResponseEntity<List<Event>> getAllMyEvents() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principalUser = authentication.getPrincipal();
        if (eventService.isArtist()) {
            Artist artist = (Artist) principalUser;
            List<Event> events = eventService.getAllMyEvents(artist.getId());
            return ResponseEntity.ok(events);
        } else {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteEvent(@PathVariable Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principalUser = authentication.getPrincipal();

        if (!eventService.isArtist()) {
            return ResponseEntity.badRequest().body("User is not an artist");
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

    @GetMapping("details/{id}")
    public ResponseEntity<?> eventDetails(@PathVariable Long id) {
        try {
            Event event = eventService.getEventById(id);
            return ResponseEntity.ok(event);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/edit/{id}")
    public ResponseEntity<String> updateEvent(@PathVariable Long id,
            @RequestPart("event") EventDTO eventDTO,
            @RequestPart(value = "image", required = false) MultipartFile newImage) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principalUser = authentication.getPrincipal();

        if (!eventService.isArtist()) {
            return ResponseEntity.badRequest().body("User is not an artist");
        }

        Artist artist = (Artist) principalUser;

        try {
            Event event = eventService.getEventById(id);

            if (!event.getArtist().getId().equals(artist.getId())) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("This event does not belong to you");
            }

            if (newImage != null) {
                eventService.deleteImages(event.getImage());
                String imageUrl = eventService.saveImages(newImage);
                eventDTO.setImage(imageUrl);
            }

            eventService.updateEvent(event, eventDTO);
            return ResponseEntity.ok("Event updated successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/event_media/{fileName:.+}")
    public ResponseEntity<Resource> getProductImage(@PathVariable String fileName) {
        String basePath = System.getProperty("user.dir") + "/artists-heaven-backend/src/main/resources/event_media/";
        Path filePath = Paths.get(basePath, fileName);
        Resource resource = new FileSystemResource(filePath.toFile());
        if (!resource.exists()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "image/png") // Cambia a image/jpeg si tus imágenes son jpeg
                .body(resource);
    }

}
