package com.artists_heaven.entities.user;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;

import java.security.Principal;
import java.util.Collections;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.multipart.MultipartFile;

import com.artists_heaven.entities.artist.Artist;
import com.artists_heaven.images.ImageServingUtil;

import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

class UserControllerTest {

        private MockMvc mockMvc;

        @Mock
        private UserService userService;

        @Mock
        private UserRepository userRepository;

        @Mock
        private ImageServingUtil imageServingUtil;

        @InjectMocks
        private UserController userController;

        @BeforeEach
        void setUp() {
                MockitoAnnotations.openMocks(this);

                mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
        }

        @Test
        void testGetAllUsers() throws Exception {
                User user = new User(1L, "Jane", "Doe", "JaneDoe", "jane.doe@example.com", "password1234", "1234567890",
                                "Street 123", "1234", "Seville", "Spain",
                                UserRole.USER, null);

                when(userService.getAllUsers()).thenReturn(Collections.singletonList(user));

                mockMvc.perform(get("/api/users/list"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$[0].firstName").value("Jane"))
                                .andExpect(jsonPath("$[0].lastName").value("Doe"));
        }

        @Test
        void testRegisterUser() throws Exception {
                // Mockea el comportamiento del servicio
                User registeredUser = new User(1L, "Jane", "Doe", "JaneDoe", "jane.doe@example.com", "password1234",
                                "1234567890", "Street 123", "1234", "Seville", "Spain",
                                UserRole.USER, null);
                when(userService.registerUser(any(User.class))).thenReturn(registeredUser);

                // Realiza la solicitud POST para registrar un nuevo usuario
                mockMvc.perform(post("/api/users/register")
                                .contentType("application/json")
                                .content(
                                                "{\"firstName\":\"Jane\",\"lastName\":\"Doe\",\"email\":\"jane.doe@example.com\",\"password\":\"password1234\"}"))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.firstName").value("Jane"))
                                .andExpect(jsonPath("$.lastName").value("Doe"));
        }

        @Test
        void testRegisterUserBadRequest() throws Exception {
                // Simula una excepción en el servicio
                when(userService.registerUser(any(User.class)))
                                .thenThrow(new IllegalArgumentException("Invalid user data"));

                // Realiza la solicitud POST y espera una respuesta BAD_REQUEST
                mockMvc.perform(post("/api/users/register")
                                .contentType("application/json")
                                .content(
                                                "{\"firstName\":\"Jane\",\"lastName\":\"Doe\",\"email\":\"invalid-email\",\"password\":\"password1234\"}"))
                                .andExpect(status().isBadRequest());
        }

        @Test
        void testGetUserProfile() throws Exception {
                // Simular un usuario autenticado
                User user = new User(1L, "Jane", "Doe", "JaneDoe", "jane.doe@example.com", "password1234", "1234567890",
                                "Street 123", "1234", "Seville", "Spain",
                                UserRole.USER, null);
                when(userRepository.findByEmail("jane.doe@example.com")).thenReturn(user);

                // Simular autenticación
                Authentication authentication = mock(Authentication.class);
                when(authentication.getPrincipal()).thenReturn(user);
                Principal principal = (Principal) authentication;

                UserProfileDTO mockDTO = new UserProfileDTO(user);
                when(userService.getUserProfile(principal)).thenReturn(mockDTO);

                // Realizar la solicitud GET
                mockMvc.perform(get("/api/users/profile").principal(principal))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.email").value("jane.doe@example.com"));
        }

        @Test
        void testGetUserArtistProfile() throws Exception {
                // Simular un usuario autenticado
                Artist artist = new Artist();
                artist.setId(1L);
                artist.setFirstName("Artist Name");
                artist.setLastName("Artist lastName");
                artist.setEmail("artist@email.com");
                artist.setArtistName("Artist Name");
                artist.setRole(UserRole.ARTIST);
                when(userRepository.findByEmail("artist@email.com")).thenReturn(artist);

                // Simular autenticación
                Authentication authentication = mock(Authentication.class);
                when(authentication.getPrincipal()).thenReturn(artist);
                Principal principal = (Principal) authentication;

                UserProfileDTO mockDTO = new UserProfileDTO(artist);
                when(userService.getUserProfile(principal)).thenReturn(mockDTO);

                // Realizar la solicitud GET
                mockMvc.perform(get("/api/users/profile").principal(principal))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.email").value("artist@email.com"));
        }

        @Test
        void testUpdateUserProfile_success() throws Exception {
                // Simular archivos
                MockMultipartFile imageFile = new MockMultipartFile(
                                "image", "profile.jpg", MediaType.IMAGE_JPEG_VALUE, "fake-image-content".getBytes());
                MockMultipartFile bannerFile = new MockMultipartFile(
                                "bannerImage", "banner.jpg", MediaType.IMAGE_JPEG_VALUE,
                                "fake-banner-content".getBytes());

                // Simular principal
                Principal principal = () -> "testUser";

                // Simular guardado de imágenes
                when(imageServingUtil.saveImages(any(MultipartFile.class), anyString(), anyString(), eq(false)))
                                .thenReturn("saved/path.jpg");

                // Simular lógica de actualización sin excepción
                doNothing().when(userService).updateUserProfile(any(UserProfileUpdateDTO.class), eq(principal),
                                anyString(), anyString());

                // Enviar request multipart simulando PUT con "_method" override (Spring lo
                // permite)
                mockMvc.perform(multipart("/api/users/profile/edit")
                                .file(imageFile)
                                .file(bannerFile)
                                .param("email", "test@example.com")
                                .param("firstName", "Alice")
                                .param("lastName", "Smith")
                                .param("artistName", "BrushMaster")
                                .param("address", "123 Art Street")
                                .param("city", "Paris")
                                .param("postalCode", "75000")
                                .param("country", "France")
                                .param("phone", "+33 123456789")
                                .param("color", "#FF5733")
                                .with(request -> {
                                        request.setMethod("PUT");
                                        request.setUserPrincipal(principal);
                                        return request;
                                }))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.message").value("Profile updated successfully"));
        }

        @Test
        void testUpdateUserProfile_withoutImages_success() throws Exception {
                Principal principal = () -> "testUser";

                doNothing().when(userService).updateUserProfile(any(UserProfileUpdateDTO.class), eq(principal), eq(""),
                                eq(""));

                mockMvc.perform(multipart("/api/users/profile/edit")
                                .param("email", "test@example.com")
                                .param("firstName", "Alice")
                                .param("lastName", "Smith")
                                .param("artistName", "BrushMaster")
                                .param("address", "123 Art Street")
                                .param("city", "Paris")
                                .param("postalCode", "75000")
                                .param("country", "France")
                                .param("phone", "+33 123456789")
                                .param("color", "#FF5733")
                                .with(request -> {
                                        request.setMethod("PUT");
                                        request.setUserPrincipal(principal);
                                        return request;
                                }))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.message").value("Profile updated successfully"));
        }

        @Test
        void testUpdateUserProfile_generalException() throws Exception {
                Principal principal = () -> "testUser";

                doThrow(new RuntimeException("Unexpected error")).when(userService)
                                .updateUserProfile(any(UserProfileUpdateDTO.class), eq(principal), anyString(),
                                                anyString());

                mockMvc.perform(multipart("/api/users/profile/edit")
                                .param("email", "test@example.com")
                                .with(request -> {
                                        request.setMethod("PUT");
                                        request.setUserPrincipal(principal);
                                        return request;
                                }))
                                .andExpect(status().isInternalServerError());
        }

        @AfterEach
        void tearDown() {
                SecurityContextHolder.clearContext();
        }

}
