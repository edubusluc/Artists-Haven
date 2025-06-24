package com.artists_heaven.event;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.util.StringUtils;
import com.artists_heaven.entities.artist.Artist;
import com.artists_heaven.entities.artist.ArtistRepository;

class EventServiceTest {

    private static final String UPLOAD_DIR = "artists-heaven-backend/src/main/resources/event_media/";

    @Mock
    private EventRepository eventRepository;

    @Mock
    private MultipartFile multipartFile;

    @Mock
    private ArtistRepository artistRepository;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private EventService eventService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        SecurityContextHolder.clearContext();
    }

    @Test
    void testGetAllEvents() {
        eventService.getAllEvents();
        verify(eventRepository, times(1)).findAll();
    }

    @Test
    void testValidateEventDateThrowsExceptionForNullDate() {
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

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            eventService.newEvent(eventDto);
        });

        assertEquals("Event date cannot be null", exception.getMessage());
    }

    @Test
    void testValidateEventDateThrowsExceptionForPastDate() {
        Artist artist = new Artist();
        artist.setId(1L);
        when(artistRepository.findById(anyLong())).thenReturn(Optional.of(artist));

        EventDTO eventDto = new EventDTO();
        eventDto.setDate(LocalDate.now().minusDays(1)); // Fecha en el pasado
        eventDto.setName("Test Event");
        eventDto.setDescription("Description");
        eventDto.setLocation("Location");
        eventDto.setMoreInfo("More Info");
        eventDto.setImage("Image");
        eventDto.setArtistId(1L);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
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
        artist.setIsVerificated(true);
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
        artist.setIsVerificated(true);
        when(artistRepository.findById(anyLong())).thenReturn(Optional.of(artist));
        when(eventRepository.save(any(Event.class))).thenReturn(new Event());

        Event event = eventService.newEvent(eventDto);
        assertNotNull(event);
        verify(eventRepository, times(1)).save(any(Event.class));
    }

    @Test
    void testSaveImagesSuccess() {
        String originalFilename = "test.jpg";
        String sanitizedFilename = "test.jpg"; // Assuming sanitizeFilename does not change the name
        String fileName = UUID.randomUUID().toString() + "_" + sanitizedFilename;
        Path targetPath = Paths.get(UPLOAD_DIR, fileName);

        MultipartFile newMultipartFile = new MockMultipartFile("file", originalFilename, "image/jpeg",
                new byte[] { 1, 2, 3, 4 });

        // Mock the static method Files.copy
        try (MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {
            mockedFiles.when(() -> Files.copy(any(InputStream.class), eq(targetPath))).thenAnswer(invocation -> null);

            String imageUrl = eventService.saveImages(newMultipartFile);

            assertTrue(imageUrl.contains("/event_media/"));
            assertTrue(imageUrl.contains(sanitizedFilename));
        }
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
        int page = 0;
        int size = 6;
        PageRequest pageable = PageRequest.of(page, size);

        Event event = new Event();
        Page<Event> events = new PageImpl<>(List.of(event), pageable, 1);

        when(eventRepository.findByArtistId(1L, pageable)).thenReturn(events);

        Page<Event> result = eventService.getAllMyEvents(1L, pageable);
        assertEquals(1, result.getContent().size());
        verify(eventRepository, times(1)).findByArtistId(1L, pageable);
    }

    @Test
    void testDeleteImagesSuccess() {
        String removedImage = "event_media/test.jpg";
        String cleanedPath = StringUtils.cleanPath(removedImage);
        Path targetPath = Paths.get("artists-heaven-backend/src/main/resources", cleanedPath).normalize();

        // Mock the static method Files.delete
        try (MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {
            mockedFiles.when(() -> Files.delete(targetPath)).thenAnswer(invocation -> null);

            eventService.deleteImages(removedImage);

            mockedFiles.verify(() -> Files.delete(targetPath), times(1));
        }
    }

    @Test
    void testDeleteImagesThrowsException() {
        String removedImage = "event_media/test.jpg";
        String cleanedPath = StringUtils.cleanPath(removedImage);
        Path targetPath = Paths.get("artists-heaven-backend/src/main/resources", cleanedPath).normalize();

        // Mock the static method Files.delete to throw an IOException
        try (MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {
            mockedFiles.when(() -> Files.delete(targetPath)).thenThrow(new IOException("Test IOException"));

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
                eventService.deleteImages(removedImage);
            });

            assertEquals("Error while deleting the image.", exception.getMessage());
            mockedFiles.verify(() -> Files.delete(targetPath), times(1));
        }
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

    @Test
    void testUpdateEventSuccess() {
        Event event = new Event();
        event.setId(1L);
        event.setName("Original Event");
        event.setDescription("Original Description");
        event.setDate(LocalDate.now().plusDays(10));
        event.setLocation("Original Location");
        event.setMoreInfo("Original More Info");
        event.setImage("original_image.jpg");

        EventDTO eventDTO = new EventDTO();
        eventDTO.setName("Updated Event");
        eventDTO.setDescription("Updated Description");
        eventDTO.setDate(LocalDate.now().plusDays(20));
        eventDTO.setLocation("Updated Location");
        eventDTO.setMoreInfo("Updated More Info");
        eventDTO.setImage("updated_image.jpg");

        when(eventRepository.save(any(Event.class))).thenReturn(event);

        eventService.updateEvent(event, eventDTO);

        assertEquals("Updated Event", event.getName());
        assertEquals("Updated Description", event.getDescription());
        assertEquals(LocalDate.now().plusDays(20), event.getDate());
        assertEquals("Updated Location", event.getLocation());
        assertEquals("Updated More Info", event.getMoreInfo());
        assertEquals("updated_image.jpg", event.getImage());

        verify(eventRepository, times(1)).save(event);
    }

    @Test
    void testUpdateEventThrowsExceptionForPastDate() {
        Event event = new Event();
        event.setId(1L);
        event.setName("Original Event");
        event.setDescription("Original Description");
        event.setDate(LocalDate.now().plusDays(10));
        event.setLocation("Original Location");
        event.setMoreInfo("Original More Info");
        event.setImage("original_image.jpg");

        EventDTO eventDTO = new EventDTO();
        eventDTO.setName("Updated Event");
        eventDTO.setDescription("Updated Description");
        eventDTO.setDate(LocalDate.now().minusDays(1)); // Fecha en el pasado
        eventDTO.setLocation("Updated Location");
        eventDTO.setMoreInfo("Updated More Info");
        eventDTO.setImage("updated_image.jpg");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            eventService.updateEvent(event, eventDTO);
        });

        assertEquals("Event date cannot be in the past", exception.getMessage());
        verify(eventRepository, times(0)).save(event);
    }
}