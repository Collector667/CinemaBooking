package com.CinemaManager.Cinema.booking.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeatDTO {
    private Long id;

    @NotNull(message = "Номер ряда обязателен")
    @Min(value = 1, message = "Номер ряда должен быть положительным")
    private Integer rowNumber;

    @NotNull(message = "Номер места обязателен")
    @Min(value = 1, message = "Номер места должен быть положительным")
    private Integer seatNumber;

    @NotNull(message = "ID зала обязательно")
    private Long hallId;

    private String hallName;
    private String status; // FREE, BOOKED, SOLD
}