package com.example.ticketsystem.queue.controller;

import com.example.ticketsystem.queue.service.QueueService;
import com.example.ticketsystem.queue.service.QueueStatusResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/queue")
public class QueueController {

    private final QueueService queueService;

    @PostMapping("/waiting")
    public QueueStatusResponse registerWaiting(@RequestParam Long userId) {
        return queueService.registerWaiting(userId);
    }

    @GetMapping("/status")
    public QueueStatusResponse getStatus(@RequestParam Long userId) {
        return queueService.getStatus(userId);
    }
}
