package com.CinemaManager.Cinema.booking.controller;

import com.CinemaManager.Cinema.booking.dto.ApiResponse;
import com.CinemaManager.Cinema.booking.dto.TicketDTO;
import com.CinemaManager.Cinema.booking.dto.UserDTO;
import com.CinemaManager.Cinema.booking.service.TicketService;
import com.CinemaManager.Cinema.booking.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final TicketService ticketService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<UserDTO>>> getAllUsers() {
        List<UserDTO> users = userService.getAllUsers();
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDTO>> getUserById(@PathVariable Long id) {
        UserDTO user = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @GetMapping("/{id}/tickets")
    public ResponseEntity<ApiResponse<List<TicketDTO>>> getUserTickets(@PathVariable Long id) {
        List<TicketDTO> tickets = ticketService.getTicketsByUser(id);
        return ResponseEntity.ok(ApiResponse.success(tickets));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDTO>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserDTO userDTO) {
        UserDTO updatedUser = userService.updateUser(id, userDTO);
        return ResponseEntity.ok(ApiResponse.success("Пользователь успешно обновлен", updatedUser));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.success("Пользователь успешно удален", null));
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<ApiResponse<UserDTO>> getUserByEmail(@PathVariable String email) {
        UserDTO user = userService.getUserByEmail(email);
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @GetMapping("/{id}/stats")
    public ResponseEntity<ApiResponse<UserStatsDTO>> getUserStats(@PathVariable Long id) {
        UserStatsDTO stats = userService.getUserStats(id);
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    // DTO для статистики пользователя
    public record UserStatsDTO(
            Long userId,
            String fullName,
            Integer totalTickets,
            Double totalSpent,
            String favoriteGenre,
            Integer ticketsThisMonth
    ) {}
}