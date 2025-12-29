package com.CinemaManager.Cinema.booking.controller;

import com.CinemaManager.Cinema.booking.dto.SessionDTO;
import com.CinemaManager.Cinema.booking.service.AdminService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminController.class)
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private AdminService adminService;

    @Test
    void getDashboardStats_ShouldReturnStats() throws Exception {
        // Arrange
        AdminController.DashboardStatsDTO stats = new AdminController.DashboardStatsDTO(
                10L,  // totalMovies
                50L,  // totalSessions
                200L, // totalTicketsSold
                100L, // totalUsers
                5000.0, // todaysRevenue
                20L   // todaysTickets
        );

        when(adminService.getDashboardStats()).thenReturn(stats);

        // Act & Assert
        mockMvc.perform(get("/api/admin/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalMovies").value(10))
                .andExpect(jsonPath("$.data.totalTicketsSold").value(200))
                .andExpect(jsonPath("$.data.todaysRevenue").value(5000.0));
    }

    @Test
    void getRevenueStats_WithDateRange_ShouldReturnRevenue() throws Exception {
        // Arrange
        Map<LocalDate, Double> dailyRevenue = new HashMap<>();
        dailyRevenue.put(LocalDate.now(), 1000.0);
        dailyRevenue.put(LocalDate.now().minusDays(1), 800.0);

        Map<String, Double> revenueByMovie = new HashMap<>();
        revenueByMovie.put("The Matrix", 500.0);
        revenueByMovie.put("Inception", 300.0);

        AdminController.RevenueStatsDTO revenueStats = new AdminController.RevenueStatsDTO(
                5000.0,        // totalRevenue
                dailyRevenue,  // dailyRevenue
                revenueByMovie, // revenueByMovie
                12.50          // averageTicketPrice
        );

        when(adminService.getRevenueStats(any(), any())).thenReturn(revenueStats);

        // Act & Assert
        mockMvc.perform(get("/api/admin/revenue")
                        .param("startDate", LocalDate.now().minusDays(7).toString())
                        .param("endDate", LocalDate.now().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalRevenue").value(5000.0))
                .andExpect(jsonPath("$.data.averageTicketPrice").value(12.50));
    }

    @Test
    void getPopularMovies_ShouldReturnPopularMovies() throws Exception {
        // Arrange
        Map<String, Long> ticketsByMovie = new HashMap<>();
        ticketsByMovie.put("The Matrix", 50L);
        ticketsByMovie.put("Inception", 30L);

        Map<String, Double> revenueByMovie = new HashMap<>();
        revenueByMovie.put("The Matrix", 1000.0);
        revenueByMovie.put("Inception", 600.0);

        AdminController.PopularMoviesDTO popularMovies = new AdminController.PopularMoviesDTO(
                ticketsByMovie,
                revenueByMovie
        );

        when(adminService.getPopularMovies(anyInt())).thenReturn(popularMovies);

        // Act & Assert
        mockMvc.perform(get("/api/admin/popular-movies")
                        .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.ticketsByMovie['The Matrix']").value(50))
                .andExpect(jsonPath("$.data.revenueByMovie['Inception']").value(600.0));
    }

    @Test
    void cancelSession_ShouldReturnSuccess() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/admin/sessions/{sessionId}/cancel", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Сеанс успешно отменен"));
    }
}