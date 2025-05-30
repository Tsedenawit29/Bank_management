package com.banking_system.bank_mang.t.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String jwtToken;
    private String username;
    private List<String> roles; // List of roles (e.g., ["ROLE_CUSTOMER", "ROLE_ADMIN"])
    private String tokenType = "Bearer"; // Standard JWT token type

    public AuthResponse(String jwtToken, String username, List<String> roles) {
        this.jwtToken = jwtToken;
        this.username = username;
        this.roles = roles;
    }}
