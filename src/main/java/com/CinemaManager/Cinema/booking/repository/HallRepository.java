package com.CinemaManager.Cinema.booking.repository;

import com.CinemaManager.Cinema.booking.entity.Hall;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HallRepository extends JpaRepository<Hall, Long> {

    // Найти зал по номеру/названию
    Optional<Hall> findByHallNumber(String hallNumber);

    // Проверить существование зала по номеру
    boolean existsByHallNumber(String hallNumber);

    // Найти залы по количеству мест
    List<Hall> findByTotalRowsAndSeatsPerRow(Integer totalRows, Integer seatsPerRow);

    // Получить зал со всеми местами (жадная загрузка)
    @Query("SELECT h FROM Hall h LEFT JOIN FETCH h.seats WHERE h.id = :hallId")
    Optional<Hall> findHallWithSeats(@Param("hallId") Long hallId);

    // Получить зал со всеми сеансами (жадная загрузка)
    @Query("SELECT h FROM Hall h LEFT JOIN FETCH h.sessions WHERE h.id = :hallId")
    Optional<Hall> findHallWithSessions(@Param("hallId") Long hallId);

    // Получить все залы с количеством мест
    @Query("SELECT h, (h.totalRows * h.seatsPerRow) as totalSeats FROM Hall h")
    List<Object[]> findAllHallsWithTotalSeats();

    // Найти доступные залы для определенного времени (не занятые другими сеансами)
    @Query("SELECT h FROM Hall h WHERE h.id NOT IN " +
            "(SELECT s.hall.id FROM Session s WHERE s.startTime <= :endTime AND s.endTime >= :startTime)")
    List<Hall> findAvailableHalls(@Param("startTime") java.time.LocalDateTime startTime,
                                  @Param("endTime") java.time.LocalDateTime endTime);
}