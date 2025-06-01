package com.banking_system.bank_mang.t.service;

import com.banking_system.bank_mang.t.dto.TransactionResponse;
import com.banking_system.bank_mang.t.entity.Account;
import com.banking_system.bank_mang.t.entity.Transaction;
import com.banking_system.bank_mang.t.exceptions.ResourceNotFoundException;
import com.banking_system.bank_mang.t.repositories.AccountRepository;
import com.banking_system.bank_mang.t.repositories.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList; // No longer strictly needed if using .stream().distinct() properly
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing transaction-related operations, including logging and retrieving history.
 */
@Service
@Transactional
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository; // To find account by username

    public TransactionService(TransactionRepository transactionRepository, AccountRepository accountRepository) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
    }

    /**
     * Records a new transaction in the database.
     * This method is typically called by AccountService after a financial operation.
     * @param transaction The Transaction entity to save.
     * @return The saved Transaction entity.
     */
    public Transaction recordTransaction(Transaction transaction) {
        // The Transaction entity itself should now handle the timestamp default,
        // so no explicit setting needed here unless you want a fail-safe.
        // If 'transaction.getTimestamp() == null' check was here before, it can now be removed
        // if your Transaction entity's default initialization is reliable.
        return transactionRepository.save(transaction);
    }

    /**
     * Retrieves transaction history for a specific user's account, with optional date filtering.
     * @param username The username of the user whose transactions are to be retrieved.
     * @param startDateString Optional start date string (e.g., "YYYY-MM-DD").
     * @param endDateString Optional end date string (e.g., "YYYY-MM-DD").
     * @return A list of TransactionResponse DTOs.
     * @throws ResourceNotFoundException if the account for the user is not found.
     * @throws IllegalArgumentException if date strings are invalid.
     */
    public List<TransactionResponse> getTransactionsForUser(String username, String startDateString, String endDateString) {
        Account account = accountRepository.findByUser_Username(username)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found for user: " + username));

        Long accountId = account.getId();
        List<Transaction> transactions = new ArrayList<>(); // Initialize to collect results
        LocalDateTime startDate = null;
        LocalDateTime endDate = null;

        // Parse start date
        if (startDateString != null && !startDateString.isEmpty()) {
            try {
                startDate = LocalDateTime.parse(startDateString + "T00:00:00"); // Start of the day
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException("Invalid start date format. Use YYYY-MM-DD.");
            }
        }

        // Parse end date
        if (endDateString != null && !endDateString.isEmpty()) {
            try {
                endDate = LocalDateTime.parse(endDateString + "T23:59:59"); // End of the day
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException("Invalid end date format. Use YYYY-MM-DD.");
            }
        }

        // Fetch transactions based on source and destination accounts
        if (startDate != null && endDate != null) {
            transactions.addAll(transactionRepository.findBySourceAccountIdAndTimestampBetweenOrderByTimestampDesc(accountId, startDate, endDate));
            transactions.addAll(transactionRepository.findByDestinationAccountIdAndTimestampBetweenOrderByTimestampDesc(accountId, startDate, endDate));
        } else if (startDate != null) {
            transactions.addAll(transactionRepository.findBySourceAccountIdAndTimestampAfterOrderByTimestampDesc(accountId, startDate)); // Added OrderBy for consistency
            transactions.addAll(transactionRepository.findByDestinationAccountIdAndTimestampAfterOrderByTimestampDesc(accountId, startDate)); // Added OrderBy for consistency
        } else if (endDate != null) {
            transactions.addAll(transactionRepository.findBySourceAccountIdAndTimestampBeforeOrderByTimestampDesc(accountId, endDate)); // Added OrderBy for consistency
            transactions.addAll(transactionRepository.findByDestinationAccountIdAndTimestampBeforeOrderByTimestampDesc(accountId, endDate)); // Added OrderBy for consistency
        } else {
            transactions.addAll(transactionRepository.findBySourceAccountId(accountId));
            transactions.addAll(transactionRepository.findByDestinationAccountId(accountId));
        }

        // Using a Set for distinctness if multiple queries return the same transaction object
        // Or simply `distinct()` on the stream.
        return transactions.stream()
                .distinct() // Ensure unique transactions
                .sorted((t1, t2) -> t2.getTimestamp().compareTo(t1.getTimestamp())) // Sort by timestamp descending
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves all transactions in the system, with optional date filtering (for admin/audit).
     * @param startDateString Optional start date string (e.g., "YYYY-MM-DD").
     * @param endDateString Optional end date string (e.g., "YYYY-MM-DD").
     * @return A list of TransactionResponse DTOs.
     * @throws IllegalArgumentException if date strings are invalid.
     */
    public List<TransactionResponse> getAllTransactions(String startDateString, String endDateString) {
        List<Transaction> transactions;
        LocalDateTime startDate = null;
        LocalDateTime endDate = null;

        // Parse start date
        if (startDateString != null && !startDateString.isEmpty()) {
            try {
                startDate = LocalDateTime.parse(startDateString + "T00:00:00"); // Start of the day
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException("Invalid start date format. Use YYYY-MM-DD.");
            }
        }

        // Parse end date
        if (endDateString != null && !endDateString.isEmpty()) {
            try {
                endDate = LocalDateTime.parse(endDateString + "T23:59:59"); // End of the day
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException("Invalid end date format. Use YYYY-MM-DD.");
            }
        }

        if (startDate != null && endDate != null) {
            transactions = transactionRepository.findByTimestampBetweenOrderByTimestampDesc(startDate, endDate); // Added OrderBy
        } else if (startDate != null) {
            transactions = transactionRepository.findByTimestampAfterOrderByTimestampDesc(startDate); // Added OrderBy
        } else if (endDate != null) {
            transactions = transactionRepository.findByTimestampBeforeOrderByTimestampDesc(endDate); // Added OrderBy
        } else {
            transactions = transactionRepository.findAllByOrderByTimestampDesc(); // Changed to get all sorted
        }

        return transactions.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // Helper method to convert Transaction entity to TransactionResponse DTO
    private TransactionResponse convertToDto(Transaction transaction) {
        TransactionResponse dto = new TransactionResponse();
        dto.setId(transaction.getId());
        dto.setTransactionType(transaction.getTransactionType());
        dto.setAmount(transaction.getAmount());
        dto.setTimestamp(transaction.getTimestamp());
        dto.setReferenceId(transaction.getReferenceId());

        // Include source and destination account numbers if available
        if (transaction.getSourceAccount() != null) {
            dto.setSourceAccountNumber(transaction.getSourceAccount().getAccountNumber());
        }
        if (transaction.getDestinationAccount() != null) {
            dto.setDestinationAccountNumber(transaction.getDestinationAccount().getAccountNumber());
        }
        return dto;
    }
}