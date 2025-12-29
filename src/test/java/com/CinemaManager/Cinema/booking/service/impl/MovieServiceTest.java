package com.CinemaManager.Cinema.booking.service.impl;

import com.CinemaManager.Cinema.booking.dto.MovieDTO;
import com.CinemaManager.Cinema.booking.entity.Movie;
import com.CinemaManager.Cinema.booking.exception.ResourceNotFoundException;
import com.CinemaManager.Cinema.booking.mapper.CinemaMapper;
import com.CinemaManager.Cinema.booking.repository.MovieRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MovieServiceTest {

    @Mock
    private MovieRepository movieRepository;

    @Mock
    private CinemaMapper cinemaMapper;

    @InjectMocks
    private MovieServiceImpl movieService;

    private Movie testMovie;
    private MovieDTO testMovieDTO;

    @BeforeEach
    void setUp() {
        testMovie = Movie.builder()
                .title("The Matrix")
                .description("Sci-fi movie")
                .duration(Duration.ofMinutes(136))
                .genre("Sci-Fi")
                .ageRestriction(16)
                .director("Lana Wachowski")
                .build();

        testMovieDTO = MovieDTO.builder()
                .id(1L)
                .title("The Matrix")
                .description("Sci-fi movie")
                .duration(Duration.ofMinutes(136))
                .genre("Sci-Fi")
                .ageRestriction(16)
                .director("Lana Wachowski")
                .build();
    }

    @Test
    void getMovieById_WhenMovieExists_ShouldReturnMovieDTO() {
        when(movieRepository.findById(1L)).thenReturn(Optional.of(testMovie));
        when(cinemaMapper.toDTO(testMovie)).thenReturn(testMovieDTO);

        MovieDTO result = movieService.getMovieById(1L);

        assertNotNull(result);
        assertEquals("The Matrix", result.getTitle());
        verify(movieRepository).findById(1L);
    }

    @Test
    void getMovieById_WhenMovieNotExists_ShouldThrowException() {
        when(movieRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> movieService.getMovieById(999L));
        verify(movieRepository).findById(999L);
    }

    @Test
    void getAllMovies_ShouldReturnAllMovies() {
        Movie movie2 = Movie.builder().title("Inception").build();
        MovieDTO movieDTO2 = MovieDTO.builder().id(2L).title("Inception").build();

        when(movieRepository.findAll()).thenReturn(Arrays.asList(testMovie, movie2));
        when(cinemaMapper.toDTO(testMovie)).thenReturn(testMovieDTO);
        when(cinemaMapper.toDTO(movie2)).thenReturn(movieDTO2);

        List<MovieDTO> result = movieService.getAllMovies();

        assertEquals(2, result.size());
        verify(movieRepository).findAll();
    }

    @Test
    void createMovie_ShouldSaveAndReturnMovieDTO() {
        when(cinemaMapper.toEntity(testMovieDTO)).thenReturn(testMovie);
        when(movieRepository.save(testMovie)).thenReturn(testMovie);
        when(cinemaMapper.toDTO(testMovie)).thenReturn(testMovieDTO);

        MovieDTO result = movieService.createMovie(testMovieDTO);

        assertNotNull(result);
        verify(movieRepository).save(testMovie);
    }

    @Test
    void updateMovie_WhenMovieExists_ShouldUpdateAndReturnDTO() {
        when(movieRepository.findById(1L)).thenReturn(Optional.of(testMovie));
        when(movieRepository.save(any(Movie.class))).thenReturn(testMovie);
        when(cinemaMapper.toDTO(testMovie)).thenReturn(testMovieDTO);

        MovieDTO result = movieService.updateMovie(1L, testMovieDTO);

        assertNotNull(result);
        verify(movieRepository).findById(1L);
        verify(movieRepository).save(testMovie);
    }

    @Test
    void deleteMovie_WhenMovieExists_ShouldDeleteMovie() {
        when(movieRepository.findById(1L)).thenReturn(Optional.of(testMovie));

        movieService.deleteMovie(1L);

        verify(movieRepository).findById(1L);
        verify(movieRepository).delete(testMovie);
    }
}