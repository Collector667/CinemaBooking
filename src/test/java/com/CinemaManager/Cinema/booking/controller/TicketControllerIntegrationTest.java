package com.CinemaManager.Cinema.booking.controller;

import com.CinemaManager.Cinema.booking.dto.PurchaseTicketDTO;
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
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class TicketControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private HallRepository hallRepository;

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private Session testSession;
    private List<Seat> availableSeats;

    @BeforeEach
    void setUp() {
        ticketRepository.deleteAll();
        sessionRepository.deleteAll();
        movieRepository.deleteAll();
        hallRepository.deleteAll();
        seatRepository.deleteAll();
        userRepository.deleteAll();

        // Создаем тестового пользователя
        testUser = User.builder()
                .firstName("Тест")
                .lastName("Пользователь")
                .email("test@example.com")
                .password("password123")
                .phone("+79123456789")
                .role(User.Role.ROLE_USER)
                .build();
        testUser = userRepository.save(testUser);

        // Создаем тестовый фильм
        Movie movie = Movie.builder()
                .title("Тестовый фильм")
                .duration(Duration.ofMinutes(120))
                .genre("Драма")
                .ageRestriction(16)
                .build();
        movie = movieRepository.save(movie);

        // Создаем тестовый зал
        Hall hall = Hall.builder()
                .hallNumber("Зал 1")
                .name("Красный зал")
                .totalRows(5)
                .seatsPerRow(5)
                .build();
        hall = hallRepository.save(hall);

        // Инициализируем места
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

        // Создаем тестовый сеанс
        testSession = Session.builder()
                .startTime(LocalDateTime.now().plusDays(1))
                .price(300.0)
                .movie(movie)
                .hall(hall)
                .build();
        testSession = sessionRepository.save(testSession);

        // Получаем доступные места
        availableSeats = seatRepository.findByHall(hall);
    }

    @Test
    void purchaseTickets_ValidData_ShouldReturnSuccess() throws Exception {
        PurchaseTicketDTO purchaseDTO = PurchaseTicketDTO.builder()
                .sessionId(testSession.getId())
                .userId(testUser.getId())
                .seatIds(List.of(
                        availableSeats.get(0).getId(),
                        availableSeats.get(1).getId()
                ))
                .build();

        mockMvc.perform(post("/api/tickets/purchase")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(purchaseDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Билеты успешно куплены"))
                .andExpect(jsonPath("$.data.totalAmount").value(600.0)) // 2 билета по 300
                .andExpect(jsonPath("$.data.purchasedTickets").isArray())
                .andExpect(jsonPath("$.data.purchasedTickets.length()").value(2));
    }



    @Test
    void reserveTickets_ValidData_ShouldReturnReservedTickets() throws Exception {
        PurchaseTicketDTO purchaseDTO = PurchaseTicketDTO.builder()
                .sessionId(testSession.getId())
                .userId(testUser.getId())
                .seatIds(List.of(availableSeats.get(0).getId()))
                .build();

        mockMvc.perform(post("/api/tickets/reserve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(purchaseDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Билеты успешно забронированы"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].status").value("BOOKED"));
    }

    @Test
    void getTicketsByUser_ShouldReturnUserTickets() throws Exception {
        // Сначала покупаем билет
        PurchaseTicketDTO purchaseDTO = PurchaseTicketDTO.builder()
                .sessionId(testSession.getId())
                .userId(testUser.getId())
                .seatIds(List.of(availableSeats.get(0).getId()))
                .build();

        mockMvc.perform(post("/api/tickets/purchase")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(purchaseDTO)));

        mockMvc.perform(get("/api/tickets/by-user/{userId}", testUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1));
    }

    @Test
    void getTicketsBySession_ShouldReturnSessionTickets() throws Exception {
        // Покупаем билет
        PurchaseTicketDTO purchaseDTO = PurchaseTicketDTO.builder()
                .sessionId(testSession.getId())
                .userId(testUser.getId())
                .seatIds(List.of(availableSeats.get(0).getId()))
                .build();

        mockMvc.perform(post("/api/tickets/purchase")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(purchaseDTO)));

        mockMvc.perform(get("/api/tickets/by-session/{sessionId}", testSession.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1));
    }

    @Test
    void confirmTicket_ValidReservation_ShouldReturnConfirmedTicket() throws Exception {
        // Сначала бронируем билет
        PurchaseTicketDTO reserveDTO = PurchaseTicketDTO.builder()
                .sessionId(testSession.getId())
                .userId(testUser.getId())
                .seatIds(List.of(availableSeats.get(0).getId()))
                .build();

        String response = mockMvc.perform(post("/api/tickets/reserve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reserveDTO)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Извлекаем номер билета из ответа
        String ticketNumber = objectMapper.readTree(response)
                .path("data")
                .get(0)
                .path("ticketNumber")
                .asText();

        // Подтверждаем бронь
        mockMvc.perform(post("/api/tickets/{ticketNumber}/confirm", ticketNumber))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Билет подтвержден"))
                .andExpect(jsonPath("$.data.status").value("SOLD"));
    }
}