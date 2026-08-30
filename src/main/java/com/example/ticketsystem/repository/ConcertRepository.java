package com.example.ticketsystem.repository;

import com.example.ticketsystem.domain.Concert;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConcertRepository extends JpaRepository<Concert, Long> {
}
