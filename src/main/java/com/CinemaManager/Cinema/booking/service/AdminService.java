package com.CinemaManager.Cinema.booking.service;

import com.CinemaManager.Cinema.booking.controller.AdminController.*;
import com.CinemaManager.Cinema.booking.dto.SessionDTO;
import java.time.LocalDate;
import java.util.List;

public interface AdminService {
    // Статистика и отчеты
    DashboardStatsDTO getDashboardStats();
    RevenueStatsDTO getRevenueStats(LocalDate startDate, LocalDate endDate);
    PopularMoviesDTO getPopularMovies(int limit);
    List<SessionDTO> getLowAttendanceSessions(int threshold);

    // Управление сеансами
    void cancelSession(Long sessionId);
    void rescheduleSession(Long sessionId, LocalDate newDate);
    List<SessionDTO> getSessionsByDateRange(LocalDate startDate, LocalDate endDate);

    // Управление пользователями
    List<UserStatsDTO> getAllUsersStats();
    void toggleUserStatus(Long userId, boolean active);

    // Системные функции
    void cleanupExpiredData();
    SystemHealthDTO getSystemHealth();
}