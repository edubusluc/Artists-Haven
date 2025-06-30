package com.artists_heaven.event;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.hamcrest.Matchers.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.artists_heaven.entities.artist.Artist;
import com.artists_heaven.entities.user.User;
import com.artists_heaven.entities.user.UserRole;
import com.artists_heaven.images.ImageServingUtil;

class EventControllerTest {

        private MockMvc mockMvc;

        @Mock
        private EventService eventService;

        @InjectMocks
        private EventController eventController;

        @Mock
        private Authentication authentication;

        @Mock
        private ImageServingUtil imageServingUtil;

        private static final String UPLOAD_DIR = "artists-heaven-backend/src/main/resources/event_media/";

        @BeforeEach
        void setUp() {
                MockitoAnnotations.openMocks(this);
                mockMvc = MockMvcBuilders.standaloneSetup(eventController).build();
                SecurityContextHolder.clearContext();
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

                MockMultipartFile image = new MockMultipartFile("images", "test.jpg", MediaType.IMAGE_JPEG_VALUE,
                                "test image content".getBytes());

                Artist artist = new Artist();
                artist.setId(1L);

                when(authentication.getPrincipal()).thenReturn(artist);
                SecurityContextHolder.getContext().setAuthentication(authentication);

                when(imageServingUtil.saveImages(image, UPLOAD_DIR, "/event_media/", false))
                                .thenReturn("/product_media/test.jpg");
                when(eventService.newEvent(any(EventDTO.class))).thenReturn(new Event());

                MockMultipartFile eventJson = new MockMultipartFile("event", "", "application/json",
                                "{\"name\": \"Test Event\", \"description\": \"Test Description\", \"date\": \"2024-12-27\", \"location\": \"Test Location\", \"moreInfo\": \"Test More Info\"}"
                                                .getBytes());

                mockMvc.perform(multipart("/api/event/new")
                                .file(eventJson)
                                .file(image))
                                .andExpect(status().isCreated());

                verify(imageServingUtil, times(1)).saveImages(image, UPLOAD_DIR, "/event_media/", false);
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

                MockMultipartFile image = new MockMultipartFile("images", "test.jpg", MediaType.IMAGE_JPEG_VALUE,
                                "test image content".getBytes());

                when(authentication.getPrincipal()).thenReturn(null);
                SecurityContextHolder.getContext().setAuthentication(authentication);

                MockMultipartFile eventJson = new MockMultipartFile("event", "", "application/json",
                                "{\"name\": \"Test Event\", \"description\": \"Test Description\", \"date\": \"2024-12-27\", \"location\": \"Test Location\", \"moreInfo\": \"Test More Info\"}"
                                                .getBytes());

                mockMvc.perform(multipart("/api/event/new")
                                .file(eventJson)
                                .file(image))
                                .andExpect(status().isBadRequest());

                verify(imageServingUtil, times(0)).saveImages(image, UPLOAD_DIR, "/event_media/", false);
                verify(eventService, times(0)).newEvent(any(EventDTO.class));
                SecurityContextHolder.clearContext();
        }

