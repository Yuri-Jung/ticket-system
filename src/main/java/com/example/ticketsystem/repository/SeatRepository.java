package com.example.ticketsystem.repository;

import com.example.ticketsystem.domain.Seat;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SeatRepository extends JpaRepository<Seat, Long> {
}
