package com.CinemaManager.Cinema.booking.dto;

import com.CinemaManager.Cinema.booking.entity.Ticket.TicketStatus;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketDTO {
    private Long id;
    private String ticketNumber;
    private TicketStatus status;
    private LocalDateTime purchaseTime;

    @NotNull(message = "ID сеанса обязательно")
    private Long sessionId;

    @NotNull(message = "ID места обязательно")
    private Long seatId;

    private Long userId;

    // Дополнительные поля для отображения
    private String movieTitle;
    private LocalDateTime sessionTime;
    private String hallName;
    private Integer rowNumber;
    private Integer seatNumber;
    private Double price;
    private String userName;
}