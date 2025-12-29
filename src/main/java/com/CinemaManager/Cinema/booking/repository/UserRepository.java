package com.CinemaManager.Cinema.booking.repository;

import com.CinemaManager.Cinema.booking.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findByRole(User.Role role);

    List<User> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(String firstName, String lastName);

    // Получить пользователя с его билетами (жадная загрузка)
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.tickets WHERE u.id = :userId")
    Optional<User> findUserWithTickets(@Param("userId") Long userId);

    // Получить количество пользователей по роли
    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role")
    Long countByRole(@Param("role") User.Role role);


    @Query("SELECT u FROM User u LEFT JOIN FETCH u.tickets WHERE u.role = :role")
    List<User> findUsersByRoleWithTickets(@Param("role") User.Role role);
}