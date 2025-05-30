package com.banking_system.bank_mang.t.controller;

import com.banking_system.bank_mang.t.dto.*;
import com.banking_system.bank_mang.t.service.AccountService;
import com.banking_system.bank_mang.t.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for customer-specific account operations and general account management.
 * Handles requests to /account/** endpoints.
 */
@RestController
@RequestMapping("/account")
// Class-level authorization: All methods in this controller require at least CUSTOMER, STAFF, or ADMIN role.
// More specific method-level authorization can be applied with @PreAuthorize.
@PreAuthorize("hasAnyRole('CUSTOMER', 'STAFF', 'ADMIN')")
public class AccountController {

    private final AccountService accountService;
    private final TransactionService transactionService;

    public AccountController(AccountService accountService, TransactionService transactionService) {
        this.accountService = accountService;
        this.transactionService = transactionService;
    }

    // Helper method to get the username of the currently authenticated user
    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName(); // Returns the username (principal)
    }

    /**
     * Allows a customer to view their own account details.
     * @return ResponseEntity with AccountDetailsResponse.
     */
    @GetMapping("/me")
    @PreAuthorize("hasRole('CUSTOMER')") // Only CUSTOMER can view their own account details
    public ResponseEntity<AccountDetailsResponse> getMyAccountDetails() {
        String username = getCurrentUsername();
        AccountDetailsResponse account = accountService.getAccountDetailsByUsername(username);
        return ResponseEntity.ok(account);
    }

    /**
     * Allows a customer to deposit funds into their account.
     * @param depositRequest DTO containing the amount to deposit.
     * @return ResponseEntity indicating success.
     */
    @PostMapping("/deposit")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<String> depositFunds(@Valid @RequestBody DepositRequest depositRequest) {
        String username = getCurrentUsername();
        accountService.deposit(username, depositRequest.getAmount());
        return new ResponseEntity<>("Deposit successful.", HttpStatus.OK);
    }

    /**
     * Allows a customer to withdraw funds from their account.
     * @param withdrawRequest DTO containing the amount to withdraw.
     * @return ResponseEntity indicating success.
     */
    @PostMapping("/withdraw")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<String> withdrawFunds(@Valid @RequestBody WithdrawRequest withdrawRequest) {
        String username = getCurrentUsername();
        accountService.withdraw(username, withdrawRequest.getAmount());
        return new ResponseEntity<>("Withdrawal successful.", HttpStatus.OK);
    }

    /**
     * Allows a customer to transfer money to another account.
     * @param transferRequest DTO containing destination account number and amount.
     * @return ResponseEntity indicating success.
     */
    @PostMapping("/transfer")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<String> transferFunds(@Valid @RequestBody TransferRequest transferRequest) {
        String sourceUsername = getCurrentUsername();
        accountService.transfer(sourceUsername, transferRequest.getDestinationAccountNumber(), transferRequest.getAmount());
        return new ResponseEntity<>("Transfer successful.", HttpStatus.OK);
    }

    /**
     * Allows a customer to view their transaction history.
     * @param startDate Optional start date for filtering (YYYY-MM-DD).
     * @param endDate Optional end date for filtering (YYYY-MM-DD).
     * @return ResponseEntity with a list of TransactionResponse DTOs.
     */
    @GetMapping("/transactions")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<List<TransactionResponse>> getTransactionHistory(
            @RequestParam(required = false) String startDate, // @RequestParam for query parameters
            @RequestParam(required = false) String endDate) {
        String username = getCurrentUsername();
        List<TransactionResponse> transactions = transactionService.getTransactionsForUser(username, startDate, endDate);
        return ResponseEntity.ok(transactions);
    }

    /**
     * Allows Staff or Admin to create a new account for a specific user.
     * @param userId The ID of the user for whom to create the account.
     * @param request DTO containing the desired account type.
     * @return ResponseEntity with AccountDetailsResponse of the new account.
     */
    @PostMapping("/create/{userId}")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')") // Only STAFF or ADMIN can create accounts for others
    public ResponseEntity<AccountDetailsResponse> createAccountForUser(
            @PathVariable Long userId, // @PathVariable to extract ID from URL path
            @Valid @RequestBody AccountCreationRequest request) {
        AccountDetailsResponse newAccount = accountService.createAccount(userId, request);
        return new ResponseEntity<>(newAccount, HttpStatus.CREATED);
    }
}