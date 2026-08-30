package com.example.ticketsystem.seat;

import com.example.ticketsystem.domain.Seat;
import com.example.ticketsystem.repository.SeatRepository;
import com.example.ticketsystem.seat.exception.SeatHoldUnavailableException;
import com.example.ticketsystem.seat.exception.SeatNotFoundException;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SeatHoldService {

    private static final long HOLD_MINUTES = 5;

    private final SeatRepository seatRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public SeatHoldResult holdSeat(Long seatId, Long userId) {
        LocalDateTime now = LocalDateTime.now();
        Seat seat = seatRepository.findById(seatId)
            .orElseThrow(() -> new SeatNotFoundException(seatId));

        if (!seat.isHoldableAt(now)) {
            throw new SeatHoldUnavailableException(seatId);
        }

        LocalDateTime heldUntil = now.plusMinutes(HOLD_MINUTES);
        seat.hold(userId, heldUntil);
        Seat savedSeat = seatRepository.save(seat);

        return new SeatHoldResult(savedSeat.getId(), userId, heldUntil);
    }
}
