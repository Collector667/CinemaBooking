package com.CinemaManager.Cinema.booking.service;

import com.CinemaManager.Cinema.booking.dto.MovieDTO;

import java.util.List;

public interface MovieService {
    MovieDTO createMovie(MovieDTO movieDTO);
    MovieDTO getMovieById(Long id);
    List<MovieDTO> getAllMovies();
    MovieDTO updateMovie(Long id, MovieDTO movieDTO);
    void deleteMovie(Long id);
    List<MovieDTO> searchMovies(String title, String genre, Integer ageRestriction);
    List<MovieDTO> getNowPlayingMovies();
}