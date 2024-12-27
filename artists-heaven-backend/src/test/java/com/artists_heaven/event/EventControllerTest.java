package com.artists_heaven.event;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.hamcrest.Matchers.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.artists_heaven.entities.artist.Artist;



public class EventControllerTest {

    private MockMvc mockMvc;

    @Mock
    private EventService eventService;

    @InjectMocks
    private EventController eventController;

    @Mock
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(eventController).build();
    }

    @Test
    void testGetAllEvents() throws Exception {
        List<Event> events = new ArrayList<>();
        when(eventService.getAllEvents()).thenReturn(events);

        mockMvc.perform(get("/api/event/allEvents"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(eventService, times(1)).getAllEvents();
    }

    @Test
    void testNewEvent() throws Exception {
        EventDTO eventDTO = new EventDTO();
        eventDTO.setName("Test Event");
        eventDTO.setDescription("Test Description");
        eventDTO.setDate(LocalDate.parse("2024-12-27"));
        eventDTO.setLocation("Test Location");
        eventDTO.setMoreInfo("Test More Info");

        MockMultipartFile image = new MockMultipartFile("images", "test.jpg", MediaType.IMAGE_JPEG_VALUE, "test image content".getBytes());

        Artist artist = new Artist();
        artist.setId(1L);

        when(authentication.getPrincipal()).thenReturn(artist);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        when(eventService.saveImages(image)).thenReturn("/product_media/test.jpg");
        when(eventService.newEvent(any(EventDTO.class))).thenReturn(new Event());

        MockMultipartFile eventJson = new MockMultipartFile("event", "", "application/json", "{\"name\": \"Test Event\", \"description\": \"Test Description\", \"date\": \"2024-12-27\", \"location\": \"Test Location\", \"moreInfo\": \"Test More Info\"}".getBytes());

        mockMvc.perform(multipart("/api/event/new")
                .file(eventJson)
                .file(image))
                .andExpect(status().isCreated());

        verify(eventService, times(1)).saveImages(image);
        verify(eventService, times(1)).newEvent(any(EventDTO.class));
    }

    @Test
    void testNewEventUserNotArtist() throws Exception {
        EventDTO eventDTO = new EventDTO();
        eventDTO.setName("Test Event");
        eventDTO.setDescription("Test Description");
        eventDTO.setDate(LocalDate.parse("2024-12-27"));
        eventDTO.setLocation("Test Location");
        eventDTO.setMoreInfo("Test More Info");

        MockMultipartFile image = new MockMultipartFile("images", "test.jpg", MediaType.IMAGE_JPEG_VALUE, "test image content".getBytes());

        when(authentication.getPrincipal()).thenReturn(null);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        MockMultipartFile eventJson = new MockMultipartFile("event", "", "application/json", "{\"name\": \"Test Event\", \"description\": \"Test Description\", \"date\": \"2024-12-27\", \"location\": \"Test Location\", \"moreInfo\": \"Test More Info\"}".getBytes());

        mockMvc.perform(multipart("/api/event/new")
                .file(eventJson)
                .file(image))
                .andExpect(status().isBadRequest());

        verify(eventService, times(0)).saveImages(image);
        verify(eventService, times(0)).newEvent(any(EventDTO.class));
    }
}

