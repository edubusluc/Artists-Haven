package com.artists_heaven.event;

import com.artists_heaven.entities.artist.Artist;
import com.artists_heaven.exception.AppExceptions.ForbiddenActionException;
import com.artists_heaven.exception.AppExceptions.ResourceNotFoundException;
import com.artists_heaven.exception.AppExceptions.UnauthorizedActionException;
import com.artists_heaven.exception.GlobalExceptionHandler;
import com.artists_heaven.images.ImageServingUtil;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.core.io.FileSystemResource;
import org.springframework.data.domain.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.*;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.nio.file.Files;
import java.util.List;
import org.springframework.core.io.Resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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

        private Artist artist;
        private Event event;

        @BeforeEach
        void setUp() {
                MockitoAnnotations.openMocks(this);
                mockMvc = MockMvcBuilders
                                .standaloneSetup(eventController)
                                .setControllerAdvice(new GlobalExceptionHandler())
                                .build();
                SecurityContextHolder.clearContext();

                artist = new Artist();
                artist.setId(1L);
                artist.setIsVerificated(true);

                event = new Event();
                event.setId(100L);
                event.setName("Test Event");
                event.setArtist(artist);
                event.setImage("test.png");
        }

        private void mockAuthenticatedArtist() {
                when(authentication.isAuthenticated()).thenReturn(true);
                when(authentication.getPrincipal()).thenReturn(artist);
                SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        private void mockAuthenticatedUser(Object principal) {
                when(authentication.isAuthenticated()).thenReturn(true);
                when(authentication.getPrincipal()).thenReturn(principal);
                SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        private void mockUnauthenticated() {
                when(authentication.isAuthenticated()).thenReturn(false);
                SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        @Nested
        class GetAllFutureEvents {
                @Test
                void shouldReturn200WithEvents() throws Exception {
                        when(eventService.findFutureEvents()).thenReturn(List.of(event));
                        mockMvc.perform(get("/api/event/allFutureEvents"))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.message").value("Future events retrieved successfully"));
                }

                @Test
                void shouldReturn204WhenNoEvents() throws Exception {
                        when(eventService.findFutureEvents()).thenReturn(List.of());
                        mockMvc.perform(get("/api/event/allFutureEvents"))
                                        .andExpect(status().isNoContent())
                                        .andExpect(jsonPath("$.message").value("No future events found"));
                }
        }

        @Nested
        class GetFutureEventsByArtist {
                @Test
                void shouldReturn200WithEvents() throws Exception {
                        when(eventService.findFutureEventsByArtist(1L)).thenReturn(List.of(event));
                        mockMvc.perform(get("/api/event/futureEvents/1"))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.message").value("Future events retrieved successfully"));
                }

                @Test
                void shouldReturn204WhenNoEvents() throws Exception {
                        when(eventService.findFutureEventsByArtist(1L)).thenReturn(List.of());
                        mockMvc.perform(get("/api/event/futureEvents/1"))
                                        .andExpect(status().isNoContent())
                                        .andExpect(jsonPath("$.message")
                                                        .value("No future events found for the artist"));
                }
        }

        @Nested
        class IsVerified {
                @Test
                void shouldReturn401WhenNotAuthenticated() throws Exception {
                        mockUnauthenticated();
                        mockMvc.perform(get("/api/event/isVerified"))
                                        .andExpect(status().isUnauthorized())
                                        .andExpect(jsonPath("$.message").value("User is not authenticated"));
                }

                @Test
                void shouldReturn200WhenArtistVerified() throws Exception {
                        mockAuthenticatedArtist();
                        mockMvc.perform(get("/api/event/isVerified"))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.data").value(true));
                }

                @Test
                void shouldReturn403WhenNotArtist() throws Exception {
                        mockAuthenticatedUser(new Object());
                        mockMvc.perform(get("/api/event/isVerified"))
                                        .andExpect(status().isForbidden())
                                        .andExpect(jsonPath("$.message").value("User is not an artist"));
                }
        }

        @Nested
        class NewEvent {
                @Test
                void shouldCreateEvent() throws Exception {
                        mockAuthenticatedArtist();
                        MockMultipartFile eventPart = new MockMultipartFile("event", "", "application/json",
                                        "{\"name\":\"New Event\"}".getBytes());
                        MockMultipartFile image = new MockMultipartFile("images", "test.png", "image/png",
                                        "fake".getBytes());

                        when(imageServingUtil.saveImages(any(), anyString(), anyString(), anyBoolean()))
                                        .thenReturn("test.png");
                        when(eventService.newEvent(any(EventDTO.class), anyString())).thenReturn(event);

                        mockMvc.perform(multipart("/api/event/new")
                                        .file(eventPart)
                                        .file(image))
                                        .andExpect(status().isCreated())
                                        .andExpect(jsonPath("$.message").value("Event created successfully"));
                }

                @Test
                void shouldFailWhenNotAuthenticated() throws Exception {
                        mockUnauthenticated();
                        MockMultipartFile eventPart = new MockMultipartFile("event", "", "application/json",
                                        "{\"name\":\"New Event\"}".getBytes());
                        MockMultipartFile image = new MockMultipartFile("images", "test.png", "image/png",
                                        "fake".getBytes());

                        mockMvc.perform(multipart("/api/event/new")
                                        .file(eventPart)
                                        .file(image))
                                        .andExpect(status().isUnauthorized());
                }

                @Test
                void shouldFailWhenNotArtist() throws Exception {
                        mockAuthenticatedUser(new Object());
                        MockMultipartFile eventPart = new MockMultipartFile("event", "", "application/json",
                                        "{\"name\":\"New Event\"}".getBytes());
                        MockMultipartFile image = new MockMultipartFile("images", "test.png", "image/png",
                                        "fake".getBytes());

                        mockMvc.perform(multipart("/api/event/new")
                                        .file(eventPart)
                                        .file(image))
                                        .andExpect(status().isForbidden());
                }

                @Test
                void eventDetails_unauthenticatedUser_throwsUnauthorized() {
                        SecurityContextHolder.clearContext();

                        Long eventId = 1L;

                        UnauthorizedActionException ex = assertThrows(UnauthorizedActionException.class, () -> {
                                eventController.eventDetails(eventId);
                        });

                        assertEquals("User is not authenticated", ex.getMessage());
                }

                @Test
                void eventDetails_userNotArtist_throwsForbidden() {
                        Authentication auth = mock(Authentication.class);
                        when(auth.isAuthenticated()).thenReturn(true);
                        when(auth.getPrincipal()).thenReturn(new Object());

                        SecurityContext context = mock(SecurityContext.class);
                        when(context.getAuthentication()).thenReturn(auth);
                        SecurityContextHolder.setContext(context);

                        Long eventId = 1L;

                        ForbiddenActionException ex = assertThrows(ForbiddenActionException.class, () -> {
                                eventController.eventDetails(eventId);
                        });

                        assertEquals("User is not an artist", ex.getMessage());
                }

                @Test
                void eventDetails_eventDoesNotBelongToArtist_throwsResourceNotFound() {
                        // Simular autenticación de un Artist
                        Artist artist = new Artist();
                        artist.setId(1L);

                        Authentication auth = mock(Authentication.class);
                        when(auth.isAuthenticated()).thenReturn(true);
                        when(auth.getPrincipal()).thenReturn(artist);

                        SecurityContext context = mock(SecurityContext.class);
                        when(context.getAuthentication()).thenReturn(auth);
                        SecurityContextHolder.setContext(context);

                        // Simular un evento que pertenece a otro artista
                        Artist otherArtist = new Artist();
                        otherArtist.setId(2L);
                        Event event = new Event();
                        event.setArtist(otherArtist);

                        when(eventService.getEventById(10L)).thenReturn(event);

                        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () -> {
                                eventController.eventDetails(10L);
                        });

                        assertEquals("Event does not belong to you", ex.getMessage());
                }
        }

        @Nested
        class GetAllMyEvents {
                @Test
                void shouldReturnEventsForArtist() throws Exception {
                        mockAuthenticatedArtist();
                        Page<Event> page = new PageImpl<>(List.of(event));
                        when(eventService.getAllMyEvents(eq(1L), any(PageRequest.class))).thenReturn(page);

                        mockMvc.perform(get("/api/event/allMyEvents?page=0&size=6"))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.message").value("Events retrieved successfully"));
                }

                @Test
                void shouldFailWhenNotArtist() throws Exception {
                        mockAuthenticatedUser(new Object());
                        mockMvc.perform(get("/api/event/allMyEvents?page=0&size=6"))
                                        .andExpect(status().isForbidden());
                }

                @Test
                void shouldFailWhenNotAuthenticated() throws Exception {
                        mockUnauthenticated();
                        mockMvc.perform(get("/api/event/allMyEvents"))
                                        .andExpect(status().isUnauthorized());
                }
        }

        @Nested
        class DeleteEvent {
                @Test
                void shouldDeleteOwnEvent() throws Exception {
                        mockAuthenticatedArtist();
                        when(eventService.getEventById(100L)).thenReturn(event);

                        mockMvc.perform(delete("/api/event/delete/100"))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.message").value("Event deleted successfully"));

                        verify(eventService).deleteEvent(100L);
                }

                @Test
                void shouldFailWhenEventNotFound() throws Exception {
                        mockAuthenticatedArtist();
                        when(eventService.getEventById(100L)).thenReturn(null);

                        mockMvc.perform(delete("/api/event/delete/100"))
                                        .andExpect(status().isNotFound());
                }

                @Test
                void shouldFailWhenEventNotBelongsToArtist() throws Exception {
                        Artist anotherArtist = new Artist();
                        anotherArtist.setId(2L);
                        event.setArtist(anotherArtist);

                        mockAuthenticatedArtist();
                        when(eventService.getEventById(100L)).thenReturn(event);

                        mockMvc.perform(delete("/api/event/delete/100"))
                                        .andExpect(status().isNotFound());
                }
        }

        @Nested
        class EventDetails {
                @Test
                void shouldReturnEventDetails() throws Exception {
                        Artist artist = new Artist();
                        artist.setId(1L);

                        Authentication authentication = mock(Authentication.class);
                        when(authentication.isAuthenticated()).thenReturn(true);
                        when(authentication.getPrincipal()).thenReturn(artist);
                        SecurityContext securityContext = mock(SecurityContext.class);
                        when(securityContext.getAuthentication()).thenReturn(authentication);
                        SecurityContextHolder.setContext(securityContext);
                        when(eventService.getEventById(100L)).thenReturn(event);

                        mockMvc.perform(get("/api/event/details/100"))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.message").value("Event retrieved successfully"));
                }

                @Test
                void shouldReturn404WhenEventNotFound() throws Exception {
                        Artist artist = new Artist();
                        artist.setId(1L);

                        Authentication authentication = mock(Authentication.class);
                        when(authentication.isAuthenticated()).thenReturn(true);
                        when(authentication.getPrincipal()).thenReturn(artist);
                        SecurityContext securityContext = mock(SecurityContext.class);
                        when(securityContext.getAuthentication()).thenReturn(authentication);
                        SecurityContextHolder.setContext(securityContext);
                        when(eventService.getEventById(100L)).thenReturn(null);

                        mockMvc.perform(get("/api/event/details/100"))
                                        .andExpect(status().isNotFound());
                }
        }

        @Nested
        class UpdateEvent {
                @Test
                void shouldUpdateEventWithImage() throws Exception {
                        mockAuthenticatedArtist();
                        when(eventService.isArtist()).thenReturn(true);
                        when(eventService.getEventById(100L)).thenReturn(event);
                        when(imageServingUtil.saveImages(any(), anyString(), anyString(), anyBoolean()))
                                        .thenReturn("new.png");

                        MockMultipartFile eventPart = new MockMultipartFile("event", "", "application/json",
                                        "{\"name\":\"Updated Event\"}".getBytes());
                        MockMultipartFile image = new MockMultipartFile("image", "new.png", "image/png",
                                        "fake".getBytes());

                        mockMvc.perform(multipart("/api/event/edit/100")
                                        .file(eventPart)
                                        .file(image)
                                        .with(request -> {
                                                request.setMethod("PUT");
                                                return request;
                                        }))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.message").value("Event updated successfully"));
                }

                @Test
                void shouldFailWhenNotArtist() throws Exception {
                        mockAuthenticatedArtist();
                        when(eventService.isArtist()).thenReturn(false);

                        MockMultipartFile eventPart = new MockMultipartFile("event", "", "application/json",
                                        "{\"name\":\"Updated Event\"}".getBytes());

                        mockMvc.perform(multipart("/api/event/edit/100")
                                        .file(eventPart)
                                        .with(request -> {
                                                request.setMethod("PUT");
                                                return request;
                                        }))
                                        .andExpect(status().isUnauthorized());
                }

                @Test
                void shouldFailWhenEventNotFound() throws Exception {
                        mockAuthenticatedArtist();
                        when(eventService.isArtist()).thenReturn(true);
                        when(eventService.getEventById(100L)).thenReturn(null);

                        MockMultipartFile eventPart = new MockMultipartFile("event", "", "application/json",
                                        "{\"name\":\"Updated Event\"}".getBytes());

                        mockMvc.perform(multipart("/api/event/edit/100")
                                        .file(eventPart)
                                        .with(request -> {
                                                request.setMethod("PUT");
                                                return request;
                                        }))
                                        .andExpect(status().isNotFound());
                }

                @Test
                void shouldFailWhenEventDoesNotBelongToArtist() throws Exception {
                        // Simular autenticación de un artista con id 1
                        Artist loggedInArtist = new Artist();
                        loggedInArtist.setId(1L);

                        Authentication auth = mock(Authentication.class);
                        when(auth.isAuthenticated()).thenReturn(true);
                        when(auth.getPrincipal()).thenReturn(artist);

                        SecurityContext context = mock(SecurityContext.class);
                        when(context.getAuthentication()).thenReturn(auth);
                        SecurityContextHolder.setContext(context);

                        when(eventService.isArtist()).thenReturn(true);

                        // Simular evento que pertenece a otro artista (id diferente)
                        Artist otherArtist = new Artist();
                        otherArtist.setId(2L);
                        Event eventOwnedByOther = new Event();
                        eventOwnedByOther.setArtist(otherArtist);

                        when(eventService.getEventById(100L)).thenReturn(eventOwnedByOther);

                        MockMultipartFile eventPart = new MockMultipartFile("event", "", "application/json",
                                        "{\"name\":\"Updated Event\"}".getBytes());

                        mockMvc.perform(multipart("/api/event/edit/100")
                                        .file(eventPart)
                                        .with(request -> {
                                                request.setMethod("PUT");
                                                return request;
                                        }))
                                        .andExpect(status().isNotFound())
                                        .andExpect(result -> assertTrue(result
                                                        .getResolvedException() instanceof ResourceNotFoundException))
                                        .andExpect(result -> assertEquals("This event does not belong to you",
                                                        result.getResolvedException().getMessage()));
                }
        }

        // GENERA UNA CARPETA MAL
        @Nested
        class GetEventMedia {
                @Test
                void shouldReturnImageWhenExists() throws Exception {
                        // Crear un recurso simulado (no toca el filesystem)
                        Resource fakeResource = new FileSystemResource(
                                        Files.createTempFile("event_image", ".png").toFile());

                        // Mockear ImageServingUtil para que devuelva el recurso
                        when(imageServingUtil.serveImage(anyString(), eq("event_image.png")))
                                        .thenReturn(ResponseEntity.ok()
                                                        .header(HttpHeaders.CONTENT_TYPE, "image/png")
                                                        .body(fakeResource));

                        // Ejecutar MockMvc
                        mockMvc.perform(get("/api/event/event_media/event_image.png"))
                                        .andExpect(status().isOk())
                                        .andExpect(header().string("Content-Type", "image/png"));
                }
        }
}
