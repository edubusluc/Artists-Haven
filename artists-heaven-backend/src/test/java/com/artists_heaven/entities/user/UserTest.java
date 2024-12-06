package com.artists_heaven.entities.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;

public class UserTest {

    @Test
    public void testGetAuthoritiesAsArtist() {
        User user = new User();
        user.setRole(UserRole.ARTIST);

        Collection<? extends GrantedAuthority> authorities = user.getAuthorities();

        assertEquals(1, authorities.size());
        assertTrue(authorities.stream().anyMatch(a -> "ROLE_ARTIST".equals(a.getAuthority())));
    }

    @Test
    public void testGetAuthoritiesAsUser() {
        User user = new User();
        user.setRole(UserRole.USER);

        Collection<? extends GrantedAuthority> authorities = user.getAuthorities();

        assertEquals(1, authorities.size());
        assertTrue(authorities.stream().anyMatch(a -> "ROLE_USER".equals(a.getAuthority())));
    }
    
}
