package com.artists_heaven.event;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import com.artists_heaven.entities.artist.Artist;
import com.artists_heaven.entities.artist.ArtistRepository;
import com.artists_heaven.exception.AppExceptions;
import com.artists_heaven.exception.AppExceptions.BadRequestException;
import com.artists_heaven.exception.AppExceptions.InvalidInputException;
import com.artists_heaven.exception.AppExceptions.ResourceNotFoundException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

@Service
public class EventService {

    private final EventRepository eventRepository;
    private final ArtistRepository artistRepository;
    private final MessageSource messageSource;

    private final OkHttpClient httpClient = new OkHttpClient();

    public EventService(EventRepository eventRepository, ArtistRepository artistRepository,
            MessageSource messageSource) {
        this.eventRepository = eventRepository;
        this.artistRepository = artistRepository;
        this.messageSource = messageSource;
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
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));
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
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));

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
            throw new BadRequestException("Event date cannot be null");
        }

        // Check if the event date is in the past
        if (eventDate.isBefore(LocalDate.now())) {
            throw new BadRequestException("Event date cannot be in the past");
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
    public Event newEvent(EventDTO eventDTO, String lang) {
        // 1. Validar que el artista existe
        Artist artist = artistRepository.findById(eventDTO.getArtistId())
                .orElseThrow(() -> new AppExceptions.ResourceNotFoundException("Artist not found"));

        // 2. Validar la fecha del evento
        validateEventDate(eventDTO.getDate());

        // 3. Validar que el artista está verificado
        if (!artist.getIsVerificated()) {
            throw new AppExceptions.ForbiddenActionException("Artist is not verified to create events");
        }

        // 4. Mapear el DTO a la entidad Event
        Event event = new Event();
        event.setName(eventDTO.getName());
        event.setDescription(eventDTO.getDescription());
        event.setDate(eventDTO.getDate());
        event.setLocation(eventDTO.getLocation());
        event.setMoreInfo(eventDTO.getMoreInfo());
        event.setImage(eventDTO.getImage());
        event.setArtist(artist);

        // 5. Obtener coordenadas
        getCoordinatesFromEvent(event, lang);

        // 6. Guardar el evento
        return eventRepository.save(event);
    }

    private void getCoordinatesFromEvent(Event event, String lang) {
        String location = URLEncoder.encode(event.getLocation(), StandardCharsets.UTF_8);
        Request request = new Request.Builder()
                .url("https://nominatim.openstreetmap.org/search?format=json&q=" + location)
                .header("User-Agent", "ArtistsHeaven/1.0 (contacto@tuemail.com)") // Identificación válida
                .build();

        Locale locale = new Locale(lang);

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }

            // 1️⃣ Leer el cuerpo como String
            String json = response.body().string();

            // 2️⃣ Parsear la respuesta como lista de mapas
            Gson gson = new Gson();
            List<Map<String, Object>> list = gson.fromJson(json, new TypeToken<List<Map<String, Object>>>() {
            }.getType());

            // 3️⃣ Extraer latitud y longitud si hay resultados
            if (list != null && !list.isEmpty()) {
                double lat = Double.parseDouble(list.get(0).get("lat").toString());
                double lon = Double.parseDouble(list.get(0).get("lon").toString());
                event.setLatitude(lat);
                event.setLongitude(lon);
            } else {
                throw new InvalidInputException("No se pudo geocodificar la dirección");
            }

        } catch (Exception e) {
            String msg = messageSource.getMessage("coordinate.notFound", null, locale);
            throw new InvalidInputException(msg);
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
    public void deleteImages(String mediaUrl) {
        if (mediaUrl == null || mediaUrl.isEmpty())
            return;

        try {
            // Extraer el nombre del archivo
            String fileName = Paths.get(mediaUrl).getFileName().toString();

            // Determinar la carpeta física según el prefijo de la URL
            String folderName;
            if (mediaUrl.startsWith("/event_media/")) {
                folderName = "event_media";
            } else if (mediaUrl.startsWith("/mainArtist_media/")) {
                folderName = "mainArtist_media";
            } else if (mediaUrl.startsWith("/verification_media/")) {
                folderName = "verification_media";
            } else {
                throw new IllegalArgumentException("Unknown media folder for URL: " + mediaUrl);
            }

            // Construir path absoluto
            Path targetPath = Paths.get(System.getProperty("user.dir"), folderName, fileName).normalize();

            // Intentar eliminar el archivo si existe
            Files.deleteIfExists(targetPath);

        } catch (IOException e) {
            throw new IllegalArgumentException("Error while deleting the image.", e);
        }
    }

    /**
     * Updates an existing event with the provided event data.
     *
     * @param event    the existing event to be updated.
     * @param eventDTO the DTO containing the new event details.
     * @throws IllegalArgumentException if the event date is in the past.
     */
    public void updateEvent(Event event, EventDTO eventDTO, String lang) {
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

        getCoordinatesFromEvent(event, lang);

        // Save the updated event to the repository
        eventRepository.save(event);
    }

    /**
     * Finds all events for a given artist occurring in the current year.
     *
     * @param artistId the ID of the artist
     * @return a list of {@link Event} objects for the current year;
     *         returns an empty list if the artistId is null
     */
    public List<Event> findEventThisYearByArtist(Long artistId) {
        if (artistId == null) {
            return Collections.emptyList();
        }
        int year = LocalDate.now().getYear();
        return eventRepository.findArtistEventThisYear(artistId, year);
    }

    /**
     * Finds all future events for a given artist, including today and later.
     *
     * @param artistId the ID of the artist
     * @return a list of {@link Event} objects that are scheduled in the future
     */
    public List<Event> findFutureEventsByArtist(Long artistId) {
        return eventRepository.findFutureEventsByArtist(artistId, LocalDate.now());
    }

    /**
     * Finds all future events in the system, including today and later.
     *
     * @return a list of {@link Event} objects that are scheduled in the future
     */
    public List<Event> findFutureEvents() {
        return eventRepository.findFutureEvents(LocalDate.now());
    }

}
