package com.banking_system.bank_mang.t.controller;

import com.banking_system.bank_mang.t.dto.TransactionResponse;
import com.banking_system.bank_mang.t.dto.UserResponse;
import com.banking_system.bank_mang.t.service.AccountService;
import com.banking_system.bank_mang.t.service.TransactionService;
import com.banking_system.bank_mang.t.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.banking_system.bank_mang.t.dto.PasswordResetRequest; // <--- NEW IMPORT

import java.util.List;

@RestController
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserService userService;
    private final AccountService accountService;
    private final TransactionService transactionService;

    public AdminController(UserService userService, AccountService accountService, TransactionService transactionService) {
        this.userService = userService;
        this.accountService = accountService;
        this.transactionService = transactionService;
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    /**
     * Allows admin to reset a user's password.
     * Corresponds to: "Reset passwords"
     * @param userId The ID of the user whose password to reset.
     * @param request DTO containing the new password. <--- CHANGED
     * @return ResponseEntity indicating success.
     */
    @PutMapping("/user/{userId}/reset-password")
    public ResponseEntity<String> resetUserPassword(
            @PathVariable Long userId,
            @RequestBody PasswordResetRequest request) { // <--- CHANGED TO @RequestBody AND DTO
        userService.resetUserPassword(userId, request.getNewPassword()); // <--- ACCESS NEW PASSWORD VIA DTO
        return ResponseEntity.ok("Password reset successfully for user ID: " + userId);
    }

    @PutMapping("/account/{accountId}/freeze")
    public ResponseEntity<String> freezeAccount(@PathVariable Long accountId) {
        accountService.freezeAccount(accountId);
        return ResponseEntity.ok("Account " + accountId + " frozen successfully.");
    }

    @PutMapping("/account/{accountId}/unfreeze")
    public ResponseEntity<String> unfreezeAccount(@PathVariable Long accountId) {
        accountService.unfreezeAccount(accountId);
        return ResponseEntity.ok("Account " + accountId + " unfrozen successfully.");
    }

    @PutMapping("/user/{userId}/enable")
    public ResponseEntity<String> enableUser(@PathVariable Long userId) {
        userService.enableUser(userId);
        return ResponseEntity.ok("User " + userId + " enabled successfully.");
    }

    @PutMapping("/user/{userId}/disable")
    public ResponseEntity<String> disableUser(@PathVariable Long userId) {
        userService.disableUser(userId);
        return ResponseEntity.ok("User " + userId + " disabled successfully.");
    }

    @GetMapping("/transactions/audit")
    public ResponseEntity<List<TransactionResponse>> getAllTransactions(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        List<TransactionResponse> transactions = transactionService.getAllTransactions(startDate, endDate);
        return ResponseEntity.ok(transactions);
    }
}