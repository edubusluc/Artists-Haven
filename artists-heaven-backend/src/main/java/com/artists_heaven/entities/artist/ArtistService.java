package com.artists_heaven.entities.artist;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.artists_heaven.entities.user.User;
import com.artists_heaven.entities.user.UserRepository;
import com.artists_heaven.entities.user.UserRole;

@Service
public class ArtistService {

    @Autowired
    private ArtistRepository artistRepository;

    @Autowired
    private UserRepository userRepository;

    private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * Registers a new artist in the system.
     *
     * @param artist the artist object containing registration details
     * @return the registered artist after being saved in the database
     * @throws IllegalArgumentException if the email or artist name already exists
     */
    public Artist registerArtist(Artist artist) {
        // Check if the email is already registered in the user repository
        User userEmail = userRepository.findByEmail(artist.getEmail());
        if (userEmail != null) {
            throw new IllegalArgumentException("El correo electrónico ya está registrado.");
        }

        // Check if the artist name is already in use
        if (artistRepository.existsByArtistName(artist.getArtistName()) == true) {
            throw new IllegalArgumentException("Ya existe un usuario con ese nombre registrado");
        }

        // Set the username to match the artist name
        String username = artist.getArtistName();
        artist.setUsername(username);

        // Set the role of the artist
        artist.setRole(UserRole.ARTIST);

        // Encrypt the artist's password for secure storage
        artist.setPassword(passwordEncoder.encode(artist.getPassword()));

        // Save the artist in the database and return the saved entity
        return artistRepository.save(artist);
    }

}
