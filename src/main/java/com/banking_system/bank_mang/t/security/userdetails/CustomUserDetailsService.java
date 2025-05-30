package com.banking_system.bank_mang.t.security.userdetails;

import com.banking_system.bank_mang.t.dto.UserResponse;
import com.banking_system.bank_mang.t.entity.User;
import com.banking_system.bank_mang.t.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    // Inject account lockout configuration from application.properties
    @Value("${app.security.account-lockout.max-attempts}")
    private int maxFailedAttempts;

    @Value("${app.security.account-lockout.duration-minutes}")
    private int lockoutDurationMinutes;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        // --- Account Lockout Logic ---
        // 1. Check if the account is currently locked
        if (!user.isAccountNonLocked() && user.getLockTime() != null) {
            // Calculate if the lockout duration has passed
            if (user.getLockTime().plusMinutes(lockoutDurationMinutes).isAfter(LocalTime.from(LocalDateTime.now()))) {
                // Account is still locked, throw exception
                throw new UsernameNotFoundException("Account for user '" + username + "' is locked. Please try again after " + lockoutDurationMinutes + " minutes.");
            } else {
                // Lockout duration has passed, unlock the account
                user.setAccountNonLocked(true);
                user.setFailedLoginAttempts(0);
                user.setLockTime(null);
                userRepository.save(user); // Save the unlocked state
            }
        }

        // If authentication is successful (this method is called after successful password check),
        // reset failed attempts if the account was not previously locked.
        // This part is crucial for resetting attempts on successful login.
        if (user.getFailedLoginAttempts() > 0 && user.isAccountNonLocked()) {
            user.setFailedLoginAttempts(0);
            userRepository.save(user);
        }

        // Return our custom UserDetails implementation
        return new CustomUserDetails(user);
    }

    /**
     * Updates the failed login attempts for a user.
     * If attempts exceed maxFailedAttempts, the account is locked.
     * This method should be called by Spring Security's authentication failure handler.
     * (We'll implicitly handle this through the SecurityConfig and BadCredentialsException)
     */
    public void updateFailedAttempts(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        user.setFailedLoginAttempts(user.getFailedLoginAttempts() + 1);

        if (user.getFailedLoginAttempts() >= maxFailedAttempts) {
            user.setAccountNonLocked(false);
            user.setLockTime(LocalTime.from(LocalDateTime.now()));
            System.out.println("User " + username + " locked due to too many failed attempts.");
        }
        userRepository.save(user);
    }

}
