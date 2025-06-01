package com.banking_system.bank_mang.t;

import com.banking_system.bank_mang.t.entity.Role;
import com.banking_system.bank_mang.t.entity.User;
import com.banking_system.bank_mang.t.enums.RoleName;
import com.banking_system.bank_mang.t.repositories.RoleRepository;
import com.banking_system.bank_mang.t.repositories.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@SpringBootApplication
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	/**
	 * This bean defines a CommandLineRunner that executes once the application starts.
	 * It's responsible for initializing default roles and users (Admin, Staff).
	 * Customers will register themselves via the API.
	 */
	@Bean
	@Transactional
	public CommandLineRunner initRolesAndDefaultUsers(
			RoleRepository roleRepository,
			UserRepository userRepository,
			PasswordEncoder passwordEncoder
	) {
		return args -> {
			System.out.println("--- Initializing Default Roles and Users ---");

			// --- 1. Initialize Roles ---
			Role adminRole = findOrCreateRole(roleRepository, RoleName.ADMIN);
			Role staffRole = findOrCreateRole(roleRepository, RoleName.STAFF);
			findOrCreateRole(roleRepository, RoleName.CUSTOMER); // Ensure CUSTOMER role exists

			// --- 2. Initialize Default Users ---
			// Create default ADMIN user if not exists
			if (userRepository.findByUsername("admin").isEmpty()) {
				User adminUser = new User();
				adminUser.setUsername("admin");
				adminUser.setEmail("admin@bank.com");
				adminUser.setPassword(passwordEncoder.encode("adminpass"));
				adminUser.setCreatedAt(LocalDateTime.now());
				adminUser.setUpdatedAt(LocalDateTime.now());
				Set<Role> adminRoles = new HashSet<>();
				adminRoles.add(adminRole);
				adminUser.setRoles(adminRoles);
				adminUser.setEnabled(true); // Ensure enabled by default
				adminUser.setAccountNonLocked(true); // Ensure not locked by default
				userRepository.save(adminUser);
				System.out.println("Default ADMIN user 'admin' initialized.");
			}

			// Create default STAFF user if not exists
			if (userRepository.findByUsername("staff1").isEmpty()) {
				User staffUser = new User();
				staffUser.setUsername("staff1");
				staffUser.setEmail("staff1@bank.com");
				staffUser.setPassword(passwordEncoder.encode("staffpass"));
				staffUser.setCreatedAt(LocalDateTime.now());
				staffUser.setUpdatedAt(LocalDateTime.now());
				Set<Role> staffRoles = new HashSet<>();
				staffRoles.add(staffRole);
				staffUser.setRoles(staffRoles);
				staffUser.setEnabled(true); // Ensure enabled by default
				staffUser.setAccountNonLocked(true); // Ensure not locked by default
				userRepository.save(staffUser);
				System.out.println("Default STAFF user 'staff1' initialized.");
			}

			// Removed the default CUSTOMER user initialization as customers will register themselves.

			System.out.println("--- Initialization Complete ---");
		};
	}

	private Role findOrCreateRole(RoleRepository roleRepository, RoleName roleNameEnum) {
		Optional<Role> existingRole = roleRepository.findByName(roleNameEnum);
		if (existingRole.isPresent()) {
			return existingRole.get();
		} else {
			Role newRole = new Role();
			newRole.setName(roleNameEnum);
			Role savedRole = roleRepository.save(newRole);
			System.out.println("Created role: " + roleNameEnum.name());
			return savedRole;
		}
	}
}