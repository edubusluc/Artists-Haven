package com.artists_heaven.event;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import com.artists_heaven.entities.artist.Artist;
import com.artists_heaven.entities.artist.ArtistRepository;

@Service
public class EventService {

    private final EventRepository eventRepository;
    private final ArtistRepository artistRepository;

    public EventService(EventRepository eventRepository, ArtistRepository artistRepository) {
        this.eventRepository = eventRepository;
        this.artistRepository = artistRepository;
    }

    /**
     * Retrieves an event by its ID.
     *
     * @param id the ID of the event to retrieve.
     * @return the event corresponding to the given ID.
     * @throws IllegalArgumentException if no event is found with the given ID.
     */
    public Event getEventById(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Event not found"));
    }

    /**
     * Retrieves all events from the repository.
     *
     * @return a list of all events.
     */
    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }

    /**
     * Deletes an event by its ID.
     *
     * @param id the ID of the event to delete.
     * @throws IllegalArgumentException if the event with the given ID is not found.
     */
    public void deleteEvent(Long id) {
        // Retrieve the event by ID, throw an exception if not found
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Event not found"));

        // Delete the retrieved event
        eventRepository.delete(event);
    }

    /**
     * Checks if the currently authenticated user is an artist.
     *
     * @return true if the authenticated user is an artist, false otherwise.
     */
    public Boolean isArtist() {
        // Retrieve the authentication object from the security context
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Get the principal (the authenticated user)
        Object principalUser = authentication.getPrincipal();

        // Check if the principal is an instance of Artist and return the result
        return principalUser instanceof Artist;
    }

    /**
     * Validates the event date to ensure it is not null and not in the past.
     *
     * @param eventDate the date of the event to be validated.
     * @throws IllegalArgumentException if the event date is null or in the past.
     */
    private void validateEventDate(LocalDate eventDate) {
        // Check if the event date is null
        if (eventDate == null) {
            throw new IllegalArgumentException("Event date cannot be null");
        }

        // Check if the event date is in the past
        if (eventDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Event date cannot be in the past");
        }
    }

    /**
     * Creates a new event based on the provided EventDTO and saves it in the
     * repository.
     *
     * @param eventDTO the DTO containing event details.
     * @return the newly created Event entity.
     * @throws IllegalArgumentException if the artist is not found, the event date
     *                                  is invalid, or any other error occurs.
     */
    public Event newEvent(EventDTO eventDTO) {
        try {
            // Fetch the artist associated with the event
            Artist artist = artistRepository.findById(eventDTO.getArtistId())
                    .orElseThrow(() -> new IllegalArgumentException("Artist not found"));

            // Validate the event date
            validateEventDate(eventDTO.getDate());

            // Validate the artist is verified
            if (!artist.getIsVerificated()) {
                throw new IllegalArgumentException("Artist is not verified");
            }

            // Map the EventDTO to an Event entity
            Event event = new Event();
            event.setName(eventDTO.getName());
            event.setDescription(eventDTO.getDescription());
            event.setDate(eventDTO.getDate());
            event.setLocation(eventDTO.getLocation());
            event.setMoreInfo(eventDTO.getMoreInfo());
            event.setImage(eventDTO.getImage());
            event.setArtist(artist);

            // Save and return the event
            return eventRepository.save(event);
        } catch (Exception e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    /**
     * Retrieves all events associated with a specific artist.
     *
     * @param id the ID of the artist whose events are to be retrieved.
     * @return a list of events linked to the specified artist.
     */
    public Page<Event> getAllMyEvents(Long id, Pageable pageable) {
        // Query the repository to find all events by the artist's ID
        return eventRepository.findByArtistId(id, pageable);
    }

    /**
     * Deletes the specified image from the storage directory.
     *
     * @param removedImage the path of the image to be deleted.
     * @throws IllegalArgumentException if an error occurs while deleting the image.
     */
    public void deleteImages(String removedImage) {
        // Clean the file path to prevent security issues
        String fileName = StringUtils.cleanPath(removedImage);

        // Create the target path to the file in the resources directory
        Path targetPath = Paths.get("artists-heaven-backend/src/main/resources", fileName).normalize();

        try {
            // Attempt to delete the file
            Files.delete(targetPath);
        } catch (IOException e) {
            // Throw an exception if the file cannot be deleted
            throw new IllegalArgumentException("Error while deleting the image.");
        }
    }

    /**
     * Updates an existing event with the provided event data.
     *
     * @param event    the existing event to be updated.
     * @param eventDTO the DTO containing the new event details.
     * @throws IllegalArgumentException if the event date is in the past.
     */
    public void updateEvent(Event event, EventDTO eventDTO) {
        // Get the current date to validate the event date
        LocalDate actualDate = LocalDate.now();

        // Check if the event date is in the past
        if (eventDTO.getDate().isBefore(actualDate)) {
            throw new IllegalArgumentException("Event date cannot be in the past");
        }

        // Update the event fields with the new values from eventDTO
        event.setName(eventDTO.getName());
        event.setDescription(eventDTO.getDescription());
        event.setDate(eventDTO.getDate());
        event.setLocation(eventDTO.getLocation());
        event.setMoreInfo(eventDTO.getMoreInfo());
        event.setImage(eventDTO.getImage());

        // Save the updated event to the repository
        eventRepository.save(event);
    }

    public List<Event> findEventThisYearByArtist(Long artistId) {
        LocalDate now = LocalDate.now();
        return eventRepository.findArtistEventThisYear(artistId, now.getYear());

    }

}
