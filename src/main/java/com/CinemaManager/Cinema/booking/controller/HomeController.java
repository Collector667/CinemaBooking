package com.CinemaManager.Cinema.booking.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
public class HomeController {

    @GetMapping("/")
    public Map<String, Object> home() {
        return Map.of(
                "service", "Cinema Booking API",
                "status", "running",
                "timestamp", LocalDateTime.now(),
                "endpoints", new String[]{
                        "/api/movies",
                        "/api/test",
                        "/h2-console"
                }
        );
    }

    @GetMapping("/api/test")
    public String test() {
        return "API is working! ✅";
    }
}