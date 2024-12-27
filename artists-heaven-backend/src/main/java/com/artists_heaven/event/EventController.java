package com.artists_heaven.event;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.artists_heaven.entities.artist.Artist;

import java.util.List;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.PostMapping;

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

}
