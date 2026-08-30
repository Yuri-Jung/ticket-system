package com.example.ticketsystem.repository;

import com.example.ticketsystem.domain.ConcertSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConcertScheduleRepository extends JpaRepository<ConcertSchedule, Long> {
}
