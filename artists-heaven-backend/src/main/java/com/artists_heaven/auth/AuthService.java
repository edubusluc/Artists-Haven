package com.artists_heaven.auth;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.artists_heaven.entities.user.User;
import com.artists_heaven.entities.user.UserRepository;
import com.artists_heaven.entities.user.UserRole;
import com.artists_heaven.configuration.JwtTokenProvider;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder,
            JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public String login(String email, String password) {
        User user = userRepository.findByEmail(email);

        if (user == null) {
            throw new IllegalArgumentException("Credenciales inválidas");
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("Credenciales inválidas");
        }

        String role = "";
        
        if(user.getRole() == UserRole.USER){
            role = "ROLE_USER";
        }else if(user.getRole() == UserRole.ARTIST){
            role = "ROLE_ARTIST";
        }else{
            role = "ROLE_ADMIN";
        }


        Authentication authentication = new UsernamePasswordAuthenticationToken(
                user.getEmail(),
                password,
                AuthorityUtils.createAuthorityList(role) 
        );

        return jwtTokenProvider.generateToken(authentication);
    }

}
