package com.artists_heaven.entities.user;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.security.Principal;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.MessageSource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.artists_heaven.entities.artist.Artist;
import com.artists_heaven.exception.AppExceptions;
import com.artists_heaven.exception.AppExceptions.DuplicateActionException;
import com.artists_heaven.exception.AppExceptions.UnauthorizedActionException;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private MessageSource messageSource;

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
        UserRegisterDTO dto = new UserRegisterDTO();
        dto.setPassword("plainPassword");

        User user = new User();
        user.setRole(UserRole.USER);

        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        User result = userService.registerUser(dto, "es");

        assertEquals(UserRole.USER, result.getRole());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testRegisterUser_EmailDuplicate_ThrowsException() {
        UserRegisterDTO dto = new UserRegisterDTO();
        dto.setEmail("existing@example.com");

        User existingUser = new User();
        when(userRepository.findByEmail(dto.getEmail())).thenReturn(Optional.of(existingUser));

        // Mock del messageSource
        when(messageSource.getMessage(eq("email.duplicate"), any(), any(Locale.class)))
                .thenReturn("Email already exists");

        DuplicateActionException ex = assertThrows(DuplicateActionException.class,
                () -> userService.registerUser(dto, "es"));

        assertTrue(ex.getMessage().contains("Email already exists"));
    }

    @Test
    void testRegisterUser_UsernameAlreadyExists_ThrowsException() {
        UserRegisterDTO dto = new UserRegisterDTO();
        dto.setUsername("existingUsername");

        // Mock de repositorio
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(userRepository.existsByUsername(dto.getUsername())).thenReturn(true);

        // Mock del messageSource
        when(messageSource.getMessage(eq("username.alreadyExists"), any(), any(Locale.class)))
                .thenReturn("Username already exists");

        // Act & Assert
        DuplicateActionException ex = assertThrows(DuplicateActionException.class,
                () -> userService.registerUser(dto, "es"));

        assertTrue(ex.getMessage().contains("Username already exists"));
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
        assertEquals("Usuario no autenticado: tipo de usuario inválido", exception.getMessage());
    }

    @Test
    void testExtractAuthenticatedUser_ThrowsExceptionWhenPrincipalNotAuthentication() {
        Principal principal = mock(Principal.class); // Mock principal not as Authentication

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.extractAuthenticatedUser(principal);
        });

        assertEquals("Usuario no autenticado: no es una autenticación válida", exception.getMessage());
    }

    @Test
    void testUpdateUserProfile() {
        User user = new User();
        user.setFirstName("OldFirstName");
        user.setLastName("OldLastName");
        user.setUsername("OldFirstName");

        UserProfileUpdateDTO userProfileDTO = new UserProfileUpdateDTO();
        userProfileDTO.setFirstName("NewFirstName");
        userProfileDTO.setLastName("NewLastName");
        userProfileDTO.setId(1L);

        Principal principal = mock(Authentication.class);
        when(((Authentication) principal).getPrincipal()).thenReturn(user);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(principal.getName()).thenReturn("OldFirstName");

        userService.updateUserProfile(userProfileDTO, principal, "", "es");

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
        artist.setUsername("OldFirstName");
        UserProfileUpdateDTO userProfileDTO = new UserProfileUpdateDTO();
        userProfileDTO.setFirstName("NewFirstName");
        userProfileDTO.setLastName("NewLastName");
        userProfileDTO.setArtistName("NewArtistName");
        userProfileDTO.setId(1L);

        Principal principal = mock(Authentication.class);
        when(((Authentication) principal).getPrincipal()).thenReturn(artist);
        when(userRepository.findById(1L)).thenReturn(Optional.of(artist));
        when(principal.getName()).thenReturn("OldFirstName");

        userService.updateUserProfile(userProfileDTO, principal, "/mainImage", "es");

        assertEquals("NewFirstName", artist.getFirstName());
        assertEquals("NewLastName", artist.getLastName());
        assertEquals("NewArtistName", artist.getArtistName());
        verify(userRepository, times(1)).save(artist);
    }

    @Test
    void testUpdateUserProfile_PrincipalNull_ThrowsUnauthorized() {
        UserProfileUpdateDTO dto = new UserProfileUpdateDTO();
        dto.setId(1L);

        UnauthorizedActionException ex = assertThrows(
                AppExceptions.UnauthorizedActionException.class,
                () -> userService.updateUserProfile(dto, null, "", "es"));

        assertEquals("User is not authenticated", ex.getMessage());
    }

    @Test
    void testUpdateUserProfile_PrincipalDifferentUser_ThrowsForbidden() {
        User user = new User();
        user.setId(1L);
        user.setUsername("user1");

        UserProfileUpdateDTO dto = new UserProfileUpdateDTO();
        dto.setId(1L);

        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn("otherUser");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        AppExceptions.ForbiddenActionException ex = assertThrows(
                AppExceptions.ForbiddenActionException.class,
                () -> userService.updateUserProfile(dto, principal, "", "es"));

        assertEquals("You cannot edit another user's profile", ex.getMessage());
    }

    @Test
    void testUpdateUserProfile_UsernameAlreadyExists_ThrowsDuplicate() {
        User user = new User();
        user.setId(1L);
        user.setUsername("oldUser");

        UserProfileUpdateDTO dto = new UserProfileUpdateDTO();
        dto.setId(1L);
        dto.setUsername("existingUsername");

        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn("oldUser");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByUsername("existingUsername")).thenReturn(true);
        when(messageSource.getMessage(eq("username.alreadyExists"), any(), any(Locale.class)))
                .thenReturn("Username already exists");

        DuplicateActionException ex = assertThrows(
                DuplicateActionException.class,
                () -> userService.updateUserProfile(dto, principal, "", "es"));

        assertEquals("Username already exists", ex.getMessage());
    }

    @Test
    void testUpdateUserProfile_ArtistWithImages() {
        Artist artist = new Artist();
        artist.setId(1L);
        artist.setUsername("artistUser");
        artist.setArtistName("OldArtist");

        UserProfileUpdateDTO dto = new UserProfileUpdateDTO();
        dto.setId(1L);
        dto.setArtistName("NewArtist");
        dto.setColor("#FFFFFF");

        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn("artistUser");
        when(userRepository.findById(1L)).thenReturn(Optional.of(artist));

        userService.updateUserProfile(dto, principal, "/mainImage", "es");

        assertEquals("NewArtist", artist.getArtistName());
        assertEquals("/mainImage", artist.getMainViewPhoto());
        assertEquals("#FFFFFF", artist.getMainColor());
        verify(userRepository, times(1)).save(artist);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }
}
