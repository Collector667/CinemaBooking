package com.CinemaManager.Cinema.booking.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "seats")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Seat extends BaseEntity {

    @Column(name = "row_num", nullable = false)
    private Integer rowNumber;

    @Column(name = "seat_number", nullable = false)
    private Integer seatNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hall_id", nullable = false)
    private Hall hall;

    @OneToMany(mappedBy = "seat", cascade = CascadeType.ALL)
    private java.util.List<Ticket> tickets;

    @Column(name = "unique_code", unique = true)
    private String uniqueCode;

    @PrePersist
    @PreUpdate
    private void generateUniqueCode() {
        if (hall != null) {
            this.uniqueCode = hall.getId() + "-" + rowNumber + "-" + seatNumber;
        }
    }
}