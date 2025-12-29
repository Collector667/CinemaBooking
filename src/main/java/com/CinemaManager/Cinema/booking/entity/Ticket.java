package com.CinemaManager.Cinema.booking.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tickets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ticket extends BaseEntity {

    @Column(name = "ticket_number", unique = true, nullable = false)
    private String ticketNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TicketStatus status;

    @Column(name = "purchase_time")
    private LocalDateTime purchaseTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private Session session;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seat_id", nullable = false)
    private Seat seat;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @PrePersist
    private void generateTicketNumber() {
        if (this.ticketNumber == null) {
            this.ticketNumber = "TKT-" + System.currentTimeMillis() + "-" +
                    (int)(Math.random() * 1000);
        }
        if (this.status == null) {
            this.status = TicketStatus.AVAILABLE;
        }
    }

    @PreUpdate
    private void updatePurchaseTime() {
        if (this.status == TicketStatus.SOLD && this.purchaseTime == null) {
            this.purchaseTime = LocalDateTime.now();
        }
    }

    public enum TicketStatus {
        AVAILABLE,
        BOOKED,
        SOLD,
        CANCELLED
    }
}