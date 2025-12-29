package com.CinemaManager.Cinema.booking.controller;

import com.CinemaManager.Cinema.booking.dto.UserDTO;
import com.CinemaManager.Cinema.booking.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @Test
    void getAllUsers_ShouldReturnUsers() throws Exception {
        // Arrange
        UserDTO user1 = UserDTO.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .phone("+1234567890")
                .build();

        UserDTO user2 = UserDTO.builder()
                .id(2L)
                .firstName("Jane")
                .lastName("Smith")
                .email("jane@example.com")
                .phone("+1234567891")
                .build();

        List<UserDTO> users = Arrays.asList(user1, user2);

        when(userService.getAllUsers()).thenReturn(users);

        // Act & Assert
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].email").value("john@example.com"))
                .andExpect(jsonPath("$.data[1].email").value("jane@example.com"));
    }

    @Test
    void getUserById_WhenUserExists_ShouldReturnUser() throws Exception {
        // Arrange
        UserDTO user = UserDTO.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .phone("+1234567890")
                .build();

        when(userService.getUserById(1L)).thenReturn(user);

        // Act & Assert
        mockMvc.perform(get("/api/users/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.firstName").value("John"))
                .andExpect(jsonPath("$.data.email").value("john@example.com"));
    }

    @Test
    void updateUser_ValidInput_ShouldReturnUpdatedUser() throws Exception {
        // Arrange
        UserDTO updateDTO = UserDTO.builder()
                .firstName("John")
                .lastName("Doe Updated")
                .email("john.updated@example.com")
                .phone("+1234567890")
                .build();

        UserDTO updatedUser = UserDTO.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe Updated")
                .email("john.updated@example.com")
                .phone("+1234567890")
                .build();

        when(userService.updateUser(eq(1L), any(UserDTO.class))).thenReturn(updatedUser);

        // Act & Assert
        mockMvc.perform(put("/api/users/{id}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Пользователь успешно обновлен"))
                .andExpect(jsonPath("$.data.lastName").value("Doe Updated"))
                .andExpect(jsonPath("$.data.email").value("john.updated@example.com"));
    }

    @Test
    void deleteUser_ShouldReturnSuccess() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/users/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Пользователь успешно удален"));
    }
}