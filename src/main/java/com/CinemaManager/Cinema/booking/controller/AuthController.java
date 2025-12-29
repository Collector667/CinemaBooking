package com.CinemaManager.Cinema.booking.controller;

import com.CinemaManager.Cinema.booking.dto.*;
import com.CinemaManager.Cinema.booking.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserDTO>> register(
            @Valid @RequestBody RegisterDTO registerDTO) {
        UserDTO user = authService.register(registerDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Регистрация прошла успешно", user));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponseDTO>> login(
            @Valid @RequestBody LoginDTO loginDTO) {
        AuthResponseDTO authResponse = authService.login(loginDTO);
        return ResponseEntity.ok(ApiResponse.success("Вход выполнен успешно", authResponse));
    }

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserDTO>> getCurrentUserProfile() {
        // В реальном проекте здесь бы брали ID из токена/сессии
        // Пока что возвращаем заглушку
        return ResponseEntity.ok(ApiResponse.success(
                "Требуется реализация получения профиля", null));
    }

    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<UserDTO>> updateProfile(
            @Valid @RequestBody UserDTO userDTO) {
        // В реальном проекте здесь бы брали ID из токена/сессии
        return ResponseEntity.ok(ApiResponse.success(
                "Требуется реализация обновления профиля", null));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout() {
        // В реальном проекте здесь бы инвалидировали токен
        return ResponseEntity.ok(ApiResponse.success("Выход выполнен успешно", null));
    }

}