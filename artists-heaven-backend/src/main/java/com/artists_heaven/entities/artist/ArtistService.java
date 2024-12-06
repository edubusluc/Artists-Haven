package com.artists_heaven.entities.artist;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.artists_heaven.entities.user.User;
import com.artists_heaven.entities.user.UserRepository;
import com.artists_heaven.entities.user.UserRole;

@Service
public class ArtistService {

    private final ArtistRepository artistRepository;
    private final UserRepository userRepository;

    private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public ArtistService(ArtistRepository artistRepository, UserRepository userRepository) {
        this.artistRepository = artistRepository;
        this.userRepository = userRepository;
    }



    public Artist registerArtist(Artist artist) {        
        User userEmail = userRepository.findByEmail(artist.getEmail());
        if(userEmail != null){
            throw new IllegalArgumentException("El correo electrónico ya está registrado.");
        }

        if(artistRepository.existsByArtistName(artist.getArtistName()) == true){
            throw new IllegalArgumentException("Ya existe un usuario con ese nombre registrado");
        }

        String username = artist.getArtistName();
        artist.setUsername(username);


        // Establecer el rol de artista
        artist.setRole(UserRole.ARTIST);
        
        // Encriptar la contraseña
        artist.setPassword(passwordEncoder.encode(artist.getPassword()));

        // Guardar el artista en la base de datos
        return (Artist) artistRepository.save(artist);
    }
    
}
