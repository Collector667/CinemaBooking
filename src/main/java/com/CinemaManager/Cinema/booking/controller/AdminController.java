package com.CinemaManager.Cinema.booking.controller;

import com.CinemaManager.Cinema.booking.dto.ApiResponse;
import com.CinemaManager.Cinema.booking.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<DashboardStatsDTO>> getDashboardStats() {
        DashboardStatsDTO stats = adminService.getDashboardStats();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    @GetMapping("/revenue")
    public ResponseEntity<ApiResponse<RevenueStatsDTO>> getRevenueStats(
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate) {
        RevenueStatsDTO revenue = adminService.getRevenueStats(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(revenue));
    }

    @GetMapping("/popular-movies")
    public ResponseEntity<ApiResponse<PopularMoviesDTO>> getPopularMovies(
            @RequestParam(defaultValue = "10") int limit) {
        PopularMoviesDTO popularMovies = adminService.getPopularMovies(limit);
        return ResponseEntity.ok(ApiResponse.success(popularMovies));
    }

    @PostMapping("/sessions/{sessionId}/cancel")
    public ResponseEntity<ApiResponse<Void>> cancelSession(@PathVariable Long sessionId) {
        adminService.cancelSession(sessionId);
        return ResponseEntity.ok(ApiResponse.success("Сеанс успешно отменен", null));
    }

    // DTO для статистики админ-панели
    public record DashboardStatsDTO(
            Long totalMovies,
            Long totalSessions,
            Long totalTicketsSold,
            Long totalUsers,
            Double todaysRevenue,
            Long todaysTickets
    ) {}

    public record RevenueStatsDTO(
            Double totalRevenue,
            Map<LocalDate, Double> dailyRevenue,
            Map<String, Double> revenueByMovie,
            Double averageTicketPrice
    ) {}

    public record PopularMoviesDTO(
            Map<String, Long> ticketsByMovie,
            Map<String, Double> revenueByMovie
    ) {}

    public record UserStatsDTO(
            Long userId,
            String fullName,
            Integer totalTickets,
            Double totalSpent,
            String favoriteGenre,
            Integer ticketsThisMonth
    ) {}

    public record SystemHealthDTO(
            boolean databaseConnected,
            long totalMovies,
            long totalUsers,
            long totalMemoryBytes,
            long usedMemoryBytes,
            double memoryUsagePercent,
            long activeSessions,
            LocalDateTime lastChecked
    ) {}
}


