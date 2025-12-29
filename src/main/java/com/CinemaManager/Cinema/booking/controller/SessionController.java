package com.CinemaManager.Cinema.booking.controller;

import com.CinemaManager.Cinema.booking.dto.ApiResponse;
import com.CinemaManager.Cinema.booking.dto.SessionDTO;
import com.CinemaManager.Cinema.booking.dto.SessionSeatsDTO;
import com.CinemaManager.Cinema.booking.service.SessionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
public class SessionController {

    private final SessionService sessionService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<SessionDTO>>> getAllSessions() {
        List<SessionDTO> sessions = sessionService.getAllSessions();
        return ResponseEntity.ok(ApiResponse.success(sessions));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SessionDTO>> getSessionById(@PathVariable Long id) {
        SessionDTO session = sessionService.getSessionById(id);
        return ResponseEntity.ok(ApiResponse.success(session));
    }

    @GetMapping("/{id}/seats")
    public ResponseEntity<ApiResponse<SessionSeatsDTO>> getSessionWithSeats(@PathVariable Long id) {
        SessionSeatsDTO sessionSeats = sessionService.getSessionWithSeats(id);
        return ResponseEntity.ok(ApiResponse.success(sessionSeats));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<SessionDTO>> createSession(
            @Valid @RequestBody SessionDTO sessionDTO) {
        SessionDTO createdSession = sessionService.createSession(sessionDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Сеанс успешно создан", createdSession));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SessionDTO>> updateSession(
            @PathVariable Long id,
            @Valid @RequestBody SessionDTO sessionDTO) {
        SessionDTO updatedSession = sessionService.updateSession(id, sessionDTO);
        return ResponseEntity.ok(ApiResponse.success("Сеанс успешно обновлен", updatedSession));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteSession(@PathVariable Long id) {
        sessionService.deleteSession(id);
        return ResponseEntity.ok(ApiResponse.success("Сеанс успешно удален", null));
    }

    @GetMapping("/by-date")
    public ResponseEntity<ApiResponse<List<SessionDTO>>> getSessionsByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<SessionDTO> sessions = sessionService.getSessionsByDate(date);
        return ResponseEntity.ok(ApiResponse.success(sessions));
    }

    @GetMapping("/by-movie/{movieId}")
    public ResponseEntity<ApiResponse<List<SessionDTO>>> getSessionsByMovie(
            @PathVariable Long movieId) {
        List<SessionDTO> sessions = sessionService.getSessionsByMovie(movieId);
        return ResponseEntity.ok(ApiResponse.success(sessions));
    }

    @GetMapping("/upcoming")
    public ResponseEntity<ApiResponse<List<SessionDTO>>> getUpcomingSessions(
            @RequestParam(defaultValue = "7") int days) {
        List<SessionDTO> sessions = sessionService.getUpcomingSessions(days);
        return ResponseEntity.ok(ApiResponse.success(sessions));
    }

    @GetMapping("/available")
    public ResponseEntity<ApiResponse<List<SessionDTO>>> getAvailableSessions() {
        List<SessionDTO> sessions = sessionService.getAvailableSessions();
        return ResponseEntity.ok(ApiResponse.success(sessions));
    }
}