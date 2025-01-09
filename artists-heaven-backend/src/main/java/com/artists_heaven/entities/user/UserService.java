package com.artists_heaven.entities.user;

import java.security.Principal;
import java.util.List;

import org.springframework.stereotype.Service;
import com.artists_heaven.entities.artist.Artist;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Registers a new user by encoding the password and setting the default role.
     * 
     * @param user The user to be registered, including their details like email,
     *             password, and name.
     * @return The registered user with their password encrypted and default role
     *         set.
     */
    public User registerUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(UserRole.USER);
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
        // Extract the authenticated user
        User user = extractAuthenticatedUser(principal);
        // Create a UserProfileDTO object from the user data
        UserProfileDTO userProfileDTO = new UserProfileDTO(user);

        // If the user is an artist, include additional details like the artist's name
        if (user instanceof Artist artist) {
            userProfileDTO.setArtistName(artist.getArtistName());
        }

        // Return the user profile DTO
        return userProfileDTO;
    }

    /**
     * Updates the profile of the authenticated user.
     * 
     * @param userProfileDTO The DTO containing the new profile data (first name,
     *                       last name, and artist name if applicable).
     * @param principal      The authenticated user's principal.
     * @throws IllegalArgumentException if the user is not authenticated.
     */
    public void updateUserProfile(UserProfileDTO userProfileDTO, Principal principal) {
        // Extract the authenticated user
        User user = extractAuthenticatedUser(principal);

        // Update the user's first and last name
        user.setFirstName(userProfileDTO.getFirstName());
        user.setLastName(userProfileDTO.getLastName());

        // If the user is an artist, update the artist name if it's provided
        if (user instanceof Artist artist && userProfileDTO.getArtistName() != null) {
            artist.setArtistName(userProfileDTO.getArtistName());
        }

        // Save the updated user data to the repository
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
        // Check if the principal is an authenticated instance
        if (!(principal instanceof Authentication authentication)) {
            throw new IllegalArgumentException("Usuario no autenticado");
        }
        // Extract the user from the authentication object
        Object principalUser = authentication.getPrincipal();
        if (!(principalUser instanceof User user)) {
            throw new IllegalArgumentException("Usuario no autenticado");
        }

        // Return the authenticated user
        return user;
    }
}
