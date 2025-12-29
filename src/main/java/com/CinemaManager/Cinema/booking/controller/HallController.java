package com.CinemaManager.Cinema.booking.controller;

import com.CinemaManager.Cinema.booking.dto.ApiResponse;
import com.CinemaManager.Cinema.booking.dto.HallDTO;
import com.CinemaManager.Cinema.booking.service.HallService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/halls")
@RequiredArgsConstructor
public class HallController {

    private final HallService hallService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<HallDTO>>> getAllHalls() {
        List<HallDTO> halls = hallService.getAllHalls();
        return ResponseEntity.ok(ApiResponse.success(halls));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<HallDTO>> getHallById(@PathVariable Long id) {
        HallDTO hall = hallService.getHallById(id);
        return ResponseEntity.ok(ApiResponse.success(hall));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<HallDTO>> createHall(
            @Valid @RequestBody HallDTO hallDTO) {
        HallDTO createdHall = hallService.createHall(hallDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Зал успешно создан", createdHall));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<HallDTO>> updateHall(
            @PathVariable Long id,
            @Valid @RequestBody HallDTO hallDTO) {
        HallDTO updatedHall = hallService.updateHall(id, hallDTO);
        return ResponseEntity.ok(ApiResponse.success("Зал успешно обновлен", updatedHall));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteHall(@PathVariable Long id) {
        hallService.deleteHall(id);
        return ResponseEntity.ok(ApiResponse.success("Зал успешно удален", null));
    }

    @PostMapping("/{id}/seats/initialize")
    public ResponseEntity<ApiResponse<Void>> initializeSeats(@PathVariable Long id) {
        hallService.initializeSeats(id);
        return ResponseEntity.ok(ApiResponse.success("Места успешно инициализированы", null));
    }
}