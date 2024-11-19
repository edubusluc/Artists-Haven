package com.artists_heaven.entities.artist;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.artists_heaven.entities.user.UserRepository;
import com.artists_heaven.entities.user.UserRole;

@Service
public class ArtistService {

    private final ArtistRepository artistRepository;
    private final UserRepository userRepository;

    private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public ArtistService(ArtistRepository artistRepository, UserRepository userRepository) {
        this.artistRepository = artistRepository;
        this.userRepository = userRepository;  // Inyectando el UserRepository
    }



    public Artist registerArtist(Artist artist) {
        // Verificar si el correo del artista ya está registrado
        if (userRepository.findByEmail(artist.getEmail()) != null) {
            throw new IllegalArgumentException("El correo electrónico ya está registrado.");
        }

        if (artistRepository.existsByArtistName(artist.getArtistName())) {
            throw new IllegalArgumentException("Ya hay un artista registrado con ese nombre");
        }

        // Establecer el rol de artista
        artist.setRole(UserRole.ARTIST);
        
        // Encriptar la contraseña
        artist.setPassword(passwordEncoder.encode(artist.getPassword()));

        // Guardar el artista en la base de datos
        return (Artist) artistRepository.save(artist);
    }
    
}
