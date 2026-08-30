package com.example.ticketsystem.repository;

import com.example.ticketsystem.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
