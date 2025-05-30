package com.banking_system.bank_mang.t.dto;

import com.banking_system.bank_mang.t.enums.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {
    private Long id;
    private TransactionType transactionType;
    private BigDecimal amount;
    private LocalDateTime timestamp;
    private String referenceId;
    private String sourceAccountNumber; // Null for deposits
    private String destinationAccountNumber; // Null for withdrawals
}
