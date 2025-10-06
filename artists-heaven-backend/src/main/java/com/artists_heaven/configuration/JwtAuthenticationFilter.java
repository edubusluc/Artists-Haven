package com.artists_heaven.configuration;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

     /**
     * This method is invoked once per request. It checks the JWT token, validates it, 
     * and sets the authentication information in the SecurityContext if valid.
     *
     * @param request the HTTP servlet request
     * @param response the HTTP servlet response
     * @param filterChain the filter chain to pass the request to the next filter
     * @throws ServletException if any servlet-specific error occurs
     * @throws IOException if an I/O error occurs during filtering
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String token = jwtTokenProvider.resolveToken(request);
        
        // Resolve the JWT token from the Authorization header
        if (token != null && jwtTokenProvider.validateToken(token)) {
            // Get authentication details from the token
            Authentication auth = jwtTokenProvider.getAuthentication(token);

             // Set the authentication in the SecurityContext
            SecurityContextHolder.getContext().setAuthentication(auth);
        }
        
        // Continue with the filter chain
        filterChain.doFilter(request, response);
    }
}