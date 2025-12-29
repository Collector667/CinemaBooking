package com.CinemaManager.Cinema.booking.repository;

import com.CinemaManager.Cinema.booking.entity.Movie;
import com.CinemaManager.Cinema.booking.entity.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SessionRepository extends JpaRepository<Session, Long> {

    // 1. Базовые методы поиска
    List<Session> findByMovie(Movie movie);
    List<Session> findByMovieId(Long movieId);
    List<Session> findByHallId(Long hallId);

    // 2. Методы поиска по времени
    List<Session> findByStartTimeBetween(LocalDateTime start, LocalDateTime end);
    List<Session> findByStartTimeAfter(LocalDateTime startTime);
    List<Session> findByStartTimeBefore(LocalDateTime endTime);

    // 3. Методы поиска по цене
    List<Session> findByPrice(Double price);
    List<Session> findByPriceBetween(Double minPrice, Double maxPrice);
    List<Session> findByPriceGreaterThanEqual(Double minPrice);
    List<Session> findByPriceLessThanEqual(Double maxPrice);

    // 4. Комбинированные методы поиска
    List<Session> findByMovieIdAndHallId(Long movieId, Long hallId);

    @Query("SELECT s FROM Session s WHERE s.movie.id = :movieId AND s.hall.id = :hallId")
    List<Session> findByMovieAndHall(@Param("movieId") Long movieId, @Param("hallId") Long hallId);

    // 5. Методы поиска по дате (с использованием @Query для работы с датами)
    @Query("SELECT s FROM Session s WHERE DATE(s.startTime) = :date")
    List<Session> findByDate(@Param("date") LocalDate date);

    @Query("SELECT s FROM Session s WHERE DATE(s.startTime) = :date AND s.movie.id = :movieId")
    List<Session> findByMovieIdAndDate(@Param("movieId") Long movieId, @Param("date") LocalDate date);

    @Query("SELECT s FROM Session s WHERE DATE(s.startTime) >= :startDate AND DATE(s.startTime) <= :endDate")
    List<Session> findByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    // 6. Методы для проверки доступности зала
    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END FROM Session s " +
            "WHERE s.hall.id = :hallId " +
            "AND s.id != COALESCE(:excludeSessionId, -1) " +
            "AND ((s.startTime < :endTime AND s.endTime > :startTime))")
    boolean hasOverlappingSessions(@Param("hallId") Long hallId,
                                   @Param("excludeSessionId") Long excludeSessionId,
                                   @Param("startTime") LocalDateTime startTime,
                                   @Param("endTime") LocalDateTime endTime);

    // 7. Методы с жадной загрузкой (FETCH JOIN)
    @Query("SELECT s FROM Session s LEFT JOIN FETCH s.tickets WHERE s.id = :sessionId")
    Optional<Session> findSessionWithTickets(@Param("sessionId") Long sessionId);

    @Query("SELECT s FROM Session s JOIN FETCH s.movie JOIN FETCH s.hall WHERE s.id = :sessionId")
    Optional<Session> findSessionWithMovieAndHall(@Param("sessionId") Long sessionId);

    @Query("SELECT s FROM Session s JOIN FETCH s.movie m JOIN FETCH s.hall h LEFT JOIN FETCH s.tickets t " +
            "WHERE s.id = :sessionId")
    Optional<Session> findSessionWithAllDetails(@Param("sessionId") Long sessionId);

    // 8. Методы для аналитики и статистики
    @Query("SELECT s, COUNT(t) as soldTickets FROM Session s " +
            "LEFT JOIN s.tickets t ON t.status = 'SOLD' " +
            "GROUP BY s ORDER BY s.startTime")
    List<Object[]> findSessionsWithTicketCount();

    @Query("SELECT (h.totalRows * h.seatsPerRow - COUNT(t)) FROM Session s " +
            "JOIN s.hall h LEFT JOIN s.tickets t " +
            "WHERE s.id = :sessionId AND t.status != 'CANCELLED' " +
            "GROUP BY h.totalRows, h.seatsPerRow")
    Integer findAvailableSeatsCount(@Param("sessionId") Long sessionId);

    @Query("SELECT s FROM Session s WHERE s.movie.id = :movieId " +
            "AND s.startTime > :currentTime " +
            "ORDER BY s.startTime ASC")
    List<Session> findUpcomingSessionsByMovie(@Param("movieId") Long movieId,
                                              @Param("currentTime") LocalDateTime currentTime);

    // 9. Методы для административных функций
    @Query("SELECT s FROM Session s WHERE s.endTime < :dateTime")
    List<Session> findSessionsEndedBefore(@Param("dateTime") LocalDateTime dateTime);

    Long countByHallId(Long hallId);
    Long countByMovieId(Long movieId);
    Long countByStartTimeAfter(LocalDateTime dateTime);

    @Query("SELECT COUNT(s) FROM Session s WHERE s.startTime BETWEEN :start AND :end")
    Long countSessionsInPeriod(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // 10. Методы для бизнес-логики кинотеатра
    @Query("SELECT s FROM Session s WHERE s.startTime > CURRENT_TIMESTAMP " +
            "ORDER BY s.startTime ASC")
    List<Session> findUpcomingSessions();

    @Query("SELECT DISTINCT s FROM Session s " +
            "JOIN s.hall h " +
            "WHERE s.startTime > CURRENT_TIMESTAMP " +
            "AND (h.totalRows * h.seatsPerRow) > " +
            "(SELECT COUNT(t) FROM Ticket t WHERE t.session.id = s.id AND t.status != 'CANCELLED')")
    List<Session> findAvailableSessions();

    // 11. Методы для поиска по нескольким критериям
    @Query("SELECT s FROM Session s WHERE " +
            "(:movieId IS NULL OR s.movie.id = :movieId) " +
            "AND (:hallId IS NULL OR s.hall.id = :hallId) " +
            "AND (:startDate IS NULL OR DATE(s.startTime) >= :startDate) " +
            "AND (:endDate IS NULL OR DATE(s.startTime) <= :endDate) " +
            "AND (:minPrice IS NULL OR s.price >= :minPrice) " +
            "AND (:maxPrice IS NULL OR s.price <= :maxPrice)")
    List<Session> searchSessions(@Param("movieId") Long movieId,
                                 @Param("hallId") Long hallId,
                                 @Param("startDate") LocalDate startDate,
                                 @Param("endDate") LocalDate endDate,
                                 @Param("minPrice") Double minPrice,
                                 @Param("maxPrice") Double maxPrice);

    // 12. Дополнительные удобные методы
    @Query("SELECT DISTINCT DATE(s.startTime) FROM Session s " +
            "WHERE s.startTime > CURRENT_TIMESTAMP " +
            "ORDER BY DATE(s.startTime) ASC")
    List<LocalDate> findUpcomingSessionDates();

    @Query("SELECT DISTINCT s.movie FROM Session s " +
            "WHERE s.startTime > CURRENT_TIMESTAMP")
    List<Movie> findMoviesWithUpcomingSessions();
}