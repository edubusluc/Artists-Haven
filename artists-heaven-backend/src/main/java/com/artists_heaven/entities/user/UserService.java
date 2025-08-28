package com.artists_heaven.entities.user;

import java.security.Principal;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;
import com.artists_heaven.entities.artist.Artist;
import com.artists_heaven.exception.AppExceptions;
import com.artists_heaven.exception.AppExceptions.DuplicateActionException;
import com.artists_heaven.exception.AppExceptions.ResourceNotFoundException;

import org.springframework.context.MessageSource;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Service
public class UserService {

    private final UserRepository userRepository;

    private final MessageSource messageSource;

    public UserService(UserRepository userRepository, MessageSource messageSource) {
        this.userRepository = userRepository;
        this.messageSource = messageSource;
    }

    private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
    }

    /**
     * Registers a new user by encoding the password and setting the default role.
     * 
     * @param user The user to be registered, including their details like email,
     *             password, and name.
     * @return The registered user with their password encrypted and default role
     *         set.
     */
    public User registerUser(UserRegisterDTO userDTO, String lang) {
        User user = new User();

        Locale locale = new Locale(lang);

        userRepository.findByEmail(userDTO.getEmail()).ifPresent(u -> {
            String msg = messageSource.getMessage("email.duplicate", null, locale);
            throw new DuplicateActionException(msg);
        });

        if (userRepository.existsByUsername(userDTO.getUsername())) {
            String msg = messageSource.getMessage("username.alreadyExists", null, locale);
            throw new DuplicateActionException(msg);
        }

        user.setUsername(userDTO.getUsername());
        user.setEmail(userDTO.getEmail());
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        user.setRole(UserRole.USER);
        user.setFirstName(userDTO.getFirstName());
        user.setLastName(userDTO.getLastName());
        user.setPhone(userDTO.getPhone());
        user.setCountry(userDTO.getCountry());
        user.setPostalCode(userDTO.getPostalCode());
        user.setCity(userDTO.getCity());
        user.setAddress(userDTO.getAddress());

        return userRepository.save(user);
    }

    /**
     * Retrieves the profile information of the authenticated user.
     * 
     * @param principal The authenticated user's principal (can be cast to an
     *                  Authentication object).
     * @return A DTO (Data Transfer Object) containing the profile data of the user.
     * @throws IllegalArgumentException if the user is not authenticated.
     */
    public UserProfileDTO getUserProfile(Principal principal) {
        User user = extractAuthenticatedUser(principal);
        UserProfileDTO dto = new UserProfileDTO(user);

        if (user instanceof Artist artist) {
            dto.setArtistName(artist.getArtistName());
            dto.setBannerImage(artist.getBannerPhoto());
            dto.setColor(artist.getMainColor());
            dto.setImage(artist.getMainViewPhoto());
        }

        return dto;
    }

    /**
     * Updates the profile of the authenticated user.
     * 
     * @param userProfileDTO The DTO containing the new profile data (first name,
     *                       last name, and artist name if applicable).
     * @param principal      The authenticated user's principal.
     * @throws IllegalArgumentException if the user is not authenticated.
     */
    public void updateUserProfile(UserProfileUpdateDTO userProfileDTO, Principal principal, String image,
            String bannerImage, String lang) {

        Locale locale = new Locale(lang);

        if (principal == null) {
            throw new AppExceptions.UnauthorizedActionException("User is not authenticated");
        }

        User user = getUserById(userProfileDTO.getId());

        // Validate that the authenticated user matches the user to be edited
        if (!principal.getName().equals(user.getUsername())) {
            throw new AppExceptions.ForbiddenActionException("You cannot edit another user's profile");
            }

        if (!user.getUsername().equals(userProfileDTO.getUsername()) && userRepository.existsByUsername(userProfileDTO.getUsername())) {
            String msg = messageSource.getMessage("username.alreadyExists", null, locale);
            throw new DuplicateActionException(msg);
        }

        // Data update
        user.setFirstName(userProfileDTO.getFirstName());
        user.setLastName(userProfileDTO.getLastName());
        user.setUsername(userProfileDTO.getUsername());
        user.setEmail(userProfileDTO.getEmail());
        user.setPhone(userProfileDTO.getPhone());
        user.setCity(userProfileDTO.getCity());
        user.setAddress(userProfileDTO.getAddress());
        user.setPostalCode(userProfileDTO.getPostalCode());
        user.setCountry(userProfileDTO.getCountry());
            

        if (user instanceof Artist artist) {
            if (userProfileDTO.getArtistName() != null) {
                artist.setArtistName(userProfileDTO.getArtistName());
                artist.setMainColor(userProfileDTO.getColor());
            }
            if (!bannerImage.isEmpty()) {
                artist.setBannerPhoto(bannerImage);
            }
            if (!image.isEmpty()) {
                artist.setMainViewPhoto(image);
            }
        }
        userRepository.save(user);
        
    }

    /**
     * Extracts the authenticated user from the Principal object.
     * 
     * @param principal The authenticated user's principal.
     * @return The authenticated user object.
     * @throws IllegalArgumentException if the principal is not an instance of
     *                                  Authentication or if the user is not
     *                                  authenticated.
     */
    public User extractAuthenticatedUser(Principal principal) {
        if (principal == null) {
            throw new IllegalArgumentException("Usuario no autenticado: principal es null");
        }

        if (!(principal instanceof Authentication authentication)) {
            throw new IllegalArgumentException("Usuario no autenticado: no es una autenticación válida");
        }

        Object principalUser = authentication.getPrincipal();

        if (!(principalUser instanceof User user)) {
            throw new IllegalArgumentException("Usuario no autenticado: tipo de usuario inválido");
        }

        return user;
    }
}
