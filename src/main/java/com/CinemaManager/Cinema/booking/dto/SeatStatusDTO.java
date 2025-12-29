package com.CinemaManager.Cinema.booking.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeatStatusDTO {
    private Long seatId;
    private Integer rowNumber;
    private Integer seatNumber;
    private String status; // FREE, BOOKED, SOLD
}