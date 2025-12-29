package com.CinemaManager.Cinema.booking.service.impl;

import com.CinemaManager.Cinema.booking.dto.MovieDTO;
import com.CinemaManager.Cinema.booking.entity.Movie;
import com.CinemaManager.Cinema.booking.exception.ResourceNotFoundException;
import com.CinemaManager.Cinema.booking.mapper.CinemaMapper;
import com.CinemaManager.Cinema.booking.repository.MovieRepository;
import com.CinemaManager.Cinema.booking.service.MovieService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MovieServiceImpl implements MovieService {

    private final MovieRepository movieRepository;
    private final CinemaMapper cinemaMapper;

    @Override
    @Transactional
    public MovieDTO createMovie(MovieDTO movieDTO) {
        log.info("Creating new movie: {}", movieDTO.getTitle());
        Movie movie = cinemaMapper.toEntity(movieDTO);
        Movie savedMovie = movieRepository.save(movie);
        log.info("Movie created with ID: {}", savedMovie.getId());
        return cinemaMapper.toDTO(savedMovie);
    }

    @Override
    @Transactional(readOnly = true)
    public MovieDTO getMovieById(Long id) {
        log.debug("Fetching movie with ID: {}", id);
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found with id: " + id));
        return cinemaMapper.toDTO(movie);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MovieDTO> getAllMovies() {
        log.debug("Fetching all movies");
        List<Movie> movies = movieRepository.findAll();
        return movies.stream()
                .map(cinemaMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public MovieDTO updateMovie(Long id, MovieDTO movieDTO) {
        log.info("Updating movie with ID: {}", id);
        Movie existingMovie = movieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found with id: " + id));

        // Обновляем только изменяемые поля
        existingMovie.setTitle(movieDTO.getTitle());
        existingMovie.setDescription(movieDTO.getDescription());
        existingMovie.setDuration(movieDTO.getDuration());
        existingMovie.setGenre(movieDTO.getGenre());
        existingMovie.setAgeRestriction(movieDTO.getAgeRestriction());
        existingMovie.setPosterUrl(movieDTO.getPosterUrl());
        existingMovie.setDirector(movieDTO.getDirector());

        Movie updatedMovie = movieRepository.save(existingMovie);
        log.info("Movie updated with ID: {}", updatedMovie.getId());
        return cinemaMapper.toDTO(updatedMovie);
    }

    @Override
    @Transactional
    public void deleteMovie(Long id) {
        log.info("Deleting movie with ID: {}", id);
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found with id: " + id));
        movieRepository.delete(movie);
        log.info("Movie deleted with ID: {}", id);
    }


    @Override
    public List<MovieDTO> searchMovies(String title, String genre, Integer ageRestriction) {
        // Используем универсальный метод с @Query
        List<Movie> movies = movieRepository.searchMovies(title, genre, ageRestriction);

        return movies.stream()
                .map(cinemaMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MovieDTO> getNowPlayingMovies() {
        log.debug("Fetching now playing movies");
        LocalDateTime now = LocalDateTime.now();
        List<Movie> activeMovies = movieRepository.findActiveMovies(now);
        return activeMovies.stream()
                .map(cinemaMapper::toDTO)
                .collect(Collectors.toList());
    }
}