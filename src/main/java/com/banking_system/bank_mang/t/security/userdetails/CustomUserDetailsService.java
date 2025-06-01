package com.banking_system.bank_mang.t.security.userdetails;

import com.banking_system.bank_mang.t.entity.User;
import com.banking_system.bank_mang.t.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Value("${app.max-attempts}")
    private int maxFailedAttempts;

    @Value("${app.duration-minutes}")
    private int lockoutDurationMinutes;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        // --- Account Lockout Logic ---
        if (!user.isAccountNonLocked() && user.getLockTime() != null) {
            // Use LocalDateTime consistently for all comparisons
            if (user.getLockTime().plusMinutes(lockoutDurationMinutes).isAfter(LocalDateTime.now())) {
                // Account is still locked
                throw new UsernameNotFoundException("Account for user '" + username + "' is locked. Please try again after " + lockoutDurationMinutes + " minutes.");
            } else {
                // Lockout duration has passed, unlock the account
                user.setAccountNonLocked(true);
                user.setFailedLoginAttempts(0);
                user.setLockTime(null);
                userRepository.save(user); // Save the unlocked state
            }
        }

        if (user.getFailedLoginAttempts() > 0 && user.isAccountNonLocked()) {
            user.setFailedLoginAttempts(0);
            userRepository.save(user);
        }

        return new CustomUserDetails(user);
    }

    public void updateFailedAttempts(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        user.setFailedLoginAttempts(user.getFailedLoginAttempts() + 1);

        if (user.getFailedLoginAttempts() >= maxFailedAttempts) {
            user.setAccountNonLocked(false);
            // Use LocalDateTime.now() here
            user.setLockTime(LocalDateTime.now());
            System.out.println("User " + username + " locked due to too many failed attempts.");
        }
        userRepository.save(user);
    }
}