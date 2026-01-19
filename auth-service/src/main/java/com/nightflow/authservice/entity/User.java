package com.nightflow.authservice.entity;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    // Şimdilik basit tutuyoruz, ileride buraya Role (Enum) ekleyeceğiz.
    private String role; // "USER", "ADMIN", "ORGANIZER"

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist // Kayıt olmadan hemen önce çalışır
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}