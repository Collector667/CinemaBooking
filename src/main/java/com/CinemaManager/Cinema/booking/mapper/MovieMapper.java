package com.CinemaManager.Cinema.booking.mapper;

import com.CinemaManager.Cinema.booking.dto.MovieDTO;
import com.CinemaManager.Cinema.booking.entity.Movie;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface MovieMapper {
    MovieDTO toDTO(Movie movie);

    @Mapping(target = "sessions", ignore = true)
    Movie toEntity(MovieDTO movieDTO);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateMovieFromDTO(MovieDTO movieDTO, @MappingTarget Movie movie);
}