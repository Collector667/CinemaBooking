package com.CinemaManager.Cinema.booking.repository;

import com.CinemaManager.Cinema.booking.entity.Hall;
import com.CinemaManager.Cinema.booking.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Long> {

    // Найти все места в определенном зале
    List<Seat> findByHall(Hall hall);

    // Найти все места в зале по ID зала
    List<Seat> findByHallId(Long hallId);

    // Найти конкретное место по номеру ряда и места в зале
    Optional<Seat> findByHallAndRowNumberAndSeatNumber(Hall hall, Integer rowNumber, Integer seatNumber);

    // Найти место по уникальному коду
    Optional<Seat> findByUniqueCode(String uniqueCode);

    // Проверить существует ли место с таким рядом и местом в зале
    boolean existsByHallAndRowNumberAndSeatNumber(Hall hall, Integer rowNumber, Integer seatNumber);

    // Получить все места для нескольких ID мест
    List<Seat> findByIdIn(List<Long> seatIds);

    // Получить количество мест в зале
    @Query("SELECT COUNT(s) FROM Seat s WHERE s.hall.id = :hallId")
    Long countByHallId(@Param("hallId") Long hallId);

    // Получить занятые места на определенный сеанс
    @Query("SELECT s FROM Seat s JOIN Ticket t ON s.id = t.seat.id " +
            "WHERE t.session.id = :sessionId AND t.status != 'CANCELLED'")
    List<Seat> findOccupiedSeatsBySession(@Param("sessionId") Long sessionId);

    // Получить свободные места на определенный сеанс
    @Query("SELECT s FROM Seat s WHERE s.hall.id = " +
            "(SELECT ses.hall.id FROM Session ses WHERE ses.id = :sessionId) " +
            "AND s.id NOT IN " +
            "(SELECT t.seat.id FROM Ticket t WHERE t.session.id = :sessionId AND t.status != 'CANCELLED')")
    List<Seat> findAvailableSeatsBySession(@Param("sessionId") Long sessionId);
}