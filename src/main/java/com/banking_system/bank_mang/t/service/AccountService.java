package com.banking_system.bank_mang.t.service;
import  com.banking_system.bank_mang.t.dto.AccountCreationRequest;
import  com.banking_system.bank_mang.t.dto.AccountDetailsResponse;
import  com.banking_system.bank_mang.t.entity.Account;
import  com.banking_system.bank_mang.t.entity.Transaction;
import  com.banking_system.bank_mang.t.entity.User;
import  com.banking_system.bank_mang.t.enums.AccountStatus;
import  com.banking_system.bank_mang.t.enums.TransactionType;
import  com.banking_system.bank_mang.t.exceptions.AccountFrozenException;
import  com.banking_system.bank_mang.t.exceptions.AccountNotApprovedException;
import  com.banking_system.bank_mang.t.exceptions.InsufficientFundsException;
import  com.banking_system.bank_mang.t.exceptions.ResourceNotFoundException;
import  com.banking_system.bank_mang.t.repositories.AccountRepository;
import  com.banking_system.bank_mang.t.repositories.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for managing bank accounts and associated financial operations (deposit, withdraw, transfer).
 * Handles business logic and interacts with AccountRepository, UserRepository, and TransactionService.
 */
@Service
@Transactional // Ensures all methods are transactional
public class AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final TransactionService transactionService; // Inject TransactionService for logging

    public AccountService(AccountRepository accountRepository, UserRepository userRepository, TransactionService transactionService) {
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
        this.transactionService = transactionService;
    }

    /**
     * Creates a new bank account for a specified user.
     * The account is initially in PENDING_APPROVAL status.
     * @param userId The ID of the user for whom to create the account.
     * @param request DTO containing the desired account type.
     * @return AccountDetailsResponse DTO of the newly created account.
     * @throws ResourceNotFoundException if the user is not found.
     */
    public AccountDetailsResponse createAccount(Long userId, AccountCreationRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        Account account = new Account();
        account.setUser(user);
        account.setAccountNumber(generateUniqueAccountNumber()); // Generate unique number
        account.setType(request.getAccountType());
        account.setBalance(BigDecimal.ZERO); // New accounts start with 0 balance
        account.setStatus(AccountStatus.PENDING_APPROVAL); // Set initial status
        account.setApprovedByStaff(false); // Explicitly set to false

        Account savedAccount = accountRepository.save(account);
        return convertToDto(savedAccount);
    }

    /**
     * Retrieves account details for a specific user by their username.
     * Assumes a user has at least one account and returns the first one found.
     * @param username The username of the account holder.
     * @return AccountDetailsResponse DTO of the account.
     * @throws ResourceNotFoundException if no account is found for the user.
     */
    public AccountDetailsResponse getAccountDetailsByUsername(String username) {
        Account account = accountRepository.findByUser_Username(username)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found for user: " + username));
        return convertToDto(account);
    }

    /**
     * Retrieves account details by account ID.
     * @param accountId The ID of the account.
     * @return AccountDetailsResponse DTO of the account.
     * @throws ResourceNotFoundException if the account is not found.
     */
    public AccountDetailsResponse getAccountDetails(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with ID: " + accountId));
        return convertToDto(account);
    }

    /**
     * Approves a pending account.
     * @param accountId The ID of the account to approve.
     * @throws ResourceNotFoundException if the account is not found.
     * @throws IllegalStateException if the account is not in PENDING_APPROVAL status.
     */
    public void approveAccount(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with ID: " + accountId));

        if (account.getStatus() != AccountStatus.PENDING_APPROVAL) {
            throw new IllegalStateException("Account " + accountId + " is not in PENDING_APPROVAL status.");
        }

        account.setApprovedByStaff(true);
        account.setStatus(AccountStatus.ACTIVE); // Set to active once approved
        accountRepository.save(account);
    }

    /**
     * Deposits funds into an account.
     * @param username The username of the account holder.
     * @param amount The amount to deposit.
     * @throws ResourceNotFoundException if the account is not found.
     * @throws AccountFrozenException if the account is frozen.
     */
    public void deposit(String username, BigDecimal amount) {
        Account account = accountRepository.findByUser_Username(username)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found for user: " + username));

        if (account.getStatus() == AccountStatus.FROZEN) {
            throw new AccountFrozenException("Account is frozen. Cannot deposit funds.");
        }
        if (account.getStatus() == AccountStatus.PENDING_APPROVAL) {
            throw new AccountNotApprovedException("Account is not yet approved. Cannot deposit funds.");
        }

        account.setBalance(account.getBalance().add(amount));
        accountRepository.save(account);

        // Log the transaction
        transactionService.recordTransaction(
                new Transaction(null, TransactionType.DEPOSIT, amount, null, UUID.randomUUID().toString(), null, account)
        );
    }

    /**
     * Withdraws funds from an account.
     * @param username The username of the account holder.
     * @param amount The amount to withdraw.
     * @throws ResourceNotFoundException if the account is not found.
     * @throws AccountFrozenException if the account is frozen.
     * @throws InsufficientFundsException if the account balance is insufficient.
     */
    public void withdraw(String username, BigDecimal amount) {
        Account account = accountRepository.findByUser_Username(username)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found for user: " + username));

        if (account.getStatus() == AccountStatus.FROZEN) {
            throw new AccountFrozenException("Account is frozen. Cannot withdraw funds.");
        }
        if (account.getStatus() == AccountStatus.PENDING_APPROVAL) {
            throw new AccountNotApprovedException("Account is not yet approved. Cannot withdraw funds.");
        }
        if (account.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException("Insufficient funds in account " + account.getAccountNumber());
        }

        account.setBalance(account.getBalance().subtract(amount));
        accountRepository.save(account);

        // Log the transaction
        transactionService.recordTransaction(
                new Transaction(null, TransactionType.WITHDRAWAL, amount, null, UUID.randomUUID().toString(), account, null)
        );
    }

    /**
     * Transfers funds between two accounts.
     * @param sourceUsername The username of the source account holder.
     * @param destinationAccountNumber The account number of the destination account.
     * @param amount The amount to transfer.
     * @throws ResourceNotFoundException if source/destination account not found.
     * @throws AccountFrozenException if source/destination account is frozen.
     * @throws AccountNotApprovedException if source/destination account is not approved.
     * @throws InsufficientFundsException if source account has insufficient funds.
     * @throws IllegalArgumentException if source and destination accounts are the same.
     */
    public void transfer(String sourceUsername, String destinationAccountNumber, BigDecimal amount) {
        Account sourceAccount = accountRepository.findByUser_Username(sourceUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Source account not found for user: " + sourceUsername));

        Account destinationAccount = accountRepository.findByAccountNumber(destinationAccountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Destination account not found with number: " + destinationAccountNumber));

        if (sourceAccount.getId().equals(destinationAccount.getId())) {
            throw new IllegalArgumentException("Cannot transfer funds to the same account.");
        }

        if (sourceAccount.getStatus() == AccountStatus.FROZEN) {
            throw new AccountFrozenException("Source account is frozen. Cannot transfer funds.");
        }
        if (sourceAccount.getStatus() == AccountStatus.PENDING_APPROVAL) {
            throw new AccountNotApprovedException("Source account is not yet approved. Cannot transfer funds.");
        }
        if (destinationAccount.getStatus() == AccountStatus.FROZEN) {
            throw new AccountFrozenException("Destination account is frozen. Cannot transfer funds.");
        }
        if (destinationAccount.getStatus() == AccountStatus.PENDING_APPROVAL) {
            throw new AccountNotApprovedException("Destination account is not yet approved. Cannot transfer funds.");
        }

        if (sourceAccount.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException("Insufficient funds in source account " + sourceAccount.getAccountNumber());
        }

        // Perform the transfer
        sourceAccount.setBalance(sourceAccount.getBalance().subtract(amount));
        destinationAccount.setBalance(destinationAccount.getBalance().add(amount));

        accountRepository.save(sourceAccount);
        accountRepository.save(destinationAccount);

        // Log the transaction (double-entry logging)
        String referenceId = UUID.randomUUID().toString(); // Unique ID for this transfer operation

        // Debit transaction for source account
        transactionService.recordTransaction(
                new Transaction(null, TransactionType.TRANSFER, amount.negate(), null, referenceId, sourceAccount, destinationAccount) // Amount negative for debit
        );
        // Credit transaction for destination account
        transactionService.recordTransaction(
                new Transaction(null, TransactionType.TRANSFER, amount, null, referenceId, sourceAccount, destinationAccount)
        );
    }

    /**
     * Freezes a bank account.
     * @param accountId The ID of the account to freeze.
     * @throws ResourceNotFoundException if the account is not found.
     * @throws IllegalStateException if the account is already frozen or closed.
     */
    public void freezeAccount(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with ID: " + accountId));

        if (account.getStatus() == AccountStatus.FROZEN) {
            throw new IllegalStateException("Account " + accountId + " is already frozen.");
        }
        if (account.getStatus() == AccountStatus.CLOSED) {
            throw new IllegalStateException("Account " + accountId + " is closed and cannot be frozen.");
        }
        if (account.getStatus() == AccountStatus.PENDING_APPROVAL) {
            throw new IllegalStateException("Account " + accountId + " is pending approval and cannot be frozen.");
        }

        account.setStatus(AccountStatus.FROZEN);
        accountRepository.save(account);
    }

    /**
     * Unfreezes a bank account.
     * @param accountId The ID of the account to unfreeze.
     * @throws ResourceNotFoundException if the account is not found.
     * @throws IllegalStateException if the account is not frozen.
     */
    public void unfreezeAccount(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with ID: " + accountId));

        if (account.getStatus() != AccountStatus.FROZEN) {
            throw new IllegalStateException("Account " + accountId + " is not frozen.");
        }

        account.setStatus(AccountStatus.ACTIVE); // Return to active status
        accountRepository.save(account);
    }

    /**
     * Retrieves all accounts in the system (for staff/admin).
     * @return A list of AccountDetailsResponse DTOs.
     */
    public List<AccountDetailsResponse> getAllAccounts() {
        return accountRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // Helper method to generate a unique 10-digit account number
    private String generateUniqueAccountNumber() {
        String accountNumber;
        do {
            // Generate a random 10-digit number
            accountNumber = String.format("%010d", (long) (Math.random() * 10_000_000_000L));
        } while (accountRepository.findByAccountNumber(accountNumber).isPresent()); // Ensure uniqueness
        return accountNumber;
    }

    // Helper method to convert Account entity to AccountDetailsResponse DTO
    private AccountDetailsResponse convertToDto(Account account) {
        AccountDetailsResponse dto = new AccountDetailsResponse();
        dto.setId(account.getId());
        dto.setAccountNumber(account.getAccountNumber());
        dto.setBalance(account.getBalance());
        dto.setAccountType(account.getType());
        dto.setStatus(account.getStatus());
        dto.setUserId(account.getUser().getId());
        dto.setUsername(account.getUser().getUsername()); // Include username for convenience
        return dto;
    }
}