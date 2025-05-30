// src/main/java/com/yourcompany/bankingsystem/service/TransactionService.java
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
import java.util.ArrayList;
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
            // Both dates provided, filter by range
            transactions = transactionRepository.findBySourceAccountIdAndTimestampBetweenOrderByTimestampDesc(accountId, startDate, endDate);
            // Also include transactions where this account is the destination account for transfers
            List<Transaction> destinationTransactions = transactionRepository.findByDestinationAccountIdAndTimestampBetweenOrderByTimestampDesc(accountId, startDate, endDate);
            transactions.addAll(destinationTransactions);
        } else if (startDate != null) {
            // Only start date provided, filter from start date onwards
            transactions = transactionRepository.findBySourceAccount_IdAndTimestampAfter(accountId, startDate);
            List<Transaction> destinationTransactions = transactionRepository.findByDestinationAccount_IdAndTimestampAfter(accountId, startDate);
            transactions.addAll(destinationTransactions);
        } else if (endDate != null) {
            // Only end date provided, filter up to end date
            transactions = transactionRepository.findBySourceAccount_IdAndTimestampBefore(accountId, endDate);
            List<Transaction> destinationTransactions = transactionRepository.findByDestinationAccount_IdAndTimestampBefore(accountId, endDate);
            transactions.addAll(destinationTransactions);
        } else {
            // No dates provided, get all transactions for the account
            transactions = transactionRepository.findBySourceAccount_Id(accountId);
            List<Transaction> destinationTransactions = transactionRepository.findByDestinationAccount_Id(accountId);
            transactions.addAll(destinationTransactions);
        }

        // Remove duplicates if any (e.g., if a transaction appears as both source and destination due to an oversight in logic, though typically it should be two distinct records for a transfer)
        // A more robust way to handle this for transfers is to query for transactions where the 'amount' field signifies a debit/credit appropriately.
        // For simplicity and based on the current transaction recording, we'll just convert and collect.
        return transactions.stream()
                .distinct() // In case a single transaction is somehow picked up by both source and destination queries (less likely with distinct IDs)
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