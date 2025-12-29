package com.CinemaManager.Cinema.booking.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionDTO {
    private Long id;

    @NotNull(message = "Время начала обязательно")
    @Future(message = "Время начала должно быть в будущем")
    private LocalDateTime startTime;

    private LocalDateTime endTime;

    @NotNull(message = "Цена обязательна")
    @Positive(message = "Цена должна быть положительной")
    private Double price;

    @NotNull(message = "ID фильма обязательно")
    private Long movieId;

    @NotNull(message = "ID зала обязательно")
    private Long hallId;

    // Дополнительные поля для удобства
    private String movieTitle;
    private String hallName;
    private Integer availableSeats;
}