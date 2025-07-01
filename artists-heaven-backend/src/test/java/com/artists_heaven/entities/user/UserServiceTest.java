package com.artists_heaven.entities.user;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.artists_heaven.entities.artist.Artist;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAllUsers() {
        User user1 = new User();
        User user2 = new User();
        List<User> userList = List.of(user1, user2);

        when(userRepository.findAll()).thenReturn(userList);

        List<User> result = userService.getAllUsers();

        assertEquals(2, result.size());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void testRegisterUser() {
        User user = new User();
        user.setPassword("plainPassword");

        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        User result = userService.registerUser(user);

        assertEquals(UserRole.USER, result.getRole());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void testGetUserProfile() {
        User user = new User();
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setRole(UserRole.USER);
        Principal principal = mock(Authentication.class);
        when(((Authentication) principal).getPrincipal()).thenReturn(user);

        UserProfileDTO result = userService.getUserProfile(principal);

        assertEquals("John", result.getFirstName());
        assertEquals("Doe", result.getLastName());
    }

    @Test
    void testGetUserProfile_Artist() {
        Artist artist = new Artist();
        artist.setFirstName("John");
        artist.setLastName("Doe");
        artist.setArtistName("JDArtist");
        artist.setRole(UserRole.ARTIST);
        Principal principal = mock(Authentication.class);
        when(((Authentication) principal).getPrincipal()).thenReturn(artist);

        UserProfileDTO result = userService.getUserProfile(principal);

        assertEquals("John", result.getFirstName());
        assertEquals("Doe", result.getLastName());
        assertEquals("JDArtist", result.getArtistName());
    }

    @Test
    void testGetUserProfileException() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(null);
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.getUserProfile(authentication);
        });
        assertEquals("Usuario no autenticado", exception.getMessage());
    }

    @Test
    void testExtractAuthenticatedUser_ThrowsExceptionWhenPrincipalNotAuthentication() {
        Principal principal = mock(Principal.class); // Mock principal not as Authentication

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.extractAuthenticatedUser(principal);
        });

        assertEquals("Usuario no autenticado", exception.getMessage());
    }

    @Test
    void testUpdateUserProfile() {
        User user = new User();
        user.setFirstName("OldFirstName");
        user.setLastName("OldLastName");
        UserProfileUpdateDTO userProfileDTO = new UserProfileUpdateDTO();
        userProfileDTO.setFirstName("NewFirstName");
        userProfileDTO.setLastName("NewLastName");
        userProfileDTO.setId(1L);

        Principal principal = mock(Authentication.class);
        when(((Authentication) principal).getPrincipal()).thenReturn(user);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        userService.updateUserProfile(userProfileDTO, principal, "", "");

        assertEquals("NewFirstName", user.getFirstName());
        assertEquals("NewLastName", user.getLastName());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void testUpdateUserArtistProfile() {
        Artist artist = new Artist();
        artist.setFirstName("OldFirstName");
        artist.setLastName("OldLastName");
        artist.setArtistName("OldArtistName");
        UserProfileUpdateDTO userProfileDTO = new UserProfileUpdateDTO();
        userProfileDTO.setFirstName("NewFirstName");
        userProfileDTO.setLastName("NewLastName");
        userProfileDTO.setArtistName("NewArtistName");
        userProfileDTO.setId(1L);

        Principal principal = mock(Authentication.class);
        when(((Authentication) principal).getPrincipal()).thenReturn(artist);
        when(userRepository.findById(1L)).thenReturn(Optional.of(artist));

        userService.updateUserProfile(userProfileDTO, principal, "/mainImage", "/image");

        assertEquals("NewFirstName", artist.getFirstName());
        assertEquals("NewLastName", artist.getLastName());
        assertEquals("NewArtistName", artist.getArtistName());
        verify(userRepository, times(1)).save(artist);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }
}
