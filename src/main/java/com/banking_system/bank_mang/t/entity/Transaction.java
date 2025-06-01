package com.banking_system.bank_mang.t.entity;

import com.banking_system.bank_mang.t.enums.TransactionType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime; // Make sure you have this import

@Entity
@Table(name = "transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType transactionType; // DEPOSIT, WITHDRAWAL, TRANSFER

    @Column(nullable = false)
    private BigDecimal amount;

    // ADD THIS CODE HERE:
    @Column(nullable = false)
    private LocalDateTime timestamp = LocalDateTime.now(); // Records when the transaction occurred

    @Column(unique = true, nullable = false) // Unique identifier for each transaction
    private String referenceId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_account_id") // Foreign key to the source account
    private Account sourceAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destination_account_id") // Foreign key to the destination account
    private Account destinationAccount;
}