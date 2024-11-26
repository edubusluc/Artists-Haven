package com.artists_heaven.auth;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.artists_heaven.entities.user.User;
import com.artists_heaven.entities.user.UserRepository;
import com.artists_heaven.entities.user.UserRole;

import jakarta.transaction.Transactional;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class AuthServiceTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    private static User userTest; 


    @BeforeAll
    public static void setup() {
        userTest = new User();
        userTest.setEmail("email@email.com");
        userTest.setFirstName("Lorem Ipsum");
        userTest.setLastName("Lorem Ipsum");
        userTest.setPassword(new BCryptPasswordEncoder().encode("password1234"));
        userTest.setRole(UserRole.USER);

        
    }

    @Test
    @Transactional
    public void testLogin_withValidCredentials_returnsJwt() {
        userRepository.save(userTest);
        String token = authService.login("email@email.com", "password1234");
        assertNotNull(token, "El token no debería ser nulo");
    }

    @Test
    @Transactional
    public void testLogin_withNonValidCredentials() {
        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            authService.login("email@email.com", "password1234");
        });
        Assertions.assertTrue(exception.getMessage().contains("Credenciales inválidas"));
        
    }




    
}
