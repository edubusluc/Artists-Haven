package com.artists_heaven.entities.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.security.Principal;
import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import com.fasterxml.jackson.databind.ObjectMapper;


import com.artists_heaven.entities.artist.Artist;

import org.springframework.security.core.Authentication;

public class UserControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserController userController;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
    }

    @Test
    public void testGetAllUsers() throws Exception {
        User user = new User(1L, "Jane", "Doe", "JaneDoe", "jane.doe@example.com", "password1234", UserRole.USER);

        when(userService.getAllUsers()).thenReturn(Collections.singletonList(user));

        mockMvc.perform(get("/api/users/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].firstName").value("Jane"))
                .andExpect(jsonPath("$[0].lastName").value("Doe"));
    }

    @Test
    public void testRegisterUser() throws Exception {
        // Mockea el comportamiento del servicio
        User registeredUser = new User(1L, "Jane", "Doe", "JaneDoe", "jane.doe@example.com", "password1234",
                UserRole.USER);
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
    public void testRegisterUserBadRequest() throws Exception {
        // Simula una excepción en el servicio
        when(userService.registerUser(any(User.class))).thenThrow(new IllegalArgumentException("Invalid user data"));

        // Realiza la solicitud POST y espera una respuesta BAD_REQUEST
        mockMvc.perform(post("/api/users/register")
                .contentType("application/json")
                .content(
                        "{\"firstName\":\"Jane\",\"lastName\":\"Doe\",\"email\":\"invalid-email\",\"password\":\"password1234\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testGetUserProfile() throws Exception {
        // Simular un usuario autenticado
        User user = new User(1L, "John", "Doe", "JohnDoe", "john.doe@example.com", "password1234", UserRole.USER);
        when(userRepository.findByEmail("john.doe@example.com")).thenReturn(user);

        // Simular autenticación
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(user);
        Principal principal = (Principal) authentication;

        // Realizar la solicitud GET
        mockMvc.perform(get("/api/users/profile").principal(principal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("john.doe@example.com"));
    }

    @Test
    public void testGetUserArtistProfile() throws Exception {
        // Simular un usuario autenticado
        Artist artist = new Artist();
        artist.setId(1L);
        artist.setFirstName("Artist Name");
        artist.setLastName("Artist lastName");
        artist.setEmail("artist@email.com");
        artist.setArtistName("Artist Name");
        when(userRepository.findByEmail("artist@email.com")).thenReturn(artist);

        // Simular autenticación
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(artist);
        Principal principal = (Principal) authentication;

        // Realizar la solicitud GET
        mockMvc.perform(get("/api/users/profile").principal(principal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("artist@email.com"));
    }

    @Test
    public void testGetUserProfileNotFound() throws Exception {
        // Simular un usuario que no se encuentra en el sistema
        when(userRepository.findByEmail("missing.user@example.com")).thenReturn(null);

        // Simular autenticación
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(null);
        Principal principal = (Principal) authentication;

        // Realizar la solicitud GET
        mockMvc.perform(get("/api/users/profile").principal(principal))
                .andExpect(jsonPath("$").value("Usuario no autenticado"));
    }

    @Test
    public void testUpdateUserProfile() throws Exception {
        // Crear un usuario simulado
        Artist artist = new Artist();
        artist.setId(1L);
        artist.setFirstName("John");
        artist.setLastName("Doe");
        artist.setEmail("john.doe@example.com");
        artist.setArtistName("Old Artist Name");

        // Crear un DTO con los nuevos datos
        UserProfileDTO updatedProfile = new UserProfileDTO();
        updatedProfile.setFirstName("New John");
        updatedProfile.setLastName("New Doe");
        updatedProfile.setArtistName("New Artist Name");

        // Simular Authentication
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(artist);

        // Mockear el UserRepository
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Realizar la solicitud PUT con el DTO
        mockMvc.perform(put("/api/users/profile/edit")
                .principal(authentication)
                .contentType("application/json")
                .content("{\"firstName\":\"New John\", \"lastName\":\"New Doe\", \"artistName\":\"New Artist Name\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Perfil actualizado correctamente"));

        // Verificar que los datos del usuario se actualizaron
        assertEquals("New John", artist.getFirstName());
        assertEquals("New Doe", artist.getLastName());
        assertEquals("New Artist Name", artist.getArtistName());
    }


@Test
public void testUpdateUserProfileUnauthorized() throws Exception {
    // Simular una solicitud PUT sin autenticación
    Principal principal = mock(Principal.class); // Mock que no es una instancia de Authentication

    // Crear un DTO de perfil para pasar en la solicitud
    UserProfileDTO userProfileDTO = new UserProfileDTO();
    userProfileDTO.setFirstName("UpdatedFirstName");
    userProfileDTO.setLastName("UpdatedLastName");

    // Realizar la solicitud PUT y verificar que la respuesta sea UNAUTHORIZED
    mockMvc.perform(put("/api/users/profile/edit")
            .principal(principal)
            .contentType("application/json")
            .content(new ObjectMapper().writeValueAsString(userProfileDTO)))
            .andExpect(status().isUnauthorized())
            .andExpect(content().string("Usuario no autenticado"));
}

}
