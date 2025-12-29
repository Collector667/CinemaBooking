package com.CinemaManager.Cinema.booking.service;

import com.CinemaManager.Cinema.booking.controller.AdminController.*;
import com.CinemaManager.Cinema.booking.dto.SessionDTO;
import java.time.LocalDate;
import java.util.List;

public interface AdminService {
    DashboardStatsDTO getDashboardStats();
    RevenueStatsDTO getRevenueStats(LocalDate startDate, LocalDate endDate);
    PopularMoviesDTO getPopularMovies(int limit);
    List<SessionDTO> getLowAttendanceSessions(int threshold);

    void cancelSession(Long sessionId);
    void rescheduleSession(Long sessionId, LocalDate newDate);
    List<SessionDTO> getSessionsByDateRange(LocalDate startDate, LocalDate endDate);

    List<UserStatsDTO> getAllUsersStats();
    void toggleUserStatus(Long userId, boolean active);

    void cleanupExpiredData();
    SystemHealthDTO getSystemHealth();
}