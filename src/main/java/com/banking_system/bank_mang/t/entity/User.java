package com.banking_system.bank_mang.t.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, nullable = false)
    private String username;
    @Column(nullable = false)
    private String passward;
    @Column(unique = true, nullable = false)
    private String email;
    private boolean enabled = true;
    private boolean accountNonLocked = true;

    private int failedLoginAttempts = 0;

    private LocalTime lockTime;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name= "user_roles",
    joinColumns = @JoinColumn(name= "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>();

}
