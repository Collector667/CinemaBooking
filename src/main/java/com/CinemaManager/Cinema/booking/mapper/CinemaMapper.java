package com.CinemaManager.Cinema.booking.mapper;

import com.CinemaManager.Cinema.booking.dto.*;
import com.CinemaManager.Cinema.booking.entity.*;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface CinemaMapper {
    // Movie mappings
    MovieDTO toDTO(Movie movie);
    @Mapping(target = "sessions", ignore = true)
    Movie toEntity(MovieDTO movieDTO);

    // Hall mappings
    HallDTO toDTO(Hall hall);
    @Mapping(target = "seats", ignore = true)
    @Mapping(target = "sessions", ignore = true)
    Hall toEntity(HallDTO hallDTO);

    // Session mappings
    @Mapping(target = "movieTitle", source = "movie.title")
    @Mapping(target = "hallName", source = "hall.name")
    SessionDTO toDTO(Session session);

    @Mapping(target = "movie", ignore = true)
    @Mapping(target = "hall", ignore = true)
    @Mapping(target = "tickets", ignore = true)
    Session toEntity(SessionDTO sessionDTO);

    // Ticket mappings
    @Mapping(target = "movieTitle", source = "session.movie.title")
    @Mapping(target = "sessionTime", source = "session.startTime")
    @Mapping(target = "hallName", source = "session.hall.name")
    @Mapping(target = "rowNumber", source = "seat.rowNumber")
    @Mapping(target = "seatNumber", source = "seat.seatNumber")
    @Mapping(target = "price", source = "session.price")
    @Mapping(target = "userName", source = "user.fullName")
    TicketDTO toDTO(Ticket ticket);

    // User mappings
    @Mapping(target = "fullName", expression = "java(user.getFirstName() + \" \" + user.getLastName())")
    UserDTO toDTO(User user);

    @Mapping(target = "tickets", ignore = true)
    User toEntity(UserDTO userDTO);
}