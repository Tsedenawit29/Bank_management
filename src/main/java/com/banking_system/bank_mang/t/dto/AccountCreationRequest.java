package com.banking_system.bank_mang.t.dto;

import com.banking_system.bank_mang.t.enums.AccountTypes;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountCreationRequest {
    @NotNull(message = "Account type cannot be null")
    private AccountTypes accountType; // e.g., SAVINGS, CURRENT
}
