package com.CinemaManager.Cinema.booking.controller;

import com.CinemaManager.Cinema.booking.dto.HallDTO;
import com.CinemaManager.Cinema.booking.service.HallService;
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

@WebMvcTest(HallController.class)
class HallControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private HallService hallService;

    @Test
    void getAllHalls_ShouldReturnHalls() throws Exception {
        // Arrange
        HallDTO hall1 = HallDTO.builder()
                .id(1L)
                .hallNumber("A1")
                .name("Main Hall")
                .totalRows(10)
                .seatsPerRow(15)
                .build();

        HallDTO hall2 = HallDTO.builder()
                .id(2L)
                .hallNumber("B1")
                .name("VIP Hall")
                .totalRows(8)
                .seatsPerRow(10)
                .build();

        List<HallDTO> halls = Arrays.asList(hall1, hall2);

        when(hallService.getAllHalls()).thenReturn(halls);

        // Act & Assert
        mockMvc.perform(get("/api/halls"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].hallNumber").value("A1"))
                .andExpect(jsonPath("$.data[1].hallNumber").value("B1"));
    }

    @Test
    void getHallById_WhenHallExists_ShouldReturnHall() throws Exception {
        // Arrange
        HallDTO hall = HallDTO.builder()
                .id(1L)
                .hallNumber("A1")
                .name("Main Hall")
                .totalRows(10)
                .seatsPerRow(15)
                .build();

        when(hallService.getHallById(1L)).thenReturn(hall);

        // Act & Assert
        mockMvc.perform(get("/api/halls/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.hallNumber").value("A1"))
                .andExpect(jsonPath("$.data.name").value("Main Hall"));
    }

    @Test
    void createHall_ValidInput_ShouldReturnCreatedHall() throws Exception {
        // Arrange
        HallDTO requestDTO = HallDTO.builder()
                .hallNumber("C1")
                .name("New Hall")
                .totalRows(12)
                .seatsPerRow(20)
                .build();

        HallDTO responseDTO = HallDTO.builder()
                .id(3L)
                .hallNumber("C1")
                .name("New Hall")
                .totalRows(12)
                .seatsPerRow(20)
                .build();

        when(hallService.createHall(any(HallDTO.class))).thenReturn(responseDTO);

        // Act & Assert
        mockMvc.perform(post("/api/halls")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Зал успешно создан"))
                .andExpect(jsonPath("$.data.id").value(3))
                .andExpect(jsonPath("$.data.hallNumber").value("C1"));
    }
}