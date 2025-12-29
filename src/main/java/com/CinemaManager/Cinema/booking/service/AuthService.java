package com.CinemaManager.Cinema.booking.service;

import com.CinemaManager.Cinema.booking.dto.*;

public interface AuthService {
    UserDTO register(RegisterDTO registerDTO);
    AuthResponseDTO login(LoginDTO loginDTO);
}