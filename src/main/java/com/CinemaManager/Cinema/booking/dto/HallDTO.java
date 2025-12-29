package com.CinemaManager.Cinema.booking.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HallDTO {
    private Long id;

    @NotBlank(message = "Номер зала обязателен")
    @Size(min = 1, max = 50, message = "Номер зала должен быть от 1 до 50 символов")
    private String hallNumber;

    @NotBlank(message = "Название зала обязательно")
    @Size(min = 1, max = 100, message = "Название зала должно быть от 1 до 100 символов")
    private String name;

    @NotNull(message = "Количество рядов обязательно")
    @Min(value = 1, message = "Минимум 1 ряд")
    @Max(value = 50, message = "Максимум 50 рядов")
    private Integer totalRows;

    @NotNull(message = "Количество мест в ряду обязательно")
    @Min(value = 1, message = "Минимум 1 место в ряду")
    @Max(value = 30, message = "Максимум 30 мест в ряду")
    private Integer seatsPerRow;

    @Size(max = 500, message = "Описание не должно превышать 500 символов")
    private String description;
}