package com.CinemaManager.Cinema.booking.controller;

import com.CinemaManager.Cinema.booking.dto.ApiResponse;
import com.CinemaManager.Cinema.booking.dto.MovieDTO;
import com.CinemaManager.Cinema.booking.service.MovieService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/movies")
@RequiredArgsConstructor
public class MovieController {

    private final MovieService movieService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<MovieDTO>>> getAllMovies() {
        List<MovieDTO> movies = movieService.getAllMovies();
        return ResponseEntity.ok(ApiResponse.success(movies));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MovieDTO>> getMovieById(@PathVariable Long id) {
        MovieDTO movie = movieService.getMovieById(id);
        return ResponseEntity.ok(ApiResponse.success(movie));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<MovieDTO>> createMovie(
            @Valid @RequestBody MovieDTO movieDTO) {
        MovieDTO createdMovie = movieService.createMovie(movieDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Фильм успешно создан", createdMovie));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<MovieDTO>> updateMovie(
            @PathVariable Long id,
            @Valid @RequestBody MovieDTO movieDTO) {
        MovieDTO updatedMovie = movieService.updateMovie(id, movieDTO);
        return ResponseEntity.ok(ApiResponse.success("Фильм успешно обновлен", updatedMovie));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteMovie(@PathVariable Long id) {
        movieService.deleteMovie(id);
        return ResponseEntity.ok(ApiResponse.success("Фильм успешно удален", null));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<MovieDTO>>> searchMovies(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String genre,
            @RequestParam(required = false) Integer ageRestriction) {
        List<MovieDTO> movies = movieService.searchMovies(title, genre, ageRestriction);
        return ResponseEntity.ok(ApiResponse.success(movies));
    }

    @GetMapping("/now-playing")
    public ResponseEntity<ApiResponse<List<MovieDTO>>> getNowPlayingMovies() {
        List<MovieDTO> movies = movieService.getNowPlayingMovies();
        return ResponseEntity.ok(ApiResponse.success(movies));
    }
}