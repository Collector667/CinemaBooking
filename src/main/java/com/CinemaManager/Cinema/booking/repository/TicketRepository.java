package com.CinemaManager.Cinema.booking.repository;

import com.CinemaManager.Cinema.booking.entity.Ticket;
import com.CinemaManager.Cinema.booking.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    List<Ticket> findByUser(User user);
    List<Ticket> findByUserId(Long userId);
    List<Ticket> findBySessionId(Long sessionId);
    Optional<Ticket> findByTicketNumber(String ticketNumber);
    List<Ticket> findByStatus(Ticket.TicketStatus status);
    List<Ticket> findBySessionIdAndStatus(Long sessionId, Ticket.TicketStatus status);
    List<Ticket> findByUserIdAndStatus(Long userId, Ticket.TicketStatus status);
    Optional<Ticket> findBySessionIdAndSeatId(Long sessionId, Long seatId);
    List<Ticket> findByIdIn(List<Long> ticketIds);

    Long countByUserId(Long userId);
    Long countByStatus(Ticket.TicketStatus status);

    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.user.id = :userId")
    Long countByUserIdCustom(@Param("userId") Long userId);

    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.session.id = :sessionId AND t.status = 'SOLD'")
    Long countSoldTicketsBySession(@Param("sessionId") Long sessionId);

    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.status = 'SOLD' " +
            "AND t.purchaseTime BETWEEN :startDate AND :endDate")
    Long countSoldTicketsByDate(@Param("startDate") LocalDateTime startDate,
                                @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.user.id = :userId " +
            "AND t.status = 'SOLD' " +
            "AND t.purchaseTime BETWEEN :startDate AND :endDate")
    Long countUserTicketsByPeriod(@Param("userId") Long userId,
                                  @Param("startDate") LocalDateTime startDate,
                                  @Param("endDate") LocalDateTime endDate);

    // 3. Методы для аналитики и отчетов
    @Query("SELECT m.title, COALESCE(SUM(s.price), 0) FROM Ticket t " +
            "JOIN t.session s " +
            "JOIN s.movie m " +
            "WHERE t.status = 'SOLD' " +
            "AND t.purchaseTime BETWEEN :startDate AND :endDate " +
            "GROUP BY m.title ORDER BY SUM(s.price) DESC")
    List<Object[]> getRevenueByMovie(@Param("startDate") LocalDateTime startDate,
                                     @Param("endDate") LocalDateTime endDate);

    @Query("SELECT m.title, COUNT(t) as ticketCount FROM Ticket t " +
            "JOIN t.session s " +
            "JOIN s.movie m " +
            "WHERE t.status = 'SOLD' " +
            "AND t.purchaseTime BETWEEN :startDate AND :endDate " +
            "GROUP BY m.title ORDER BY COUNT(t) DESC")
    List<Object[]> getTicketCountByMovie(@Param("startDate") LocalDateTime startDate,
                                         @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COALESCE(SUM(s.price), 0) FROM Ticket t " +
            "JOIN t.session s WHERE t.status = 'SOLD' " +
            "AND t.purchaseTime BETWEEN :startDate AND :endDate")
    Double calculateRevenueByPeriod(@Param("startDate") LocalDateTime startDate,
                                    @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COALESCE(SUM(s.price), 0) FROM Ticket t " +
            "JOIN t.session s " +
            "WHERE t.user.id = :userId AND t.status = 'SOLD'")
    Double calculateUserSpending(@Param("userId") Long userId);

    // 4. Методы для проверки доступности и работы с данными
    @Query("SELECT CASE WHEN COUNT(t) = 0 THEN true ELSE false END FROM Ticket t " +
            "WHERE t.session.id = :sessionId " +
            "AND t.seat.id IN :seatIds " +
            "AND t.status != 'CANCELLED'")
    boolean areSeatsAvailable(@Param("sessionId") Long sessionId,
                              @Param("seatIds") List<Long> seatIds);

    @Query("SELECT t FROM Ticket t WHERE t.status = 'BOOKED' " +
            "AND t.createdAt < :expirationTime")
    List<Ticket> findExpiredReservations(@Param("expirationTime") LocalDateTime expirationTime);

    // 5. Методы для загрузки связанных данных (жадная загрузка)
    @Query("SELECT t FROM Ticket t " +
            "JOIN FETCH t.session s " +
            "JOIN FETCH s.movie " +
            "JOIN FETCH s.hall " +
            "JOIN FETCH t.seat " +
            "WHERE t.id = :ticketId")
    Optional<Ticket> findTicketWithDetails(@Param("ticketId") Long ticketId);

    @Query("SELECT t FROM Ticket t " +
            "JOIN FETCH t.session s " +
            "JOIN FETCH s.movie " +
            "JOIN FETCH s.hall " +
            "JOIN FETCH t.seat " +
            "WHERE t.user.id = :userId " +
            "ORDER BY s.startTime DESC")
    List<Ticket> findUserTicketsWithDetails(@Param("userId") Long userId);

    @Query("SELECT t FROM Ticket t WHERE t.status = 'SOLD' " +
            "AND t.purchaseTime BETWEEN :startDate AND :endDate")
    List<Ticket> findSoldTicketsByPeriod(@Param("startDate") LocalDateTime startDate,
                                         @Param("endDate") LocalDateTime endDate);

    // 6. Модифицирующие операции
    @Modifying
    @Transactional
    @Query("UPDATE Ticket t SET t.status = :newStatus WHERE t.id IN :ticketIds")
    int updateTicketStatus(@Param("ticketIds") List<Long> ticketIds,
                           @Param("newStatus") Ticket.TicketStatus newStatus);

    @Modifying
    @Transactional
    @Query("UPDATE Ticket t SET t.status = 'CANCELLED', t.user = null " +
            "WHERE t.status = 'BOOKED' AND t.createdAt < :expirationTime")
    int cancelExpiredReservations(@Param("expirationTime") LocalDateTime expirationTime);

    // 7. Дополнительные аналитические методы
    @Query("SELECT COUNT(t) FROM Ticket t " +
            "WHERE t.session.id = :sessionId " +
            "AND t.status IN ('SOLD', 'BOOKED')")
    Long countOccupiedSeatsBySession(@Param("sessionId") Long sessionId);

    @Query("SELECT s.startTime, COUNT(t) as ticketCount FROM Ticket t " +
            "JOIN t.session s " +
            "WHERE t.status = 'SOLD' " +
            "AND t.purchaseTime BETWEEN :startDate AND :endDate " +
            "GROUP BY s.startTime " +
            "ORDER BY s.startTime")
    List<Object[]> getTicketsByHour(@Param("startDate") LocalDateTime startDate,
                                    @Param("endDate") LocalDateTime endDate);
}