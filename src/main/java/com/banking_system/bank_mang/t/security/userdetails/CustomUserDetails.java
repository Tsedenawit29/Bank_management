package com.banking_system.bank_mang.t.security.userdetails;
import com.banking_system.bank_mang.t.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
public class CustomUserDetails implements UserDetails {

    private Long id;
    private String username;
    private String password; // Hashed password
    private String email;
    private boolean enabled; // Account enabled/disabled
    private boolean accountNonLocked; // Account locked/unlocked
    private Collection<? extends GrantedAuthority> authorities; // User's roles/permissions

    /**
     * Constructor that maps our User entity to Spring Security's UserDetails.
     * @param user The User entity from our database.
     */
    public CustomUserDetails(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.password = user.getPassword();
        this.email = user.getEmail();
        this.enabled = user.isEnabled();
        this.accountNonLocked = user.isAccountNonLocked();
        // Map User's roles to Spring Security's GrantedAuthority objects
        this.authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName().name())) // Roles must be prefixed with "ROLE_"
                .collect(Collectors.toList());
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    // --- Account Status Methods (from UserDetails interface) ---
    // These methods determine if an account is valid for authentication.

    @Override
    public boolean isAccountNonExpired() {
        // We don't have account expiration logic for now, so always true.
        // If you implement expiration, return user.isAccountNonExpired();
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        // Returns the actual locked status from our User entity.
        return accountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        // We don't have credential expiration logic for now, so always true.
        // If you implement password expiry, return user.isCredentialsNonExpired();
        return true;
    }

    @Override
    public boolean isEnabled() {
        // Returns the actual enabled status from our User entity.
        return enabled;
    }
}