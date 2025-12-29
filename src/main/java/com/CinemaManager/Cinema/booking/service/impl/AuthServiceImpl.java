package com.CinemaManager.Cinema.booking.service.impl;

import com.CinemaManager.Cinema.booking.dto.*;
import com.CinemaManager.Cinema.booking.entity.User;
import com.CinemaManager.Cinema.booking.exception.BusinessException;
import com.CinemaManager.Cinema.booking.mapper.CinemaMapper;
import com.CinemaManager.Cinema.booking.repository.UserRepository;
import com.CinemaManager.Cinema.booking.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final CinemaMapper cinemaMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UserDTO register(RegisterDTO registerDTO) {
        log.info("Registering new user: {}", registerDTO.getEmail());

        // Проверяем, существует ли пользователь с таким email
        if (userRepository.existsByEmail(registerDTO.getEmail())) {
            throw new BusinessException("User with email " + registerDTO.getEmail() + " already exists");
        }

        // Создаем User из RegisterDTO
        User user = User.builder()
                .firstName(registerDTO.getFirstName())
                .lastName(registerDTO.getLastName())
                .email(registerDTO.getEmail())
                .password(passwordEncoder.encode(registerDTO.getPassword()))
                .phone(registerDTO.getPhone())
                .role(User.Role.ROLE_USER)
                .build();

        User savedUser = userRepository.save(user);
        log.info("User registered with ID: {}", savedUser.getId());

        return cinemaMapper.toDTO(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponseDTO login(LoginDTO loginDTO) {
        log.info("Login attempt for user: {}", loginDTO.getEmail());

        // Находим пользователя по email
        User user = userRepository.findByEmail(loginDTO.getEmail())
                .orElseThrow(() -> new BusinessException("Invalid email or password"));

        // Проверяем пароль
        if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
            throw new BusinessException("Invalid email or password");
        }

        // Генерируем токен (для учебного проекта используем упрощённый подход)
        String token = generateToken(user);

        log.info("User logged in successfully: {}", loginDTO.getEmail());

        // Используем builder из AuthResponseDTO
        return AuthResponseDTO.builder()
                .token(token)
                .user(cinemaMapper.toDTO(user))
                .expiresIn(3600L) // 1 час в секундах
                .tokenType("Bearer")
                .build();
    }

    /**
     * Упрощённая генерация токена для учебного проекта
     * В реальном проекте используйте JWT (jsonwebtoken)
     */
    private String generateToken(User user) {
        // Создаём простой токен: user_id + timestamp + random
        return "demo-token-" + user.getId() +
                "-" + System.currentTimeMillis() +
                "-" + (int)(Math.random() * 1000);
    }
}