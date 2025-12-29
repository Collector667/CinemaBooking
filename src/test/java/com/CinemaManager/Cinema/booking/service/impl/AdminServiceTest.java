package com.CinemaManager.Cinema.booking.service.impl;

import com.CinemaManager.Cinema.booking.controller.AdminController.*;
import com.CinemaManager.Cinema.booking.entity.*;
import com.CinemaManager.Cinema.booking.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private MovieRepository movieRepository;

    @Mock
    private SessionRepository sessionRepository;

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private HallRepository hallRepository;

    @InjectMocks
    private AdminServiceImpl adminService;

    @BeforeEach
    void setUp() {
        // Настройка моков для базовых методов
        when(movieRepository.count()).thenReturn(10L);
        when(sessionRepository.count()).thenReturn(50L);
        when(ticketRepository.countByStatus(Ticket.TicketStatus.SOLD)).thenReturn(200L);
        when(userRepository.count()).thenReturn(100L);
    }

    @Test
    void getDashboardStats_ShouldReturnStats() {
        when(ticketRepository.calculateRevenueByPeriod(any(), any())).thenReturn(5000.0);
        when(ticketRepository.countSoldTicketsByDate(any(), any())).thenReturn(20L);

        DashboardStatsDTO stats = adminService.getDashboardStats();

        assertNotNull(stats);
        assertEquals(10L, stats.totalMovies());
        assertEquals(50L, stats.totalSessions());
        assertEquals(200L, stats.totalTicketsSold());
        assertEquals(100L, stats.totalUsers());
        assertEquals(5000.0, stats.todaysRevenue());
        assertEquals(20L, stats.todaysTickets());
    }

    @Test
    void getPopularMovies_ShouldReturnPopularMovies() {
        Object[] movieData1 = new Object[]{"The Matrix", 50L};
        Object[] movieData2 = new Object[]{"Inception", 30L};
        List<Object[]> ticketData = Arrays.asList(movieData1, movieData2);

        Object[] revenueData1 = new Object[]{"The Matrix", 1000.0};
        Object[] revenueData2 = new Object[]{"Inception", 600.0};
        List<Object[]> revenueData = Arrays.asList(revenueData1, revenueData2);

        when(ticketRepository.getTicketCountByMovie(any(), any())).thenReturn(ticketData);
        when(ticketRepository.getRevenueByMovie(any(), any())).thenReturn(revenueData);

        PopularMoviesDTO result = adminService.getPopularMovies(10);

        assertNotNull(result);
        assertEquals(2, result.ticketsByMovie().size());
        assertEquals(2, result.revenueByMovie().size());
    }

    @Test
    void cancelSession_ValidSession_ShouldCancelSession() {
        Movie movie = Movie.builder().duration(java.time.Duration.ofMinutes(120)).build();
        Session session = Session.builder()
                .startTime(LocalDateTime.now().plusDays(1))
                .movie(movie)
                .build();

        when(sessionRepository.findById(1L)).thenReturn(java.util.Optional.of(session));

        adminService.cancelSession(1L);

        verify(sessionRepository).delete(session);
    }

    @Test
    void getSystemHealth_ShouldReturnHealthInfo() {
        when(sessionRepository.countByStartTimeAfter(any())).thenReturn(15L);

        SystemHealthDTO health = adminService.getSystemHealth();

        assertNotNull(health);
        assertTrue(health.databaseConnected());
        assertEquals(10L, health.totalMovies());
        assertEquals(100L, health.totalUsers());
        assertEquals(15L, health.activeSessions());
    }
}