package com.CinemaManager.Cinema.booking.controller;

import com.CinemaManager.Cinema.booking.dto.SessionDTO;
import com.CinemaManager.Cinema.booking.entity.*;
import com.CinemaManager.Cinema.booking.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class SessionControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private HallRepository hallRepository;

    @Autowired
    private SeatRepository seatRepository;

    private Movie testMovie;
    private Hall testHall;
    private Session testSession;

    @BeforeEach
    void setUp() {
        sessionRepository.deleteAll();
        movieRepository.deleteAll();
        hallRepository.deleteAll();
        seatRepository.deleteAll();

        // Создаем тестовый фильм
        testMovie = Movie.builder()
                .title("Тестовый фильм")
                .description("Описание тестового фильма")
                .duration(Duration.ofMinutes(120))
                .genre("Драма")
                .ageRestriction(16)
                .build();
        testMovie = movieRepository.save(testMovie);

        // Создаем тестовый зал
        testHall = Hall.builder()
                .hallNumber("Зал 1")
                .name("Красный зал")
                .totalRows(10)
                .seatsPerRow(15)
                .description("Основной зал кинотеатра")
                .build();
        testHall = hallRepository.save(testHall);

        // Инициализируем места
        initializeSeats(testHall);

        // Создаем тестовый сеанс
        testSession = Session.builder()
                .startTime(LocalDateTime.now().plusDays(1))
                .price(350.0)
                .movie(testMovie)
                .hall(testHall)
                .build();
        testSession = sessionRepository.save(testSession);
    }

    private void initializeSeats(Hall hall) {
        for (int row = 1; row <= hall.getTotalRows(); row++) {
            for (int seatNum = 1; seatNum <= hall.getSeatsPerRow(); seatNum++) {
                Seat seat = Seat.builder()
                        .rowNumber(row)
                        .seatNumber(seatNum)
                        .hall(hall)
                        .build();
                seatRepository.save(seat);
            }
        }
    }

    @Test
    void getAllSessions_ShouldReturnSessionList() throws Exception {
        mockMvc.perform(get("/api/sessions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1));
    }

    @Test
    void getSessionById_WhenSessionExists_ShouldReturnSession() throws Exception {
        mockMvc.perform(get("/api/sessions/{id}", testSession.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(testSession.getId()))
                .andExpect(jsonPath("$.data.price").value(350.0))
                .andExpect(jsonPath("$.data.movieTitle").value("Тестовый фильм"))
                .andExpect(jsonPath("$.data.hallName").value("Красный зал"));
    }

    @Test
    void getSessionWithSeats_ShouldReturnSessionWithSeatStatuses() throws Exception {
        mockMvc.perform(get("/api/sessions/{id}/seats", testSession.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.sessionId").value(testSession.getId()))
                .andExpect(jsonPath("$.data.movieTitle").value("Тестовый фильм"))
                .andExpect(jsonPath("$.data.totalRows").value(10))
                .andExpect(jsonPath("$.data.seatsPerRow").value(15))
                .andExpect(jsonPath("$.data.seats").isArray())
                .andExpect(jsonPath("$.data.seats.length()").value(150)); // 10 * 15
    }

    @Test
    void createSession_ValidData_ShouldReturnCreatedSession() throws Exception {
        SessionDTO sessionDTO = SessionDTO.builder()
                .startTime(LocalDateTime.now().plusDays(2))
                .price(400.0)
                .movieId(testMovie.getId())
                .hallId(testHall.getId())
                .build();

        mockMvc.perform(post("/api/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sessionDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Сеанс успешно создан"))
                .andExpect(jsonPath("$.data.price").value(400.0));
    }





    @Test
    void getSessionsByMovie_ShouldReturnSessionsForMovie() throws Exception {
        mockMvc.perform(get("/api/sessions/by-movie/{movieId}", testMovie.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1));
    }

    @Test
    void getUpcomingSessions_ShouldReturnFutureSessions() throws Exception {
        mockMvc.perform(get("/api/sessions/upcoming")
                        .param("days", "7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void getAvailableSessions_ShouldReturnSessionsWithAvailableSeats() throws Exception {
        mockMvc.perform(get("/api/sessions/available"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }
}