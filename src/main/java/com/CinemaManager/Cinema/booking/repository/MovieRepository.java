package com.CinemaManager.Cinema.booking.repository;

import com.CinemaManager.Cinema.booking.entity.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {

    // 1. Базовые методы поиска
    List<Movie> findByTitleContainingIgnoreCase(String title);
    List<Movie> findByGenre(String genre);
    List<Movie> findByAgeRestriction(Integer ageRestriction);
    List<Movie> findByGenreIn(List<String> genres);

    // 2. Комбинированные методы поиска (для всех комбинаций)
    List<Movie> findByTitleContainingIgnoreCaseAndGenre(String title, String genre);
    List<Movie> findByTitleContainingIgnoreCaseAndAgeRestriction(String title, Integer ageRestriction);
    List<Movie> findByGenreAndAgeRestriction(String genre, Integer ageRestriction);
    List<Movie> findByTitleContainingIgnoreCaseAndGenreAndAgeRestriction(
            String title, String genre, Integer ageRestriction);

    // 3. Универсальный метод поиска с @Query (альтернатива)
    @Query("SELECT m FROM Movie m WHERE " +
            "(:title IS NULL OR LOWER(m.title) LIKE LOWER(CONCAT('%', :title, '%'))) " +
            "AND (:genre IS NULL OR m.genre = :genre) " +
            "AND (:ageRestriction IS NULL OR m.ageRestriction = :ageRestriction)")
    List<Movie> searchMovies(@Param("title") String title,
                             @Param("genre") String genre,
                             @Param("ageRestriction") Integer ageRestriction);

    // 4. Дополнительные методы
    @Query("SELECT DISTINCT m FROM Movie m JOIN m.sessions s WHERE s.startTime > :currentTime")
    List<Movie> findActiveMovies(@Param("currentTime") LocalDateTime currentTime);

    @Query("SELECT m FROM Movie m LEFT JOIN FETCH m.sessions WHERE m.id = :movieId")
    Optional<Movie> findMovieWithSessions(@Param("movieId") Long movieId);

    @Query("SELECT m, COUNT(s) as sessionCount FROM Movie m LEFT JOIN m.sessions s " +
            "GROUP BY m ORDER BY sessionCount DESC")
    List<Object[]> findPopularMovies();

    // 5. Методы для фильтрации по диапазону продолжительности
    List<Movie> findByDurationBetween(java.time.Duration min, java.time.Duration max);

    // 6. Метод для поиска по режиссеру
    List<Movie> findByDirectorContainingIgnoreCase(String director);
}