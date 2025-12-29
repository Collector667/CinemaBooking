package com.CinemaManager.Cinema.booking.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.time.Duration;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovieDTO {
    private Long id;

    @NotBlank(message = "Название фильма обязательно")
    @Size(min = 1, max = 255, message = "Название должно быть от 1 до 255 символов")
    private String title;

    @Size(max = 1000, message = "Описание не должно превышать 1000 символов")
    private String description;

    @NotNull(message = "Продолжительность обязательна")
    @Positive(message = "Продолжительность должна быть положительной")
    private Duration duration;

    @NotBlank(message = "Жанр обязателен")
    private String genre;

    @NotNull(message = "Возрастное ограничение обязательно")
    @Min(value = 0, message = "Возрастное ограничение не может быть отрицательным")
    @Max(value = 21, message = "Возрастное ограничение не может превышать 21")
    private Integer ageRestriction;

    private String posterUrl;
    private String director;
}