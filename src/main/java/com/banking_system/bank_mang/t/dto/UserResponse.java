package com.banking_system.bank_mang.t.dto;

import com.banking_system.bank_mang.t.enums.AccountStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private boolean enabled;
    private boolean accountNonLocked;
    private List<String> roles;
    private Long accountId; // Optional: To link directly to their main account
    private String accountNumber; // Optional: To display their main account number
    private AccountStatus accountStatus; // Optional: To display their main account status
}