        @Test
        void testGetAllMyEvents() throws Exception {
                Artist artist = new Artist();
                artist.setId(1L);

                Event event = new Event();

                int page = 0;
                int size = 6;
                PageRequest pageable = PageRequest.of(page, size);

                Page<Event> eventPage = new PageImpl<>(List.of(event), pageable, 1);

                when(authentication.getPrincipal()).thenReturn(artist);
                SecurityContextHolder.getContext().setAuthentication(authentication);
                when(eventService.isArtist()).thenReturn(true);
                when(eventService.getAllMyEvents(artist.getId(), pageable)).thenReturn(eventPage);

                mockMvc.perform(get("/api/event/allMyEvents"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content", hasSize(1)));

                verify(eventService, times(1)).isArtist();
                verify(eventService, times(1)).getAllMyEvents(artist.getId(), pageable);
        }

        @Test
        void testGetAllMyEventsNotArtist() throws Exception {
                Artist artist = new Artist();
                artist.setId(1L);

                int page = 0;
                int size = 6;
                PageRequest pageable = PageRequest.of(page, size);

                when(authentication.getPrincipal()).thenReturn(artist);
                SecurityContextHolder.getContext().setAuthentication(authentication);
                when(eventService.isArtist()).thenReturn(false);

                mockMvc.perform(get("/api/event/allMyEvents"))
                                .andExpect(status().isBadRequest());

                verify(eventService, times(1)).isArtist();
                verify(eventService, times(0)).getAllMyEvents(1L, pageable);
        }

        @Test
        void testDeleteEvent() throws Exception {
                Artist artist = new Artist();
                artist.setId(1L);

                Event event = new Event();
                event.setId(1L);
                event.setArtist(artist);

                when(authentication.getPrincipal()).thenReturn(artist);
                SecurityContextHolder.getContext().setAuthentication(authentication);
                when(eventService.isArtist()).thenReturn(true);
                when(eventService.getEventById(1L)).thenReturn(event);

                mockMvc.perform(delete("/api/event/delete/1"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$", is("Event deleted successfully")));

                verify(eventService, times(1)).isArtist();
                verify(eventService, times(1)).getEventById(1L);
                verify(eventService, times(1)).deleteEvent(1L);
        }

        @Test
        void testDeleteEventNotArtist() throws Exception {
                User user = new User();
                user.setId(1L);
                when(eventService.isArtist()).thenReturn(false);
                when(authentication.getPrincipal()).thenReturn(user);
                SecurityContextHolder.getContext().setAuthentication(authentication);

                mockMvc.perform(delete("/api/event/delete/1"))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$", is("User is not an artist")));

                verify(eventService, times(1)).isArtist();
                verify(eventService, times(0)).getEventById(anyLong());
                verify(eventService, times(0)).deleteEvent(anyLong());
        }

        @Test
        void testDeleteEventNotBelongToArtist() throws Exception {
                Artist artist = new Artist();
                artist.setId(1L);

                Artist anotherArtist = new Artist();
                anotherArtist.setId(2L);

                Event event = new Event();
                event.setId(1L);
                event.setArtist(anotherArtist);

                when(authentication.getPrincipal()).thenReturn(artist);
                SecurityContextHolder.getContext().setAuthentication(authentication);
                when(eventService.isArtist()).thenReturn(true);
                when(eventService.getEventById(1L)).thenReturn(event);

                mockMvc.perform(delete("/api/event/delete/1"))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$", is("This event does not belong to you")));

                verify(eventService, times(1)).isArtist();
                verify(eventService, times(1)).getEventById(1L);
                verify(eventService, times(0)).deleteEvent(1L);
        }

        @Test
        void testDeleteEventThrowsException() throws Exception {
                Artist artist = new Artist();
                artist.setId(1L);

                when(authentication.getPrincipal()).thenReturn(artist);
                SecurityContextHolder.getContext().setAuthentication(authentication);
                when(eventService.isArtist()).thenReturn(true);
                when(eventService.getEventById(1L)).thenThrow(new IllegalArgumentException("Event not found"));

                mockMvc.perform(delete("/api/event/delete/1"))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$", is("Event not found")));

                verify(eventService, times(1)).isArtist();
                verify(eventService, times(1)).getEventById(1L);
                verify(eventService, times(0)).deleteEvent(1L);
        }

        @Test
        void testEventDetailsSuccess() throws Exception {
                Event event = new Event();
                event.setId(1L);

                when(eventService.getEventById(1L)).thenReturn(event);

                mockMvc.perform(get("/api/event/details/1"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id", is(1)));

                verify(eventService, times(1)).getEventById(1L);
        }

        @Test
        void testEventDetailsNotFound() throws Exception {
                when(eventService.getEventById(1L)).thenThrow(new IllegalArgumentException("Event not found"));

                mockMvc.perform(get("/api/event/details/1"))
                                .andExpect(status().isNotFound());

                verify(eventService, times(1)).getEventById(1L);
        }

        @Test
        void testUpdateEventSuccess() throws Exception {
                Artist artist = new Artist();
                artist.setId(1L);

                Event event = new Event();
                event.setId(1L);
                event.setArtist(artist);

                EventDTO eventDTO = new EventDTO();
                eventDTO.setName("Updated Event");
                eventDTO.setDescription("Updated Description");

                MockMultipartFile eventJson = new MockMultipartFile("event", "", "application/json",
                                "{\"name\": \"Updated Event\", \"description\": \"Updated Description\"}".getBytes());
                MockMultipartFile image = new MockMultipartFile("image", "test.jpg", "image/jpeg",
                                "test image content".getBytes());

                when(authentication.getPrincipal()).thenReturn(artist);
                SecurityContextHolder.getContext().setAuthentication(authentication);
                when(eventService.isArtist()).thenReturn(true);
                when(eventService.getEventById(1L)).thenReturn(event);

                mockMvc.perform(MockMvcRequestBuilders.multipart("/api/event/edit/{id}", event.getId())
                                .file(eventJson)
                                .file(image)
                                .with(request -> {
                                        request.setMethod("PUT");
                                        return request;
                                }))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$", is("Event updated successfully")));

                verify(eventService, times(1)).isArtist();
                verify(eventService, times(1)).getEventById(1L);
                verify(imageServingUtil, times(1)).saveImages(image, UPLOAD_DIR, "/event_media/", false);
                verify(eventService, times(1)).updateEvent(any(Event.class), any(EventDTO.class));
        }

        @Test
        void testUpdateEventNotArtist() throws Exception {
                EventDTO eventDTO = new EventDTO();
                eventDTO.setName("Updated Event");
                eventDTO.setDescription("Updated Description");

                User user = new User();
                user.setId(1L);
                user.setRole(UserRole.USER);

                MockMultipartFile eventJson = new MockMultipartFile("event", "", "application/json",
                                "{\"name\": \"Updated Event\", \"description\": \"Updated Description\"}".getBytes());

                when(eventService.isArtist()).thenReturn(false);

                Authentication authentication = mock(Authentication.class);
                when(authentication.getPrincipal()).thenReturn(user);
                when(authentication.isAuthenticated()).thenReturn(true);
                SecurityContext securityContext = mock(SecurityContext.class);
                when(securityContext.getAuthentication()).thenReturn(authentication);
                SecurityContextHolder.setContext(securityContext);

                mockMvc.perform(MockMvcRequestBuilders.multipart("/api/event/edit/1")
                                .file(eventJson)
                                .with(request -> {
                                        request.setMethod("PUT");
                                        return request;
                                }))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$", is("User is not an artist")));

                verify(eventService, times(1)).isArtist();
                verify(eventService, times(0)).getEventById(anyLong());
                verify(eventService, times(0)).deleteImages(anyString());
                verify(eventService, times(0)).updateEvent(any(Event.class), any(EventDTO.class));
        }

        @Test
        void testUpdateEventNotBelongToArtist() throws Exception {
                Artist artist = new Artist();
                artist.setId(1L);

                Artist anotherArtist = new Artist();
                anotherArtist.setId(2L);

                Event event = new Event();
                event.setId(1L);
                event.setArtist(anotherArtist);

                EventDTO eventDTO = new EventDTO();
                eventDTO.setName("Updated Event");
                eventDTO.setDescription("Updated Description");

                MockMultipartFile eventJson = new MockMultipartFile("event", "", "application/json",
                                "{\"name\": \"Updated Event\", \"description\": \"Updated Description\"}".getBytes());

                when(authentication.getPrincipal()).thenReturn(artist);
                SecurityContextHolder.getContext().setAuthentication(authentication);
                when(eventService.isArtist()).thenReturn(true);
                when(eventService.getEventById(1L)).thenReturn(event);

                mockMvc.perform(MockMvcRequestBuilders.multipart("/api/event/edit/1")
                                .file(eventJson)
                                .with(request -> {
                                        request.setMethod("PUT");
                                        return request;
                                }))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$", is("This event does not belong to you")));

                verify(eventService, times(1)).isArtist();
                verify(eventService, times(1)).getEventById(1L);
                verify(eventService, times(0)).deleteImages(anyString());
                verify(eventService, times(0)).updateEvent(any(Event.class), any(EventDTO.class));
        }

        @Test
        void testGetProductImageSuccess() throws Exception {
                String fileName = UUID.randomUUID() + "test.jpg";
                String basePath = System.getProperty("user.dir")
                                + "/artists-heaven-backend/src/main/resources/event_media/";
                Path filePath = Paths.get(basePath, fileName);

                // Ensure the directory exists
                Files.createDirectories(filePath.getParent());

                // Create the file for the test
                Files.createFile(filePath);

                mockMvc.perform(get("/api/event/event_media/" + fileName))
                                .andExpect(status().isOk())
                                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, "image/png"));

                // Clean up the created file
                Files.deleteIfExists(filePath);
        }

        @Test
        void testGetProductImageNotFound() throws Exception {
                String fileName = "nonexistent.jpg";

                mockMvc.perform(get("/api/event/event_media/" + fileName))
                                .andExpect(status().isNotFound());
        }

        @AfterEach
        void tearDown() {
                SecurityContextHolder.clearContext();
        }

}
