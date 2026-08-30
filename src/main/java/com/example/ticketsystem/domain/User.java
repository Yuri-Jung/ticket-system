package com.example.ticketsystem.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
    name = "USERS",
    indexes = {
        @Index(name = "uk_users_email", columnList = "email", unique = true)
    }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String email;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public User(String email, String name, LocalDateTime createdAt) {
        this.email = email;
        this.name = name;
        this.createdAt = createdAt;
    }
}
