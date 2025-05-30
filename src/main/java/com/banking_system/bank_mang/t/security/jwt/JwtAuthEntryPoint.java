package com.banking_system.bank_mang.t.security.jwt;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Handles authentication failures (e.g., accessing a secured resource without a valid token).
 * It sends an HTTP 401 Unauthorized response to the client.
 */
@Component // Marks this as a Spring component
public class JwtAuthEntryPoint implements AuthenticationEntryPoint {

    /**
     * This method is invoked when a user tries to access a secured REST resource
     * without supplying any credentials or supplying invalid credentials.
     * @param request The HTTP request.
     * @param response The HTTP response.
     * @param authException The authentication exception that occurred.
     */
    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {
        // Send an HTTP 401 Unauthorized error response
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, authException.getMessage());
    }
}
