package com.artists_heaven.event;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.artists_heaven.entities.artist.Artist;
import com.artists_heaven.entities.artist.ArtistRepository;

class EventServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private ArtistRepository artistRepository;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private EventService eventService;

    private static final String UPLOAD_DIR = "artists-heaven-backend/src/main/resources/event_media/";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAllEvents() {
        eventService.getAllEvents();
        verify(eventRepository, times(1)).findAll();
    }

    @Test
    void testValidateEventDateThrowsExceptionForNullDate() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            Artist artist = new Artist();
            artist.setId(1L);
            when(artistRepository.findById(anyLong())).thenReturn(Optional.of(artist));

            EventDTO eventDto = new EventDTO();
            eventDto.setDate(null);
            eventDto.setName("Test Event");
            eventDto.setDescription("Description");
            eventDto.setLocation("Location");
            eventDto.setMoreInfo("More Info");
            eventDto.setImage("Image");
            eventDto.setArtistId(1L);
            eventService.newEvent(eventDto);
        });
        assertEquals("Event date cannot be null", exception.getMessage());
    }

    @Test
    void testValidateEventDateThrowsExceptionForPastDate() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {

            Artist artist = new Artist();
            artist.setId(1L);
            when(artistRepository.findById(anyLong())).thenReturn(Optional.of(artist));

            EventDTO eventDto = new EventDTO();
            eventDto.setDate(LocalDate.now().minusDays(1));
            eventDto.setName("Test Event");
            eventDto.setDescription("Description");
            eventDto.setLocation("Location");
            eventDto.setMoreInfo("More Info");
            eventDto.setImage("Image");
            eventDto.setArtistId(1L);
            eventService.newEvent(eventDto);

        });
        assertEquals("Event date cannot be in the past", exception.getMessage());
    }

    @Test
    void testNewEventThrowsExceptionForInvalidArtist() {

        when(artistRepository.findById(anyLong())).thenReturn(Optional.empty());

        EventDTO eventDto = new EventDTO();
        eventDto.setDate(LocalDate.now().minusDays(1));
        eventDto.setName("Test Event");
        eventDto.setDescription("Description");
        eventDto.setLocation("Location");
        eventDto.setMoreInfo("More Info");
        eventDto.setImage("Image");
        eventDto.setArtistId(null);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            eventService.newEvent(eventDto);
        });
        assertEquals("Artist not found", exception.getMessage());
    }

    @Test
    void testNewEventThrowsExceptionForInvalidData() {
        EventDTO eventDto = new EventDTO();
        eventDto.setDate(LocalDate.now().plusDays(1));
        eventDto.setName(null);
        eventDto.setDescription("Description");
        eventDto.setLocation("Location");
        eventDto.setMoreInfo("More Info");
        eventDto.setImage("Image");
        eventDto.setArtistId(1L);

        Artist artist = new Artist();
        when(artistRepository.findById(anyLong())).thenReturn(Optional.of(artist));
        when(eventRepository.save(any(Event.class))).thenThrow(new RuntimeException("Invalid data"));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            eventService.newEvent(eventDto);
        });
        assertEquals("Invalid data", exception.getMessage());
    }

    @Test
    void testNewEventSuccess() {
        EventDTO eventDto = new EventDTO();
        eventDto.setDate(LocalDate.now().plusDays(1));
        eventDto.setName("Event Test");
        eventDto.setDescription("Description");
        eventDto.setLocation("Location");
        eventDto.setMoreInfo("More Info");
        eventDto.setImage("Image");
        eventDto.setArtistId(1L);
        Artist artist = new Artist();
        when(artistRepository.findById(anyLong())).thenReturn(Optional.of(artist));
        when(eventRepository.save(any(Event.class))).thenReturn(new Event());

        Event event = eventService.newEvent(eventDto);
        assertNotNull(event);
        verify(eventRepository, times(1)).save(any(Event.class));
    }

    @Test
    void testSaveImagesThrowsExceptionForPathTraversal() {
        MockMultipartFile image = new MockMultipartFile("images", "../test.jpg",
                "image/jpeg", "test image content".getBytes());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            eventService.saveImages(image);
        });
        assertEquals("Entry is outside of the target directory",
                exception.getMessage());
    }

    @Test
    void testSaveImagesThrowsExceptionForIOException() throws IOException {
        MockMultipartFile image = new MockMultipartFile("images", "test.jpg", "image/jpeg",
                "test image content".getBytes());

        Path targetPath = Paths.get(UPLOAD_DIR, "test.jpg").normalize();
        Files.createDirectories(targetPath.getParent());
        Files.createFile(targetPath);
        targetPath.toFile().setReadOnly(); // Make the file read-only to cause an IOException

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            eventService.saveImages(image);
        });
        assertEquals("Error al guardar las imágenes.", exception.getMessage());

        // Clean up the dummy file
        targetPath.toFile().setWritable(true);
        Files.deleteIfExists(targetPath);
    }

    @Test
    void testSaveImagesSuccess() throws IOException {
        MockMultipartFile image = new MockMultipartFile("images", "test.jpg", "image/jpeg",
                "test image content".getBytes());

        Path targetPath = Paths.get(UPLOAD_DIR, "test.jpg").normalize();
        Files.createDirectories(targetPath.getParent());

        String imageUrl = eventService.saveImages(image);
        assertEquals("/event_media/test.jpg", imageUrl);

        // Clean up the dummy file
        Files.deleteIfExists(targetPath);
    }

    @Test
    void testDeleteEventSuccess() {
        Event event = new Event();
        event.setId(1L);

        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));

        eventService.deleteEvent(1L);

        verify(eventRepository, times(1)).findById(1L);
        verify(eventRepository, times(1)).delete(event);
    }

    @Test
    void testDeleteEventThrowsExceptionForNotFound() {
        when(eventRepository.findById(1L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            eventService.deleteEvent(1L);
        });

        assertEquals("Event not found", exception.getMessage());
        verify(eventRepository, times(1)).findById(1L);
        verify(eventRepository, times(0)).delete(any(Event.class));
    }

    @Test
    void testGetAllMyEvents() {
        List<Event> events = List.of(new Event(), new Event());
        when(eventRepository.findByArtistId(1L)).thenReturn(events);

        List<Event> result = eventService.getAllMyEvents(1L);

        assertEquals(2, result.size());
        verify(eventRepository, times(1)).findByArtistId(1L);
    }

    @Test
    void testDeleteImagesSuccess() throws IOException {
        String removedImage = "event_media/test.jpg";
        Path targetPath = Paths.get("artists-heaven-backend/src/main/resources", removedImage).normalize();
        Files.createDirectories(targetPath.getParent());
        Files.createFile(targetPath);

        eventService.deleteImages(removedImage);

        assertFalse(Files.exists(targetPath));
    }

    @Test
    void testDeleteImagesThrowsExceptionForPathTraversal() {
        String removedImage = "../test.jpg";

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            eventService.deleteImages(removedImage);
        });

        assertEquals("Entry is outside of the target directory", exception.getMessage());
    }

    @Test
    void testGetEventByIdSuccess() {
        Event event = new Event();
        event.setId(1L);

        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));

        Event result = eventService.getEventById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(eventRepository, times(1)).findById(1L);
    }

    @Test
    void testGetEventByIdThrowsExceptionForNotFound() {
        when(eventRepository.findById(1L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            eventService.getEventById(1L);
        });

        assertEquals("Event not found", exception.getMessage());
        verify(eventRepository, times(1)).findById(1L);
    }

    @Test
    void testIsArtistReturnsTrue() {
        Artist artist = new Artist();
        when(authentication.getPrincipal()).thenReturn(artist);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        Boolean result = eventService.isArtist();

        assertTrue(result);
    }

    @Test
    void testIsArtistReturnsFalse() {
        Object user = new Object();
        when(authentication.getPrincipal()).thenReturn(user);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        Boolean result = eventService.isArtist();

        assertFalse(result);
    }
}