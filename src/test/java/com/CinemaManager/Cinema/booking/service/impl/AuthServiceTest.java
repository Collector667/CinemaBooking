package com.CinemaManager.Cinema.booking.service.impl;

import com.CinemaManager.Cinema.booking.dto.*;
import com.CinemaManager.Cinema.booking.entity.User;
import com.CinemaManager.Cinema.booking.exception.BusinessException;
import com.CinemaManager.Cinema.booking.mapper.CinemaMapper;
import com.CinemaManager.Cinema.booking.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CinemaMapper cinemaMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthServiceImpl authService;

    private RegisterDTO registerDTO;
    private LoginDTO loginDTO;
    private User user;
    private UserDTO userDTO;

    @BeforeEach
    void setUp() {
        registerDTO = RegisterDTO.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .password("password123")
                .phone("+1234567890")
                .build();

        loginDTO = LoginDTO.builder()
                .email("john@example.com")
                .password("password123")
                .build();

        user = User.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .password("encodedPassword")
                .phone("+1234567890")
                .role(User.Role.ROLE_USER)
                .build();

        userDTO = UserDTO.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .phone("+1234567890")
                .role(User.Role.ROLE_USER)
                .build();
    }

    @Test
    void register_NewUser_ShouldReturnUserDTO() {
        when(userRepository.existsByEmail(registerDTO.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(registerDTO.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(cinemaMapper.toDTO(any(User.class))).thenReturn(userDTO);

        UserDTO result = authService.register(registerDTO);

        assertNotNull(result);
        assertEquals("john@example.com", result.getEmail());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void login_ValidCredentials_ShouldReturnAuthResponse() {
        when(userRepository.findByEmail(loginDTO.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())).thenReturn(true);
        when(cinemaMapper.toDTO(user)).thenReturn(userDTO);

        AuthResponseDTO result = authService.login(loginDTO);

        assertNotNull(result);
        assertNotNull(result.getToken());
        verify(userRepository).findByEmail(loginDTO.getEmail());
    }
}