package com.CinemaManager.Cinema.booking.service;

import com.CinemaManager.Cinema.booking.dto.SessionDTO;
import com.CinemaManager.Cinema.booking.dto.SessionSeatsDTO;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface SessionService {
    SessionDTO getSessionById(Long id);
    SessionSeatsDTO getSessionWithSeats(Long id);
    List<SessionDTO> getAllSessions();
    List<SessionDTO> getSessionsByMovie(Long movieId);
    List<SessionDTO> getSessionsByDate(LocalDate date);
    List<SessionDTO> getUpcomingSessions(int days);
    List<SessionDTO> getAvailableSessions();
    SessionDTO createSession(SessionDTO sessionDTO);
    SessionDTO updateSession(Long id, SessionDTO sessionDTO);
    void deleteSession(Long id);
    boolean isHallAvailable(Long hallId, LocalDateTime startTime, LocalDateTime endTime, Long excludeSessionId);
}