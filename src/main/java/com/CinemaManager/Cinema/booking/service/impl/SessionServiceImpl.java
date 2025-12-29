package com.CinemaManager.Cinema.booking.service.impl;

import com.CinemaManager.Cinema.booking.dto.SessionDTO;
import com.CinemaManager.Cinema.booking.dto.SessionSeatsDTO;
import com.CinemaManager.Cinema.booking.dto.SeatStatusDTO;
import com.CinemaManager.Cinema.booking.entity.*;
import com.CinemaManager.Cinema.booking.exception.BusinessException;
import com.CinemaManager.Cinema.booking.exception.ResourceNotFoundException;
import com.CinemaManager.Cinema.booking.mapper.CinemaMapper;
import com.CinemaManager.Cinema.booking.repository.*;
import com.CinemaManager.Cinema.booking.service.SessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SessionServiceImpl implements SessionService {

    private final SessionRepository sessionRepository;
    private final MovieRepository movieRepository;
    private final HallRepository hallRepository;
    private final SeatRepository seatRepository;
    private final TicketRepository ticketRepository;
    private final CinemaMapper cinemaMapper;

    @Override
    @Transactional
    public SessionDTO createSession(SessionDTO sessionDTO) {
        log.info("Creating new session for movie ID: {} in hall ID: {}",
                sessionDTO.getMovieId(), sessionDTO.getHallId());

        // Проверяем существование фильма и зала
        Movie movie = movieRepository.findById(sessionDTO.getMovieId())
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found with id: " + sessionDTO.getMovieId()));
        Hall hall = hallRepository.findById(sessionDTO.getHallId())
                .orElseThrow(() -> new ResourceNotFoundException("Hall not found with id: " + sessionDTO.getHallId()));

        // Проверяем, что время сеанса в будущем
        if (sessionDTO.getStartTime().isBefore(LocalDateTime.now())) {
            throw new BusinessException("Session start time must be in the future");
        }

        // Проверяем, что зал свободен в это время
        if (sessionRepository.hasOverlappingSessions(
                hall.getId(),
                null,
                sessionDTO.getStartTime(),
                sessionDTO.getStartTime().plus(movie.getDuration()))) {
            throw new BusinessException("Hall is already booked for this time period");
        }

        Session session = new Session();
        session.setStartTime(sessionDTO.getStartTime());
        session.setPrice(sessionDTO.getPrice());
        session.setMovie(movie);
        session.setHall(hall);

        Session savedSession = sessionRepository.save(session);
        log.info("Session created with ID: {}", savedSession.getId());
        return cinemaMapper.toDTO(savedSession);
    }

    @Override
    @Transactional(readOnly = true)
    public SessionDTO getSessionById(Long id) {
        log.debug("Fetching session with ID: {}", id);
        Session session = sessionRepository.findSessionWithMovieAndHall(id)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found with id: " + id));
        return cinemaMapper.toDTO(session);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SessionDTO> getAllSessions() {
        log.debug("Fetching all sessions");
        List<Session> sessions = sessionRepository.findAll();
        return sessions.stream()
                .map(cinemaMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public SessionDTO updateSession(Long id, SessionDTO sessionDTO) {
        log.info("Updating session with ID: {}", id);
        Session existingSession = sessionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found with id: " + id));

        // Проверяем существование фильма и зала (если они изменились)
        Movie movie = existingSession.getMovie();
        if (!existingSession.getMovie().getId().equals(sessionDTO.getMovieId())) {
            movie = movieRepository.findById(sessionDTO.getMovieId())
                    .orElseThrow(() -> new ResourceNotFoundException("Movie not found with id: " + sessionDTO.getMovieId()));
        }

        Hall hall = existingSession.getHall();
        if (!existingSession.getHall().getId().equals(sessionDTO.getHallId())) {
            hall = hallRepository.findById(sessionDTO.getHallId())
                    .orElseThrow(() -> new ResourceNotFoundException("Hall not found with id: " + sessionDTO.getHallId()));
        }

        // Проверяем, что зал свободен в новое время (если время изменилось)
        if (!existingSession.getStartTime().equals(sessionDTO.getStartTime())) {
            if (sessionRepository.hasOverlappingSessions(
                    hall.getId(),
                    id,
                    sessionDTO.getStartTime(),
                    sessionDTO.getStartTime().plus(movie.getDuration()))) {
                throw new BusinessException("Hall is already booked for this time period");
            }
        }

        existingSession.setStartTime(sessionDTO.getStartTime());
        existingSession.setPrice(sessionDTO.getPrice());
        existingSession.setMovie(movie);
        existingSession.setHall(hall);

        Session updatedSession = sessionRepository.save(existingSession);
        log.info("Session updated with ID: {}", updatedSession.getId());
        return cinemaMapper.toDTO(updatedSession);
    }

    @Override
    @Transactional
    public void deleteSession(Long id) {
        log.info("Deleting session with ID: {}", id);
        Session session = sessionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found with id: " + id));

        // Проверяем, есть ли проданные билеты на этот сеанс
        Long soldTicketsCount = ticketRepository.countSoldTicketsBySession(id);
        if (soldTicketsCount > 0) {
            throw new BusinessException("Cannot delete session with sold tickets");
        }

        // Отменяем все бронирования на этот сеанс
        List<Ticket> sessionTickets = ticketRepository.findBySessionId(id);
        for (Ticket ticket : sessionTickets) {
            if (ticket.getStatus() == Ticket.TicketStatus.BOOKED) {
                ticket.setStatus(Ticket.TicketStatus.CANCELLED);
            }
        }
        ticketRepository.saveAll(sessionTickets);

        sessionRepository.delete(session);
        log.info("Session deleted with ID: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public SessionSeatsDTO getSessionWithSeats(Long sessionId) {
        log.debug("Fetching session with seats for session ID: {}", sessionId);
        Session session = sessionRepository.findSessionWithMovieAndHall(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found with id: " + sessionId));

        // Получаем все места в зале
        List<Seat> allSeats = seatRepository.findByHall(session.getHall());

        // Получаем занятые места на этот сеанс
        List<Seat> occupiedSeats = seatRepository.findOccupiedSeatsBySession(sessionId);

        // Создаем DTO для каждого места со статусом
        List<SeatStatusDTO> seatStatuses = new ArrayList<>();
        for (Seat seat : allSeats) {
            boolean isOccupied = occupiedSeats.stream()
                    .anyMatch(occupied -> occupied.getId().equals(seat.getId()));

            SeatStatusDTO seatStatus = SeatStatusDTO.builder()
                    .seatId(seat.getId())
                    .rowNumber(seat.getRowNumber())
                    .seatNumber(seat.getSeatNumber())
                    .status(isOccupied ? "OCCUPIED" : "FREE")
                    .build();
            seatStatuses.add(seatStatus);
        }

        // Создаем итоговый DTO
        SessionSeatsDTO sessionSeatsDTO = SessionSeatsDTO.builder()
                .sessionId(session.getId())
                .movieTitle(session.getMovie().getTitle())
                .startTime(session.getStartTime())
                .endTime(session.getEndTime())
                .price(session.getPrice())
                .hallName(session.getHall().getName())
                .totalRows(session.getHall().getTotalRows())
                .seatsPerRow(session.getHall().getSeatsPerRow())
                .seats(seatStatuses)
                .build();

        return sessionSeatsDTO;
    }









    // В SessionServiceImpl.java

    @Override
    public List<SessionDTO> getSessionsByMovie(Long movieId) {
        // Используйте новый метод
        List<Session> sessions = sessionRepository.findByMovieId(movieId);
        return sessions.stream()
                .map(cinemaMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<SessionDTO> getSessionsByDate(LocalDate date) {
        // Используйте метод с @Query для работы с датами
        List<Session> sessions = sessionRepository.findByDate(date);
        return sessions.stream()
                .map(cinemaMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<SessionDTO> getUpcomingSessions(int days) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endDate = now.plusDays(days);

        List<Session> sessions = sessionRepository.findByStartTimeBetween(now, endDate);
        return sessions.stream()
                .map(cinemaMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<SessionDTO> getAvailableSessions() {
        // Используйте специальный метод для сеансов со свободными местами
        List<Session> sessions = sessionRepository.findAvailableSessions();
        return sessions.stream()
                .map(cinemaMapper::toDTO)
                .collect(Collectors.toList());
    }

    // Метод для проверки доступности зала
    public boolean isHallAvailable(Long hallId, LocalDateTime startTime, LocalDateTime endTime, Long excludeSessionId) {
        return !sessionRepository.hasOverlappingSessions(hallId, excludeSessionId, startTime, endTime);
    }
}