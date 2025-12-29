package com.CinemaManager.Cinema.booking.controller;

import com.CinemaManager.Cinema.booking.dto.MovieDTO;
import com.CinemaManager.Cinema.booking.entity.Movie;
import com.CinemaManager.Cinema.booking.repository.MovieRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class MovieControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MovieRepository movieRepository;

    private Movie testMovie;

    @BeforeEach
    void setUp() {
        movieRepository.deleteAll();

        testMovie = Movie.builder()
                .title("Интерстеллар")
                .description("Фантастический фильм о космических путешествиях")
                .duration(Duration.ofMinutes(169))
                .genre("Фантастика")
                .ageRestriction(12)
                .director("Кристофер Нолан")
                .posterUrl("https://example.com/poster.jpg")
                .build();
        testMovie = movieRepository.save(testMovie);
    }

    @Test
    void getAllMovies_ShouldReturnMovieList() throws Exception {
        mockMvc.perform(get("/api/movies"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].title").value("Интерстеллар"));
    }

    @Test
    void getMovieById_WhenMovieExists_ShouldReturnMovie() throws Exception {
        mockMvc.perform(get("/api/movies/{id}", testMovie.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(testMovie.getId()))
                .andExpect(jsonPath("$.data.title").value("Интерстеллар"))
                .andExpect(jsonPath("$.data.genre").value("Фантастика"));
    }







    @Test
    void deleteMovie_WhenMovieExists_ShouldReturnSuccess() throws Exception {
        mockMvc.perform(delete("/api/movies/{id}", testMovie.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Фильм успешно удален"));
    }

    @Test
    void searchMovies_ByTitle_ShouldReturnMatchingMovies() throws Exception {
        // Создаем еще один фильм
        Movie movie2 = Movie.builder()
                .title("Матрица")
                .description("Фильм о виртуальной реальности")
                .duration(Duration.ofMinutes(136))
                .genre("Фантастика")
                .ageRestriction(16)
                .director("Вачовски")
                .build();
        movieRepository.save(movie2);

        mockMvc.perform(get("/api/movies/search")
                        .param("title", "интерстеллар"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].title").value("Интерстеллар"));
    }

    @Test
    void searchMovies_ByGenre_ShouldReturnMatchingMovies() throws Exception {
        mockMvc.perform(get("/api/movies/search")
                        .param("genre", "Фантастика"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1));
    }

    @Test
    void searchMovies_ByMultipleCriteria_ShouldReturnMatchingMovies() throws Exception {
        mockMvc.perform(get("/api/movies/search")
                        .param("title", "интерстеллар")
                        .param("genre", "Фантастика")
                        .param("ageRestriction", "12"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1));
    }

    @Test
    void getNowPlayingMovies_ShouldReturnActiveMovies() throws Exception {
        mockMvc.perform(get("/api/movies/now-playing"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }
}