package com.banking_system.bank_mang.t.controller;

import com.banking_system.bank_mang.t.dto.TransactionResponse; // New import
import com.banking_system.bank_mang.t.dto.UserResponse;
import com.banking_system.bank_mang.t.service.AccountService;
import com.banking_system.bank_mang.t.service.TransactionService; // New import
import com.banking_system.bank_mang.t.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for administrative operations.
 * Handles requests to /admin/** endpoints.
 * All methods require the ADMIN role.
 */
@RestController
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')") // All methods in this controller require ADMIN role
public class AdminController {

    private final UserService userService;
    private final AccountService accountService;
    private final TransactionService transactionService; // New dependency

    public AdminController(UserService userService, AccountService accountService, TransactionService transactionService) {
        this.userService = userService;
        this.accountService = accountService;
        this.transactionService = transactionService; // Inject new dependency
    }

    /**
     * Allows admin to view all registered users.
     * Corresponds to: "Full control over users"
     * @return ResponseEntity with a list of UserResponse DTOs.
     */
    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    /**
     * Allows admin to reset a user's password.
     * Corresponds to: "Reset passwords"
     * @param userId The ID of the user whose password to reset.
     * @param newPassword The new password string.
     * @return ResponseEntity indicating success.
     */
    @PutMapping("/user/{userId}/reset-password")
    public ResponseEntity<String> resetUserPassword(@PathVariable Long userId, @RequestParam String newPassword) {
        userService.resetUserPassword(userId, newPassword);
        return ResponseEntity.ok("Password reset successfully for user ID: " + userId);
    }

    /**
     * Allows admin to freeze a specific bank account.
     * Corresponds to: "Full control over... accounts" (Admin can also do what Staff can)
     * @param accountId The ID of the account to freeze.
     * @return ResponseEntity indicating success.
     */
    @PutMapping("/account/{accountId}/freeze")
    public ResponseEntity<String> freezeAccount(@PathVariable Long accountId) {
        accountService.freezeAccount(accountId);
        return ResponseEntity.ok("Account " + accountId + " frozen successfully.");
    }

    /**
     * Allows admin to unfreeze a specific bank account.
     * Corresponds to: "Full control over... accounts"
     * @param accountId The ID of the account to unfreeze.
     * @return ResponseEntity indicating success.
     */
    @PutMapping("/account/{accountId}/unfreeze")
    public ResponseEntity<String> unfreezeAccount(@PathVariable Long accountId) {
        accountService.unfreezeAccount(accountId);
        return ResponseEntity.ok("Account " + accountId + " unfrozen successfully.");
    }

    /**
     * Allows admin to enable a user account.
     * Corresponds to: "Override access"
     * @param userId The ID of the user to enable.
     * @return ResponseEntity indicating success.
     */
    @PutMapping("/user/{userId}/enable")
    public ResponseEntity<String> enableUser(@PathVariable Long userId) {
        userService.enableUser(userId);
        return ResponseEntity.ok("User " + userId + " enabled successfully.");
    }

    /**
     * Allows admin to disable a user account.
     * Corresponds to: "Override access"
     * @param userId The ID of the user to disable.
     * @return ResponseEntity indicating success.
     */
    @PutMapping("/user/{userId}/disable")
    public ResponseEntity<String> disableUser(@PathVariable Long userId) {
        userService.disableUser(userId);
        return ResponseEntity.ok("User " + userId + " disabled successfully.");
    }

    /**
     * Allows admin to access system audit reports by viewing all transactions.
     * Corresponds to: "Access audit reports"
     * @param startDate Optional start date for filtering (YYYY-MM-DD).
     * @param endDate Optional end date for filtering (YYYY-MM-DD).
     * @return ResponseEntity with a list of TransactionResponse DTOs.
     *
     * IMPORTANT: This method assumes a `getAllTransactions()` or similar flexible query
     * method exists in `TransactionService` that can fetch all transactions (or filtered ones)
     * not just for a specific user. You will need to implement that in `TransactionService`.
     */
    @GetMapping("/transactions/audit")
    public ResponseEntity<List<TransactionResponse>> getAllTransactions(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        // You'll need to implement a method in TransactionService to get all transactions
        // or transactions based on a date range without a specific user.
        // For example: transactionService.getAllTransactions(startDate, endDate);
        // For now, let's mock it or assume a new method in TransactionService.
        // As a temporary placeholder, reusing the existing user-specific method is not appropriate here.
        // Assuming a new method like:
        List<TransactionResponse> transactions = transactionService.getAllTransactions(startDate, endDate); // This method needs to be added to TransactionService
        return ResponseEntity.ok(transactions);
    }
}