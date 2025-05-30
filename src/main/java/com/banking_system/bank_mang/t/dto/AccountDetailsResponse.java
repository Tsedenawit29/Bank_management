package com.banking_system.bank_mang.t.dto;

import com.banking_system.bank_mang.t.enums.AccountStatus;
import com.banking_system.bank_mang.t.enums.AccountTypes;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountDetailsResponse {
    private Long id;
    private String accountNumber;
    private BigDecimal balance;
    private AccountTypes accountType;
    private AccountStatus status;
    private Long userId; // Include user ID for context
    private String username; // Include username for context
}