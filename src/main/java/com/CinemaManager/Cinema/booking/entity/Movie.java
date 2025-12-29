package com.CinemaManager.Cinema.booking.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "movies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Movie extends BaseEntity {

    @Column(nullable = false)
    private String title;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private Duration duration;

    @Column(nullable = false)
    private String genre;

    @Column(name = "age_restriction", nullable = false)
    private Integer ageRestriction;

    @Column(name = "poster_url")
    private String posterUrl;

    @Column(name = "director")
    private String director;

    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Session> sessions = new ArrayList<>();
}