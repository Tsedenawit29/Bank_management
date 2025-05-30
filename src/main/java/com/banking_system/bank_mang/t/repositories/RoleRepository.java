package com.banking_system.bank_mang.t.repositories;

import com.banking_system.bank_mang.t.entity.Role;
import com.banking_system.bank_mang.t.enums.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.authentication.jaas.JaasPasswordCallbackHandler;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(RoleName name);
}
