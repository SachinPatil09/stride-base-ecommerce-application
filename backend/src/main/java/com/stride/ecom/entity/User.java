package com.stride.ecom.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * User Entity
 * Stores registered users. Each user has a ROLE (USER or ADMIN).
 *
 * STRIDE Note:
 *  - Spoofing: Password is BCrypt-hashed so plaintext is never stored
 *  - Elevation of Privilege: Role field controls what APIs each user can access
 */
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 50, message = "Name must be between 2 and 50 characters")
    @Column(nullable = false)
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Column(nullable = false, unique = true)
    private String email;

    // BCrypt hashed password — never stored in plaintext (STRIDE: Spoofing mitigation)
    @JsonIgnore
    @NotBlank
    @Column(nullable = false)
    private String password;

    // Role: USER or ADMIN (STRIDE: Elevation of Privilege mitigation)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.USER;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    // Enum for roles
    public enum Role {
        USER, ADMIN
    }
}
