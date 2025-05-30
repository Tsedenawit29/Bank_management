package com.banking_system.bank_mang.t.service;
import com.banking_system.bank_mang.t.dto.AuthResponse;
import com.banking_system.bank_mang.t.dto.LoginRequest;
import com.banking_system.bank_mang.t.dto.RegisterRequest;
import com.banking_system.bank_mang.t.entity.Role;
import com.banking_system.bank_mang.t.entity.User;
import com.banking_system.bank_mang.t.enums.RoleName;
import com.banking_system.bank_mang.t.exceptions.EmailAlreadyExistsException;
import com.banking_system.bank_mang.t.exceptions.InvalidCredentialsException;
import com.banking_system.bank_mang.t.exceptions.ResourceNotFoundException;
import com.banking_system.bank_mang.t.exceptions.UsernameAlreadyExistsException;
import com.banking_system.bank_mang.t.repositories.RoleRepository;
import com.banking_system.bank_mang.t.repositories.UserRepository;
import com.banking_system.bank_mang.t.security.jwt.JwtTokenProvider;
import com.banking_system.bank_mang.t.security.userdetails.CustomUserDetailsService; // To update failed attempts
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for handling user authentication (registration and login).
 * Interacts with UserRepository, RoleRepository, PasswordEncoder, and JwtTokenProvider.
 */
@Service // Marks this as a Spring service
@Transactional // Ensures methods are transactional (all or nothing operations)
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService customUserDetailsService; // For handling failed attempts

    // Constructor injection for all dependencies
    public AuthService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtTokenProvider jwtTokenProvider,
                       CustomUserDetailsService customUserDetailsService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.customUserDetailsService = customUserDetailsService;
    }

    /**
     * Registers a new user with the CUSTOMER role.
     * @param registerRequest DTO containing user registration details.
     * @throws UsernameAlreadyExistsException if the username is already taken.
     * @throws EmailAlreadyExistsException if the email is already registered.
     * @throws ResourceNotFoundException if the CUSTOMER role is not found (should not happen if roles are seeded).
     */
    public void registerUser(RegisterRequest registerRequest) {
        // Check if username already exists
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new UsernameAlreadyExistsException("Username is already taken!");
        }

        // Check if email already exists
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new EmailAlreadyExistsException("Email is already registered!");
        }

        // Create a new User entity
        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setEmail(registerRequest.getEmail());
        // Hash the password before saving
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));

        // Assign the default CUSTOMER role
        Role customerRole = roleRepository.findByName(RoleName.CUSTOMER)
                .orElseThrow(() -> new ResourceNotFoundException("CUSTOMER role not found. Please seed roles."));
        user.setRoles(Collections.singleton(customerRole)); // Assign only the customer role

        userRepository.save(user); // Save the new user to the database
    }

    /**
     * Authenticates a user and generates a JWT token.
     * @param loginRequest DTO containing login credentials.
     * @return AuthResponse containing the JWT token and user details.
     * @throws InvalidCredentialsException if authentication fails.
     */
    public AuthResponse loginUser(LoginRequest loginRequest) {
        try {
            // Authenticate the user using Spring Security's AuthenticationManager
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );

            // If authentication is successful, set the authentication object in the SecurityContext
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Generate JWT token
            String token = jwtTokenProvider.generateToken(authentication);

            // Get user's roles for the response
            List<String> roles = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority) // Get role strings (e.g., "ROLE_CUSTOMER")
                    .collect(Collectors.toList());

            return new AuthResponse(token, loginRequest.getUsername(), roles);

        } catch (BadCredentialsException ex) {
            // If authentication fails due to bad credentials, update failed attempts
            // Note: This needs to be carefully managed to avoid race conditions in high concurrency.
            // For this project, a simple update is sufficient.
            customUserDetailsService.updateFailedAttempts(loginRequest.getUsername());
            throw new InvalidCredentialsException("Invalid username or password.");
        } catch (Exception ex) {
            // Catch other exceptions during authentication (e.g., account locked)
            throw new InvalidCredentialsException(ex.getMessage());
        }
    }
}