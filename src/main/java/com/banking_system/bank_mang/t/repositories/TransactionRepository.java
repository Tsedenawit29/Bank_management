package com.banking_system.bank_mang.t.repositories;

import com.banking_system.bank_mang.t.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    // Find all transactions where the given account is either the source or the destination
    // Ordered by timestamp in descending order (most recent first)
    List<Transaction> findBySourceAccountIdOrDestinationAccountIdOrderByTimestampDesc(Long sourceAccountId, Long destinationAccountId);

    // Find transactions for a specific account within a date range
    List<Transaction> findBySourceAccountIdAndTimestampBetweenOrderByTimestampDesc(Long sourceAccountId, LocalDateTime startDate, LocalDateTime endDate);
    List<Transaction> findByDestinationAccountIdAndTimestampBetweenOrderByTimestampDesc(Long destinationAccountId, LocalDateTime startDate, LocalDateTime endDate);

    // Combined query for transactions by account ID and date range
    List<Transaction> findBySourceAccountIdOrDestinationAccountIdAndTimestampBetweenOrderByTimestampDesc(
            Long sourceAccountId, Long destinationAccountId, LocalDateTime startDate, LocalDateTime endDate);

    // Find transactions by reference ID (useful for auditing double-entry)
    Optional<Transaction> findByReferenceId(String referenceId);

    List<Transaction> findBySourceAccount_IdAndTimestampAfter(Long accountId, LocalDateTime startDate);

    List<Transaction> findByDestinationAccount_IdAndTimestampAfter(Long accountId, LocalDateTime startDate);

    List<Transaction> findBySourceAccount_IdAndTimestampBefore(Long accountId, LocalDateTime endDate);

    List<Transaction> findByDestinationAccount_IdAndTimestampBefore(Long accountId, LocalDateTime endDate);

    List<Transaction> findBySourceAccount_Id(Long accountId);

    List<Transaction> findByDestinationAccount_Id(Long accountId);
    List<Transaction> findByTimestampBetween(LocalDateTime start, LocalDateTime end);
    List<Transaction> findByTimestampAfter(LocalDateTime start);
    List<Transaction> findByTimestampBefore(LocalDateTime end);

}
