package com.banking_system.bank_mang.t.security.jwt;

import com.banking_system.bank_mang.t.security.userdetails.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.couchbase.CouchbaseProperties;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Custom filter that intercepts incoming requests to validate JWT tokens.
 * It runs once per request to ensure token validity before processing.
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    @Autowired
    private JwtTokenProvider tokenProvider;
    @Autowired
    private CustomUserDetailsService customUserDetailsService;
    /**
     * Performing the  actual filtering logic for each request
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            // 1. Get JWT token from the request header
            String token = getJwtFromRequest(request);

            // 2. Validate the token and set authentication in Spring Security context
            if (StringUtils.hasText(token) && tokenProvider.validateToken(token)) {
                // Extract username from the token
                String username = tokenProvider.getUsernameFromJwt(token);

                // Load user details (including roles) from the database
                UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);

                // Create an authentication object
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());

                // Set additional details about the request (e.g., remote IP address)
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Set the authentication object in Spring Security's SecurityContext
                // This makes the user authenticated for the current request
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception ex) {
            // Log any errors during JWT processing
            System.err.println("Could not set user authentication in security context: " + ex.getMessage());
            // You might want to throw a specific exception here that your GlobalExceptionHandler can catch
        }

        // Continue with the filter chain
        filterChain.doFilter(request, response);
    }

    /**
     * Extracts the JWT token from the Authorization header (Bearer token).
     * @param request The HTTP request.
     * @return The JWT token string, or null if not found.
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        // Check if Authorization header exists and starts with "Bearer "
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // Extract the token part
        }
        return null;
    }
}
