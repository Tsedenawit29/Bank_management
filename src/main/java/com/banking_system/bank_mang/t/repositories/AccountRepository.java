package com.banking_system.bank_mang.t.repositories;

import com.banking_system.bank_mang.t.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {
    // Find an account by its unique account number
    Optional<Account> findByAccountNumber(String accountNumber);

    // Find an account associated with a specific user ID
    // Note: A user might have multiple accounts (e.g., savings and current).
    // This method returns the first one found, or you might need to return a List.
    Optional<Account> findByUser_Id(Long userId); // Finds one account for a user

    // Find all accounts associated with a specific user ID
    List<Account> findAllByUser_Id(Long userId); // Finds all accounts for a user

    // Find an account by the username of the associated user
    Optional<Account> findByUser_Username(String username);

    // Find accounts that are pending approval by staff
    List<Account> findByIsApprovedByStaffFalse();
}
