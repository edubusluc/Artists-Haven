package com.artists_heaven.entities.user;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import jakarta.transaction.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class UserServiceTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired 
    private UserService userService;

    private static User userTest; 

    @BeforeAll
    public static void setup() {
        userTest = new User();
        userTest.setEmail("email@email.com");
        userTest.setFirstName("Lorem Ipsum");
        userTest.setLastName("Lorem Ipsum");
        userTest.setPassword("password1234");
        userTest.setRole(UserRole.USER);
    }

    @Test
    @Transactional
    public void testRegisterUserEmailError() {
        // Guardar el usuario de prueba en la base de datos
        userService.registerUser(userTest);

        List<User> users = userService.getAllUsers();
        User user_test = users.get(0);

        assertThat(user_test.getPassword()).isNotEqualTo("password1234");
        assertThat(user_test.getRole()).isEqualTo(UserRole.USER);
        
    }

    @Test
    @Transactional
    public void testGetAllUsers() {
        userRepository.save(userTest);
        List<User> allUsers = userService.getAllUsers();
        assertThat(allUsers.size()==1);
    }
}
