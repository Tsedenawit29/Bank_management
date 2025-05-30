package com.banking_system.bank_mang.t.controller;

import com.banking_system.bank_mang.t.dto.AuthResponse;
import com.banking_system.bank_mang.t.dto.LoginRequest;
import com.banking_system.bank_mang.t.dto.RegisterRequest;
import com.banking_system.bank_mang.t.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth") // Base path
public class AuthController {

    private final AuthService authService;

    // Constructor injection for AuthService
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        authService.registerUser(registerRequest);
        return new ResponseEntity<>("User registered successfully!", HttpStatus.CREATED); // Return 201 Created status
    }

    /**
     * Handles user login.
     * @param loginRequest DTO containing login credentials.
     * @return ResponseEntity with AuthResponse (JWT token) on success.
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> loginUser(@Valid @RequestBody LoginRequest loginRequest) {
        AuthResponse authResponse = authService.loginUser(loginRequest);
        return ResponseEntity.ok(authResponse); // Return 200 OK with the AuthResponse DTO
    }
}