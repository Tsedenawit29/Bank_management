package com.banking_system.bank_mang.t.controller;

import com.banking_system.bank_mang.t.dto.AccountDetailsResponse;
import com.banking_system.bank_mang.t.dto.UserResponse;
import com.banking_system.bank_mang.t.service.AccountService;
import com.banking_system.bank_mang.t.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for bank staff operations.
 * Handles requests to /staff/** endpoints.
 * All methods require STAFF or ADMIN role.
 */
@RestController
@RequestMapping("/staff")
@PreAuthorize("hasAnyRole('STAFF', 'ADMIN')") // All methods in this controller require STAFF or ADMIN role
public class StaffController {

    private final UserService userService;
    private final AccountService accountService;

    public StaffController(UserService userService, AccountService accountService) {
        this.userService = userService;
        this.accountService = accountService;
    }

    /**
     * Allows staff to view users who have accounts pending approval.
     * Corresponds to: "View and approve customer registrations" (first part)
     * @return ResponseEntity with a list of UserResponse DTOs.
     */
    @GetMapping("/users/pending-accounts")
    public ResponseEntity<List<UserResponse>> getPendingAccountUsers() {
        List<UserResponse> users = userService.getUsersWithPendingAccounts();
        return ResponseEntity.ok(users);
    }

    /**
     * Allows staff to approve a pending bank account.
     * Corresponds to: "View and approve customer registrations" (second part: approving)
     * @param accountId The ID of the account to approve.
     * @return ResponseEntity indicating success.
     */
    @PutMapping("/account/{accountId}/approve")
    public ResponseEntity<String> approveAccount(@PathVariable Long accountId) {
        accountService.approveAccount(accountId);
        return ResponseEntity.ok("Account " + accountId + " approved successfully.");
    }

    /**
     * Allows staff to view all bank accounts in the system.
     * Corresponds to: "View and manage customer accounts" (view part)
     * @return ResponseEntity with a list of AccountDetailsResponse DTOs.
     */
    @GetMapping("/accounts/all")
    public ResponseEntity<List<AccountDetailsResponse>> getAllAccounts() {
        List<AccountDetailsResponse> accounts = accountService.getAllAccounts();
        return ResponseEntity.ok(accounts);
    }

    /**
     * Allows staff to freeze a specific bank account.
     * Corresponds to: "Freeze/unfreeze accounts"
     * @param accountId The ID of the account to freeze.
     * @return ResponseEntity indicating success.
     */
    @PutMapping("/account/{accountId}/freeze")
    public ResponseEntity<String> freezeAccount(@PathVariable Long accountId) {
        accountService.freezeAccount(accountId);
        return ResponseEntity.ok("Account " + accountId + " frozen successfully.");
    }

    /**
     * Allows staff to unfreeze a specific bank account.
     * Corresponds to: "Freeze/unfreeze accounts"
     * @param accountId The ID of the account to unfreeze.
     * @return ResponseEntity indicating success.
     */
    @PutMapping("/account/{accountId}/unfreeze")
    public ResponseEntity<String> unfreezeAccount(@PathVariable Long accountId) {
        accountService.unfreezeAccount(accountId);
        return ResponseEntity.ok("Account " + accountId + " unfrozen successfully.");
    }
}