package com.artists_heaven.event;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.artists_heaven.entities.artist.Artist;
import com.artists_heaven.entities.artist.ArtistRepository;

@Service
public class EventService {

    private final EventRepository eventRepository;
    private final ArtistRepository artistRepository;

    private static final String UPLOAD_DIR = "artists-heaven-backend/src/main/resources/event_media/";

    public EventService(EventRepository eventRepository, ArtistRepository artistRepository) {
        this.eventRepository = eventRepository;
        this.artistRepository = artistRepository;
    }

    public Event getEventById(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Event not found"));
    }

    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }

    public void deleteEvent(Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Event not found"));

        eventRepository.delete(event);
    }

    public Boolean isArtist() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principalUser = authentication.getPrincipal();
        if (principalUser instanceof Artist) {
            return true;
        } else {
            return false;
        }
    }

    private void validateEventDate(LocalDate eventDate) {
        if (eventDate == null) {
            throw new IllegalArgumentException("Event date cannot be null");
        }

        if (eventDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Event date cannot be in the past");
        }
    }

    public Event newEvent(EventDTO eventDTO) {
        try {
            Artist artist = artistRepository.findById(eventDTO.getArtistId())
                    .orElseThrow(() -> new IllegalArgumentException("Artist not found"));

            validateEventDate(eventDTO.getDate());

            Event event = new Event();
            event.setName(eventDTO.getName());
            event.setDescription(eventDTO.getDescription());
            event.setDate(eventDTO.getDate());
            event.setLocation(eventDTO.getLocation());
            event.setMoreInfo(eventDTO.getMoreInfo());
            event.setImage(eventDTO.getImage());
            event.setArtist(artist);
            return eventRepository.save(event);
        } catch (Exception e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    public String saveImages(MultipartFile image)  {
        String imageUrl = "";
    
        // Validar y sanitizar el nombre del archivo original
        String originalFilename = image.getOriginalFilename();
        
        
        // Validar que el archivo no esté vacío
        if (originalFilename == null || originalFilename.isEmpty()) {
            throw new IllegalArgumentException("El nombre del archivo no es válido.");
        }

        originalFilename = sanitizeFilename(originalFilename);
    
        // Crear un nuevo nombre de archivo seguro
        String fileName = UUID.randomUUID().toString() + "_" + originalFilename;
        Path targetPath = Paths.get(UPLOAD_DIR, fileName);
    
        try {
            // Validar el contenido del archivo (puedes implementar validaciones adicionales si es necesario)
            if (image.isEmpty() || !isValidImage(image)) {
                throw new IllegalArgumentException("El archivo no es una imagen válida.");
            }
    
            // Guardar la imagen en el directorio
            Files.copy(image.getInputStream(), targetPath);
            
            // Agregar la URL o nombre del archivo a la lista (ajustado según la necesidad)
            imageUrl = "/event_media/" + fileName;
        } catch (IOException e) {
            throw new IllegalArgumentException("Error al guardar las imágenes.");
        }
    
        return imageUrl;
    }
    
    // Método para sanitizar el nombre del archivo
    private String sanitizeFilename(String filename) {
        return filename.replaceAll("[^a-zA-Z0-9\\._-]", "_");
    }
    
    // Método para validar el contenido del archivo
    private boolean isValidImage(MultipartFile image) {
        // Aquí puedes agregar validaciones adicionales según el tipo de archivo que esperas
        String contentType = image.getContentType();
        return contentType != null && (contentType.equals("image/jpeg") || contentType.equals("image/png"));
    }

    public List<Event> getAllMyEvents(Long id) {
        return eventRepository.findByArtistId(id);
    }

    public void deleteImages(String removedImage) {
        String fileName = StringUtils.cleanPath(removedImage);
        Path targetPath = Paths.get("artists-heaven-backend/src/main/resources", fileName).normalize();

        try {
            Files.delete(targetPath);
        } catch (IOException e) {
            throw new IllegalArgumentException("Error al eliminar las imágenes.");
        }
    }

    public void updateEvent(Event event, EventDTO eventDTO) {
        LocalDate actualDate = LocalDate.now();
        
        if (eventDTO.getDate().isBefore(actualDate)) {
            throw new IllegalArgumentException("Event date cannot be in the past");
        }
    
        event.setName(eventDTO.getName());
        event.setDescription(eventDTO.getDescription());
        event.setDate(eventDTO.getDate());
        event.setLocation(eventDTO.getLocation());
        event.setMoreInfo(eventDTO.getMoreInfo());
        event.setImage(eventDTO.getImage());
        
        eventRepository.save(event);
    }
    

}
