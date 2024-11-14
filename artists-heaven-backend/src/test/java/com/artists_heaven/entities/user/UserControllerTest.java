package com.artists_heaven.entities.user;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

public class UserControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
    }

    @Test
    public void testGetAllUsers() throws Exception {
        User user = new User(1L, "John", "Doe", "john.doe@example.com", "password1234", null);
        when(userService.getAllUsers()).thenReturn(Collections.singletonList(user));

        mockMvc.perform(get("/api/users/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].firstName").value("John"))
                .andExpect(jsonPath("$[0].lastName").value("Doe"));
    }

    @Test
    public void testRegisterUser() throws Exception {
        // Mockea el comportamiento del servicio
        User registeredUser = new User(1L, "Jane", "Doe", "jane.doe@example.com", "password1234", null);
        when(userService.registerUser(any(User.class))).thenReturn(registeredUser);

        // Realiza la solicitud POST para registrar un nuevo usuario
        mockMvc.perform(post("/api/users/register")
                        .contentType("application/json")
                        .content("{\"firstName\":\"Jane\",\"lastName\":\"Doe\",\"email\":\"jane.doe@example.com\",\"password\":\"password1234\"}"))
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
                        .content("{\"firstName\":\"Jane\",\"lastName\":\"Doe\",\"email\":\"invalid-email\",\"password\":\"password1234\"}"))
                .andExpect(status().isBadRequest());
    }
}
