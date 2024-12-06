package com.artists_heaven.entities.user;

import java.security.Principal;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.artists_heaven.entities.artist.Artist;

import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.security.core.Authentication;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;

    public UserController(UserService userService, UserRepository userRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
    }

    @GetMapping("/list")
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    @PostMapping("/register")
    public ResponseEntity<User> registerUser(@RequestBody User user) {
        try {
            User registeredUser = userService.registerUser(user);
            return new ResponseEntity<>(registeredUser, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getUserProfile(Principal principal) {
    
        Authentication authentication = (Authentication) principal;
    
        Object principalUser = authentication.getPrincipal();

        
        if (principalUser instanceof User) {
            User user = (User) principalUser;
         
            // Crear un DTO (Data Transfer Object) para la respuesta
            UserProfileDTO userProfileDTO = new UserProfileDTO(user);
    
            // Si el usuario es un artista, podemos agregar más detalles
            if (user instanceof Artist) {
                Artist artist = (Artist) user;
                userProfileDTO.setArtistName(artist.getArtistName());
            }
    
            return ResponseEntity.ok(userProfileDTO); // Devolver los datos del perfil en formato JSON
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuario no autenticado");
        }
    }

    @PutMapping("/profile/edit")
    public ResponseEntity<?> updateUserProfile(@RequestBody UserProfileDTO userProfileDTO, Principal principal) {
        if (!(principal instanceof Authentication)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuario no autenticado");
        }
    
        Authentication authentication = (Authentication) principal;
        User user = (User) authentication.getPrincipal();
    
        user.setFirstName(userProfileDTO.getFirstName());
        user.setLastName(userProfileDTO.getLastName());
        if (user instanceof Artist && userProfileDTO.getArtistName() != null) {
            ((Artist) user).setArtistName(userProfileDTO.getArtistName());
        }
    
        userRepository.save(user);
        return ResponseEntity.ok(Map.of("message", "Perfil actualizado correctamente"));
    }
}
