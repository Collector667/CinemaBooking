package com.CinemaManager.Cinema.booking.service;

import com.CinemaManager.Cinema.booking.controller.UserController;
import com.CinemaManager.Cinema.booking.dto.UserDTO;
import java.util.List;

public interface UserService {
    UserDTO createUser(UserDTO userDTO);
    UserDTO getUserById(Long id);
    UserDTO getUserByEmail(String email);
    List<UserDTO> getAllUsers();
    UserDTO updateUser(Long id, UserDTO userDTO);
    void deleteUser(Long id);
    UserController.UserStatsDTO getUserStats(Long userId);
}