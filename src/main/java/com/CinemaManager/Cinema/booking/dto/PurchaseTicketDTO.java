package com.CinemaManager.Cinema.booking.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseTicketDTO {
    @NotNull(message = "ID сеанса обязательно")
    private Long sessionId;

    @NotEmpty(message = "Должно быть выбрано хотя бы одно место")
    private List<Long> seatIds;

    private Long userId; // Может быть null, если берется из контекста безопасности
}