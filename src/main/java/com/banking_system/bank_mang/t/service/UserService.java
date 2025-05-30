package com.banking_system.bank_mang.t.service;
import com.banking_system.bank_mang.t.dto.UserResponse;
import com.banking_system.bank_mang.t.entity.Account;
import com.banking_system.bank_mang.t.entity.Role;
import com.banking_system.bank_mang.t.entity.User;
import com.banking_system.bank_mang.t.enums.AccountStatus;
import com.banking_system.bank_mang.t.enums.RoleName;
import com.banking_system.bank_mang.t.exceptions.ResourceNotFoundException;
import com.banking_system.bank_mang.t.repositories.AccountRepository;
import com.banking_system.bank_mang.t.repositories.RoleRepository;
import com.banking_system.bank_mang.t.repositories.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service for managing user-related operations (excluding authentication).
 * Used by Admin/Staff to view and manage users.
 */
@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final AccountRepository accountRepository; // To get account info for UserResponse
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, RoleRepository roleRepository, AccountRepository accountRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Creates a new user with specified roles.
     * This method is typically used by an ADMIN to create new staff/admin users.
     * For customer registration, use AuthService.registerUser().
     * @param user The User entity to save.
     * @param roleNames The set of role names to assign to the user.
     * @return The created User entity.
     * @throws ResourceNotFoundException if any specified role is not found.
     */
    public User createUser(User user, Set<RoleName> roleNames) {
        Set<Role> roles = roleNames.stream()
                .map(roleName -> roleRepository.findByName(roleName)
                        .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleName)))
                .collect(Collectors.toSet());
        user.setRoles(roles);
        user.setPassword(passwordEncoder.encode(user.getPassword())); // Ensure password is hashed
        return userRepository.save(user);
    }

    /**
     * Retrieves a user by their username.
     * @param username The username of the user.
     * @return The User entity.
     * @throws ResourceNotFoundException if the user is not found.
     */
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
    }

    /**
     * Retrieves a user by their ID.
     * @param userId The ID of the user.
     * @return The User entity.
     * @throws ResourceNotFoundException if the user is not found.
     */
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
    }

    /**
     * Retrieves all users in the system.
     * @return A list of UserResponse DTOs.
     */
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToUserResponse) // Map each User entity to UserResponse DTO
                .collect(Collectors.toList());
    }

    /**
     * Resets a user's password.
     * @param userId The ID of the user.
     * @param newPassword The new password (will be hashed).
     * @throws ResourceNotFoundException if the user is not found.
     */
    public void resetUserPassword(Long userId, String newPassword) {
        User user = getUserById(userId);
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    /**
     * Enables a user account.
     * @param userId The ID of the user.
     * @throws ResourceNotFoundException if the user is not found.
     */
    public void enableUser(Long userId) {
        User user = getUserById(userId);
        user.setEnabled(true);
        userRepository.save(user);
    }

    /**
     * Disables a user account.
     * @param userId The ID of the user.
     * @throws ResourceNotFoundException if the user is not found.
     */
    public void disableUser(Long userId) {
        User user = getUserById(userId);
        user.setEnabled(false);
        userRepository.save(user);
    }

    /**
     * Retrieves users who have registered but whose accounts are not yet approved.
     * This is typically for staff to review.
     * @return A list of UserResponse DTOs for users with pending accounts.
     */
    public List<UserResponse> getUsersWithPendingAccounts() {
        // Find all accounts that are pending approval
        List<Account> pendingAccounts = accountRepository.findByIsApprovedByStaffFalse();

        // Get unique users from these accounts and convert to DTOs
        return pendingAccounts.stream()
                .map(Account::getUser) // Get the User object from each Account
                .distinct() // Ensure unique users if a user could have multiple pending accounts
                .map(this::convertToUserResponse) // Convert to UserResponse DTO
                .collect(Collectors.toList());
    }

    // Helper method to convert User entity to UserResponse DTO
    private UserResponse convertToUserResponse(User user) {
        UserResponse dto = new UserResponse();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setEnabled(user.isEnabled());
        dto.setAccountNonLocked(user.isAccountNonLocked());
        dto.setRoles(user.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toList()));

        // Optionally, fetch and include primary account details
        Optional<Account> primaryAccount = accountRepository.findAllByUser_Id(user.getId()).stream()
                .filter(account -> account.getStatus() != AccountStatus.CLOSED) // Consider only active/frozen/pending
                .findFirst(); // Or define a "primary" account logic

        primaryAccount.ifPresent(account -> {
            dto.setAccountId(account.getId());
            dto.setAccountNumber(account.getAccountNumber());
            dto.setAccountStatus(account.getStatus());
        });

        return dto;
    }
}