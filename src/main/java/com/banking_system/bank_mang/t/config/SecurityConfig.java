package com.banking_system.bank_mang.t.config;

import com.banking_system.bank_mang.t.security.jwt.JwtAuthenticationFilter;
import com.banking_system.bank_mang.t.security.jwt.JwtAuthEntryPoint;
import com.banking_system.bank_mang.t.security.userdetails.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity; // For @PreAuthorize
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * Spring Security configuration for the banking system.
 * Defines security rules, integrates JWT authentication, and configures authorization.
 */
@Configuration // Marks this class as a source of bean definitions
@EnableWebSecurity // Enables Spring Security's web security features
@EnableMethodSecurity // Enables method-level security annotations like @PreAuthorize
public class SecurityConfig {
    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class); // ADD THIS LINE
    private final CustomUserDetailsService userDetailsService;
    private final JwtAuthEntryPoint authEntryPoint;
    private final PasswordEncoder passwordEncoder; // Injected from AppConfig

    // Constructor injection for dependencies
    public SecurityConfig(CustomUserDetailsService userDetailsService,
                          JwtAuthEntryPoint authEntryPoint,
                          PasswordEncoder passwordEncoder) {
        this.userDetailsService = userDetailsService;
        this.authEntryPoint = authEntryPoint;
        this.passwordEncoder = passwordEncoder;

        // Log the hash code of the PasswordEncoder instance
        logger.info("SecurityConfig PasswordEncoder hash: {}", passwordEncoder.hashCode());
    }

    /**
     * Defines the JwtAuthenticationFilter as a Spring-managed bean.
     * This filter will be responsible for validating JWT tokens in incoming requests.
     * @return A new instance of JwtAuthenticationFilter.
     */
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter();
    }

    /**
     * Exposes the AuthenticationManager bean.
     * The AuthenticationManager is used by AuthService to perform user authentication.
     * @param authenticationConfiguration Spring's AuthenticationConfiguration.
     * @return The AuthenticationManager.
     * @throws Exception if an error occurs during configuration.
     */

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    /**
     * Configures the security filter chain. This is where you define HTTP security rules.
     * @param http The HttpSecurity object to configure.
     * @return The built SecurityFilterChain.
     * @throws Exception if an error occurs during configuration.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Disable CSRF (Cross-Site Request Forgery) protection for stateless REST APIs.
                // CSRF is typically for session-based applications. JWT is stateless.
                .csrf(AbstractHttpConfigurer::disable)

                // Configure exception handling, specifically for authentication entry point.
                // When an unauthenticated user tries to access a protected resource, JwtAuthEntryPoint handles it.
                .exceptionHandling(exception -> exception.authenticationEntryPoint(authEntryPoint))

                // Configure session management to be stateless.
                // This means no session is created or used by Spring Security, which is essential for JWT.
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Define authorization rules for different HTTP requests
                .authorizeHttpRequests(authorize -> authorize
                        // Allow public access to authentication endpoints
                        .requestMatchers("/auth/**").permitAll()
                        // Allow public access to Swagger/OpenAPI documentation endpoints
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        // Require ADMIN role for /admin/** endpoints
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        // Require STAFF or ADMIN role for /staff/** endpoints
                        .requestMatchers("/staff/**").hasAnyRole("ADMIN", "STAFF")
                        // Require CUSTOMER, STAFF, or ADMIN role for /account/** endpoints
                        .requestMatchers("/account/**").hasAnyRole("CUSTOMER", "STAFF", "ADMIN")
                        // All other requests must be authenticated
                        .anyRequest().authenticated()
                );

        // Add our custom JWT authentication filter before Spring Security's default
        // UsernamePasswordAuthenticationFilter. This ensures our JWT validation runs first.
        http.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build(); // Build and return the configured SecurityFilterChain
    }
}