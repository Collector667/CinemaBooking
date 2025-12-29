package com.CinemaManager.Cinema.booking.controller;

import com.CinemaManager.Cinema.booking.dto.MovieDTO;
import com.CinemaManager.Cinema.booking.service.MovieService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.testng.annotations.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MovieController.class)
class MovieControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MovieService movieService;

    @Test
    void getAllMovies_ShouldReturnMovies() throws Exception {
        // Arrange
        MovieDTO movie1 = MovieDTO.builder()
                .id(1L)
                .title("The Matrix")
                .genre("Sci-Fi")
                .duration(Duration.ofMinutes(136))
                .ageRestriction(16)
                .build();

        MovieDTO movie2 = MovieDTO.builder()
                .id(2L)
                .title("Inception")
                .genre("Action")
                .duration(Duration.ofMinutes(148))
                .ageRestriction(12)
                .build();

        List<MovieDTO> movies = Arrays.asList(movie1, movie2);

        when(movieService.getAllMovies()).thenReturn(movies);

        // Act & Assert
        mockMvc.perform(get("/api/movies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].title").value("The Matrix"))
                .andExpect(jsonPath("$.data[1].title").value("Inception"));
    }

    @Test
    void getMovieById_WhenMovieExists_ShouldReturnMovie() throws Exception {
        // Arrange
        MovieDTO movie = MovieDTO.builder()
                .id(1L)
                .title("The Matrix")
                .genre("Sci-Fi")
                .duration(Duration.ofMinutes(136))
                .ageRestriction(16)
                .build();

        when(movieService.getMovieById(1L)).thenReturn(movie);

        // Act & Assert
        mockMvc.perform(get("/api/movies/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("The Matrix"))
                .andExpect(jsonPath("$.data.genre").value("Sci-Fi"));
    }

    @Test
    void createMovie_ValidInput_ShouldReturnCreatedMovie() throws Exception {
        // Arrange
        MovieDTO requestDTO = MovieDTO.builder()
                .title("New Movie")
                .description("Description")
                .duration(Duration.ofMinutes(120))
                .genre("Drama")
                .ageRestriction(12)
                .build();

        MovieDTO responseDTO = MovieDTO.builder()
                .id(1L)
                .title("New Movie")
                .description("Description")
                .duration(Duration.ofMinutes(120))
                .genre("Drama")
                .ageRestriction(12)
                .build();

        when(movieService.createMovie(any(MovieDTO.class))).thenReturn(responseDTO);

        // Act & Assert
        mockMvc.perform(post("/api/movies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Фильм успешно создан"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.title").value("New Movie"));
    }

    @Test
    void createMovie_InvalidInput_ShouldReturnBadRequest() throws Exception {
        // Arrange - MovieDTO без обязательных полей
        MovieDTO invalidMovie = MovieDTO.builder()
                .title("")  // Пустой заголовок - невалидно
                .duration(null)  // Отсутствует продолжительность
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/movies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidMovie)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateMovie_ValidInput_ShouldReturnUpdatedMovie() throws Exception {
        // Arrange
        MovieDTO updateDTO = MovieDTO.builder()
                .title("Updated Title")
                .description("Updated description")
                .duration(Duration.ofMinutes(130))
                .genre("Sci-Fi")
                .ageRestriction(16)
                .build();

        MovieDTO updatedMovie = MovieDTO.builder()
                .id(1L)
                .title("Updated Title")
                .description("Updated description")
                .duration(Duration.ofMinutes(130))
                .genre("Sci-Fi")
                .ageRestriction(16)
                .build();

        when(movieService.updateMovie(eq(1L), any(MovieDTO.class))).thenReturn(updatedMovie);

        // Act & Assert
        mockMvc.perform(put("/api/movies/{id}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Фильм успешно обновлен"))
                .andExpect(jsonPath("$.data.title").value("Updated Title"));
    }

    @Test
    void deleteMovie_ShouldReturnSuccess() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/movies/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Фильм успешно удален"));
    }

    @Test
    void searchMovies_WithParameters_ShouldReturnFilteredResults() throws Exception {
        // Arrange
        MovieDTO movie = MovieDTO.builder()
                .id(1L)
                .title("The Matrix")
                .genre("Sci-Fi")
                .ageRestriction(16)
                .build();

        when(movieService.searchMovies("matrix", "Sci-Fi", 16))
                .thenReturn(Arrays.asList(movie));

        // Act & Assert
        mockMvc.perform(get("/api/movies/search")
                        .param("title", "matrix")
                        .param("genre", "Sci-Fi")
                        .param("ageRestriction", "16"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].title").value("The Matrix"));
    }
}