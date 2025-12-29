package com.CinemaManager.Cinema.booking.service.impl;

import com.CinemaManager.Cinema.booking.dto.SessionDTO;
import com.CinemaManager.Cinema.booking.entity.Movie;
import com.CinemaManager.Cinema.booking.entity.Hall;
import com.CinemaManager.Cinema.booking.entity.Session;
import com.CinemaManager.Cinema.booking.exception.BusinessException;
import com.CinemaManager.Cinema.booking.mapper.CinemaMapper;
import com.CinemaManager.Cinema.booking.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SessionServiceTest {

    @Mock
    private SessionRepository sessionRepository;

    @Mock
    private MovieRepository movieRepository;

    @Mock
    private HallRepository hallRepository;

    @Mock
    private CinemaMapper cinemaMapper;

    @InjectMocks
    private SessionServiceImpl sessionService;

    private Movie movie;
    private Hall hall;
    private Session session;
    private SessionDTO sessionDTO;

    @BeforeEach
    void setUp() {
        movie = Movie.builder()
                .title("The Matrix")
                .duration(Duration.ofMinutes(136))
                .build();

        hall = Hall.builder()
                .name("Main Hall")
                .totalRows(10)
                .seatsPerRow(15)
                .build();

        session = Session.builder()
                .startTime(LocalDateTime.now().plusDays(1))
                .price(12.50)
                .movie(movie)
                .hall(hall)
                .build();

        sessionDTO = SessionDTO.builder()
                .id(1L)
                .startTime(LocalDateTime.now().plusDays(1))
                .price(12.50)
                .movieId(1L)
                .hallId(1L)
                .build();
    }

    @Test
    void getSessionById_WhenSessionExists_ShouldReturnSessionDTO() {
        when(sessionRepository.findSessionWithMovieAndHall(1L)).thenReturn(Optional.of(session));
        when(cinemaMapper.toDTO(session)).thenReturn(sessionDTO);

        SessionDTO result = sessionService.getSessionById(1L);

        assertNotNull(result);
        assertEquals(12.50, result.getPrice());
        verify(sessionRepository).findSessionWithMovieAndHall(1L);
    }

    @Test
    void createSession_ValidSession_ShouldSaveAndReturnDTO() {
        when(movieRepository.findById(1L)).thenReturn(Optional.of(movie));
        when(hallRepository.findById(1L)).thenReturn(Optional.of(hall));
        when(sessionRepository.hasOverlappingSessions(any(), any(), any(), any())).thenReturn(false);
        when(sessionRepository.save(any(Session.class))).thenReturn(session);
        when(cinemaMapper.toDTO(session)).thenReturn(sessionDTO);

        SessionDTO result = sessionService.createSession(sessionDTO);

        assertNotNull(result);
        verify(sessionRepository).save(any(Session.class));
    }
}