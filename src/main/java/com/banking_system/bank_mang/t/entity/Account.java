package com.banking_system.bank_mang.t.entity;
import com.banking_system.bank_mang.t.enums.AccountStatus;
import com.banking_system.bank_mang.t.enums.AccountTypes;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
@Entity
@Table(name = "accounts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String accountNumber;

    @Column(nullable = false)
    private BigDecimal balance = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountTypes type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountStatus status = AccountStatus.PENDING_APPROVAL;
    private boolean isApprovedByStaff = false;

    // Many-to-One relationship with User entity
    // Multiple accounts can belong to one user.
    @ManyToOne(fetch = FetchType.LAZY) // Fetch user lazily (only when accessed)
    @JoinColumn(name = "user_id", nullable = false) // Foreign key column in 'accounts' table
    private User user;