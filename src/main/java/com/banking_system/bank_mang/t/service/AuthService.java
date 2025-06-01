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
import com.banking_system.bank_mang.t.security.userdetails.CustomUserDetailsService;
import org.slf4j.Logger; // Import Logger
import org.slf4j.LoggerFactory; // Import LoggerFactory
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class); // ADD THIS LINE

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService customUserDetailsService;

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

        // Log the hash code of the PasswordEncoder instance to confirm it's the same everywhere
        logger.info("AuthService PasswordEncoder hash: {}", passwordEncoder.hashCode());
    }

    public void registerUser(RegisterRequest registerRequest) {
        logger.info("Registering user: {}", registerRequest.getUsername());

        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            logger.warn("Registration failed: Username {} already taken.", registerRequest.getUsername());
            throw new UsernameAlreadyExistsException("Username is already taken!");
        }

        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            logger.warn("Registration failed: Email {} already registered.", registerRequest.getEmail());
            throw new EmailAlreadyExistsException("Email is already registered!");
        }

        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setEmail(registerRequest.getEmail());
        // LOGGING FOR DEBUGGING: Shows the exact password string and its length before hashing
        logger.debug("Registering password (quoted): '{}' (length: {})", registerRequest.getPassword(), registerRequest.getPassword().length());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        logger.debug("Password encoded successfully for user: {}", registerRequest.getUsername());

        Role customerRole = roleRepository.findByName(RoleName.CUSTOMER)
                .orElseThrow(() -> new ResourceNotFoundException("CUSTOMER role not found. Please seed roles."));
        user.setRoles(Collections.singleton(customerRole));

        userRepository.save(user);
        logger.info("User {} registered successfully.", registerRequest.getUsername());
    }

    public AuthResponse loginUser(LoginRequest loginRequest) {
        logger.info("Attempting login for user: {}", loginRequest.getUsername());
        try {
            // LOGGING FOR DEBUGGING: Shows the exact password string and its length being used for login
            logger.debug("Login attempt with username: '{}', password (quoted): '{}' (length: {})",
                    loginRequest.getUsername(), loginRequest.getPassword(), loginRequest.getPassword().length());

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            String token = jwtTokenProvider.generateToken(authentication);

            List<String> roles = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());

            logger.info("User {} logged in successfully. Roles: {}", loginRequest.getUsername(), roles);
            return new AuthResponse(token, loginRequest.getUsername(), roles);

        } catch (BadCredentialsException ex) {
            logger.warn("Login failed for user {}: Bad credentials. Error: {}", loginRequest.getUsername(), ex.getMessage());
            customUserDetailsService.updateFailedAttempts(loginRequest.getUsername());
            throw new InvalidCredentialsException("Invalid username or password.");
        } catch (UsernameNotFoundException ex) {
            logger.warn("Login failed for user {}: Account status issue. Error: {}", loginRequest.getUsername(), ex.getMessage());
            throw new InvalidCredentialsException(ex.getMessage());
        } catch (Exception ex) {
            logger.error("An unexpected error occurred during login for user {}: {}", loginRequest.getUsername(), ex.getMessage(), ex);
            throw new InvalidCredentialsException("An unexpected error occurred during login.");
        }
    }
}