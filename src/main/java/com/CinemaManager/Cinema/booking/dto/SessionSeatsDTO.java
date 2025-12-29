package com.CinemaManager.Cinema.booking.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionSeatsDTO {
    private Long sessionId;
    private String movieTitle;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Double price;
    private String hallName;
    private Integer totalRows;
    private Integer seatsPerRow;
    private List<SeatStatusDTO> seats; // Статус каждого места
}