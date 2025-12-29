package com.CinemaManager.Cinema.booking.service.impl;

import com.CinemaManager.Cinema.booking.controller.AdminController.*;
import com.CinemaManager.Cinema.booking.dto.SessionDTO;
import com.CinemaManager.Cinema.booking.dto.UserDTO;
import com.CinemaManager.Cinema.booking.entity.*;
import com.CinemaManager.Cinema.booking.exception.BusinessException;
import com.CinemaManager.Cinema.booking.exception.ResourceNotFoundException;
import com.CinemaManager.Cinema.booking.mapper.CinemaMapper;
import com.CinemaManager.Cinema.booking.repository.*;
import com.CinemaManager.Cinema.booking.service.AdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminServiceImpl implements AdminService {

    private final MovieRepository movieRepository;
    private final SessionRepository sessionRepository;
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final HallRepository hallRepository;
    private final CinemaMapper cinemaMapper;

    @Override
    @Transactional(readOnly = true)
    public DashboardStatsDTO getDashboardStats() {
        log.info("Fetching dashboard statistics");

        long totalMovies = movieRepository.count();
        long totalSessions = sessionRepository.count();
        long totalTicketsSold = ticketRepository.countByStatus(Ticket.TicketStatus.SOLD);
        long totalUsers = userRepository.count();

        // Выручка за сегодня
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = LocalDate.now().atTime(23, 59, 59);
        Double todaysRevenue = ticketRepository.calculateRevenueByPeriod(todayStart, todayEnd);

        // Билеты за сегодня
        Long todaysTickets = ticketRepository.countSoldTicketsByDate(todayStart, todayEnd);

        return new DashboardStatsDTO(
                totalMovies,
                totalSessions,
                totalTicketsSold,
                totalUsers,
                todaysRevenue != null ? todaysRevenue : 0.0,
                todaysTickets != null ? todaysTickets : 0L
        );
    }

    @Override
    @Transactional(readOnly = true)
    public RevenueStatsDTO getRevenueStats(LocalDate startDate, LocalDate endDate) {
        log.info("Fetching revenue statistics from {} to {}", startDate, endDate);

        // Устанавливаем диапазон дат
        if (startDate == null) {
            startDate = LocalDate.now().minusDays(30); // Последние 30 дней по умолчанию
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

        // Общая выручка за период
        Double totalRevenue = ticketRepository.calculateRevenueByPeriod(startDateTime, endDateTime);

        // Выручка по дням
        Map<LocalDate, Double> dailyRevenue = new LinkedHashMap<>();
        LocalDate currentDate = startDate;

        while (!currentDate.isAfter(endDate)) {
            LocalDateTime dayStart = currentDate.atStartOfDay();
            LocalDateTime dayEnd = currentDate.atTime(23, 59, 59);
            Double dayRevenue = ticketRepository.calculateRevenueByPeriod(dayStart, dayEnd);
            dailyRevenue.put(currentDate, dayRevenue != null ? dayRevenue : 0.0);
            currentDate = currentDate.plusDays(1);
        }

        // Выручка по фильмам
        Map<String, Double> revenueByMovie = new HashMap<>();
        List<Object[]> movieRevenueData = ticketRepository.getRevenueByMovie(startDateTime, endDateTime);

        for (Object[] data : movieRevenueData) {
            String movieTitle = (String) data[0];
            Double revenue = (Double) data[1];
            revenueByMovie.put(movieTitle, revenue != null ? revenue : 0.0);
        }

        // Средняя цена билета
        Long ticketsCount = ticketRepository.countSoldTicketsByDate(startDateTime, endDateTime);
        Double averageTicketPrice = ticketsCount != null && ticketsCount > 0
                ? (totalRevenue != null ? totalRevenue / ticketsCount : 0.0)
                : 0.0;

        return new RevenueStatsDTO(
                totalRevenue != null ? totalRevenue : 0.0,
                dailyRevenue,
                revenueByMovie,
                averageTicketPrice
        );
    }

    @Override
    @Transactional(readOnly = true)
    public PopularMoviesDTO getPopularMovies(int limit) {
        log.info("Fetching {} most popular movies", limit);

        // Билеты по фильмам
        Map<String, Long> ticketsByMovie = new LinkedHashMap<>();
        List<Object[]> movieTicketData = ticketRepository.getTicketCountByMovie(LocalDateTime.now().minusDays(30),
                LocalDateTime.now());

        int count = 0;
        for (Object[] data : movieTicketData) {
            if (count >= limit) break;
            String movieTitle = (String) data[0];
            Long ticketCount = (Long) data[1];
            ticketsByMovie.put(movieTitle, ticketCount != null ? ticketCount : 0L);
            count++;
        }

        // Выручка по фильмам
        Map<String, Double> revenueByMovie = new LinkedHashMap<>();
        List<Object[]> movieRevenueData = ticketRepository.getRevenueByMovie(LocalDateTime.now().minusDays(30),
                LocalDateTime.now());

        count = 0;
        for (Object[] data : movieRevenueData) {
            if (count >= limit) break;
            String movieTitle = (String) data[0];
            Double revenue = (Double) data[1];
            revenueByMovie.put(movieTitle, revenue != null ? revenue : 0.0);
            count++;
        }

        return new PopularMoviesDTO(ticketsByMovie, revenueByMovie);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SessionDTO> getLowAttendanceSessions(int threshold) {
        log.info("Fetching sessions with attendance below {}%", threshold);

        List<SessionDTO> lowAttendanceSessions = new ArrayList<>();
        List<Session> allSessions = sessionRepository.findAll();

        for (Session session : allSessions) {
            if (session.getStartTime().isAfter(LocalDateTime.now())) {
                // Для будущих сеансов считаем проданные билеты
                Long soldTickets = ticketRepository.countSoldTicketsBySession(session.getId());
                int totalSeats = session.getHall().getTotalRows() * session.getHall().getSeatsPerRow();

                if (totalSeats > 0) {
                    double attendancePercent = (soldTickets != null ? soldTickets.doubleValue() : 0.0) / totalSeats * 100;
                    if (attendancePercent < threshold) {
                        SessionDTO sessionDTO = cinemaMapper.toDTO(session);
                        sessionDTO.setAvailableSeats(totalSeats - (soldTickets != null ? soldTickets.intValue() : 0));
                        lowAttendanceSessions.add(sessionDTO);
                    }
                }
            }
        }

        return lowAttendanceSessions;
    }

    @Override
    @Transactional
    public void cancelSession(Long sessionId) {
        log.info("Cancelling session with ID: {}", sessionId);

        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Session not found with id: " + sessionId));

        // Проверяем, что сеанс еще не начался
        if (session.getStartTime().isBefore(LocalDateTime.now())) {
            throw new BusinessException("Cannot cancel session that has already started");
        }

        // Отменяем все билеты на этот сеанс
        List<Ticket> sessionTickets = ticketRepository.findBySessionId(sessionId);
        for (Ticket ticket : sessionTickets) {
            ticket.setStatus(Ticket.TicketStatus.CANCELLED);
            // TODO: Уведомить пользователей об отмене
        }

        ticketRepository.saveAll(sessionTickets);

        // Удаляем сеанс (или помечаем как отмененный)
        sessionRepository.delete(session);

        log.info("Session {} cancelled successfully. {} tickets refunded.",
                sessionId, sessionTickets.size());
    }

    @Override
    @Transactional
    public void rescheduleSession(Long sessionId, LocalDate newDate) {
        log.info("Rescheduling session {} to {}", sessionId, newDate);

        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Session not found with id: " + sessionId));

        // Проверяем, что сеанс еще не начался
        if (session.getStartTime().isBefore(LocalDateTime.now())) {
            throw new BusinessException("Cannot reschedule session that has already started");
        }

        // Сохраняем оригинальное время для расчета нового
        LocalTime originalTime = session.getStartTime().toLocalTime();
        LocalDateTime newDateTime = newDate.atTime(originalTime);

        // Проверяем доступность зала в новое время
        LocalDateTime newEndTime = newDateTime.plus(session.getMovie().getDuration());
        boolean hasOverlap = sessionRepository.hasOverlappingSessions(
                session.getHall().getId(), sessionId, newDateTime, newEndTime);

        if (hasOverlap) {
            throw new BusinessException("Hall is not available at the new time");
        }

        // Обновляем время сеанса
        session.setStartTime(newDateTime);
        session.setEndTime(newEndTime);

        sessionRepository.save(session);

        // TODO: Уведомить пользователей об изменении времени

        log.info("Session {} rescheduled to {}", sessionId, newDateTime);
    }



    @Override
    @Transactional(readOnly = true)
    public List<UserStatsDTO> getAllUsersStats() {
        log.info("Fetching statistics for all users");

        List<User> users = userRepository.findAll();
        List<UserStatsDTO> userStatsList = new ArrayList<>();

        for (User user : users) {
            Long userId = user.getId();

            // Количество билетов пользователя
            Long totalTickets = ticketRepository.countByUserId(userId);

            // Общая сумма потраченная пользователем
            Double totalSpent = ticketRepository.calculateUserSpending(userId);

            // Самый частый жанр (упрощенная логика)
            String favoriteGenre = calculateFavoriteGenre(userId);

            // Билеты за текущий месяц
            LocalDateTime monthStart = LocalDate.now().withDayOfMonth(1).atStartOfDay();
            LocalDateTime monthEnd = LocalDate.now().atTime(23, 59, 59);
            Long ticketsThisMonth = ticketRepository.countUserTicketsByPeriod(userId, monthStart, monthEnd);

            UserStatsDTO stats = new UserStatsDTO(
                    userId,
                    user.getFullName(),
                    totalTickets != null ? totalTickets.intValue() : 0,
                    totalSpent != null ? totalSpent : 0.0,
                    favoriteGenre,
                    ticketsThisMonth != null ? ticketsThisMonth.intValue() : 0
            );

            userStatsList.add(stats);
        }

        return userStatsList;
    }

    @Override
    @Transactional
    public void toggleUserStatus(Long userId, boolean active) {
        log.info("Setting user {} status to {}", userId, active ? "active" : "inactive");

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with id: " + userId));

        // TODO: Добавить поле active в сущность User
        // user.setActive(active);
        userRepository.save(user);

        log.info("User {} status updated", userId);
    }
// В AdminServiceImpl.java

    @Override
    @Transactional
    public void cleanupExpiredData() {
        log.info("Starting expired data cleanup");

        // Отменяем просроченные бронирования
        LocalDateTime reservationExpiryTime = LocalDateTime.now().minusMinutes(15);
        int cancelledReservations = ticketRepository.cancelExpiredReservations(reservationExpiryTime);

        // Удаляем завершенные сеансы из прошлого (более 7 дней назад)
        LocalDateTime pastDate = LocalDateTime.now().minusDays(7);
        List<Session> oldSessions = sessionRepository.findSessionsEndedBefore(pastDate);
        sessionRepository.deleteAll(oldSessions);

        log.info("Cleanup completed. Cancelled {} reservations, removed {} old sessions.",
                cancelledReservations, oldSessions.size());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SessionDTO> getSessionsByDateRange(LocalDate startDate, LocalDate endDate) {
        log.info("Fetching sessions from {} to {}", startDate, endDate);

        if (startDate == null) startDate = LocalDate.now();
        if (endDate == null) endDate = startDate.plusDays(7);

        List<Session> sessions = sessionRepository.findByDateRange(startDate, endDate);

        return sessions.stream()
                .map(cinemaMapper::toDTO)
                .collect(Collectors.toList());
    }


    @Override
    @Transactional(readOnly = true)
    public SystemHealthDTO getSystemHealth() {
        log.info("Checking system health");

        // Проверяем подключение к базе данных
        boolean dbConnected = checkDatabaseConnection();

        // Проверяем доступность репозиториев
        long movieCount = movieRepository.count();
        long userCount = userRepository.count();

        // Рассчитываем использование памяти
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        double memoryUsagePercent = (double) usedMemory / totalMemory * 100;

        // Получаем количество активных сеансов
        long activeSessions = sessionRepository.countByStartTimeAfter(LocalDateTime.now());

        return new SystemHealthDTO(
                dbConnected,
                movieCount,
                userCount,
                totalMemory,
                usedMemory,
                memoryUsagePercent,
                activeSessions,
                LocalDateTime.now()
        );
    }

    // Вспомогательные методы

    private boolean checkDatabaseConnection() {
        try {
            movieRepository.count(); // Простой запрос для проверки
            return true;
        } catch (Exception e) {
            log.error("Database connection check failed: {}", e.getMessage());
            return false;
        }
    }

    private String calculateFavoriteGenre(Long userId) {
        // TODO: Реализовать логику определения любимого жанра
        // На основе купленных билетов и просмотренных фильмов
        return "Action"; // Заглушка
    }
}