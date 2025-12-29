package com.CinemaManager.Cinema.booking.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketPurchaseResponseDTO {
    private boolean success;
    private String message;
    private LocalDateTime purchaseTime;
    private Double totalAmount;
    private List<TicketInfoDTO> purchasedTickets;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TicketInfoDTO {
        private String ticketNumber;
        private String movieTitle;
        private LocalDateTime sessionTime;
        private String hallName;
        private Integer rowNumber;
        private Integer seatNumber;
        private Double price;
    }
}