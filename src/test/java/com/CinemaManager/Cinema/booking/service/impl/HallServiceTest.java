package com.CinemaManager.Cinema.booking.service.impl;

import com.CinemaManager.Cinema.booking.dto.HallDTO;
import com.CinemaManager.Cinema.booking.entity.Hall;
import com.CinemaManager.Cinema.booking.entity.Seat;
import com.CinemaManager.Cinema.booking.exception.ResourceNotFoundException;
import com.CinemaManager.Cinema.booking.mapper.CinemaMapper;
import com.CinemaManager.Cinema.booking.repository.HallRepository;
import com.CinemaManager.Cinema.booking.repository.SeatRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HallServiceTest {

    @Mock
    private HallRepository hallRepository;

    @Mock
    private SeatRepository seatRepository;

    @Mock
    private CinemaMapper cinemaMapper;

    @InjectMocks
    private HallServiceImpl hallService;

    private Hall testHall;
    private HallDTO testHallDTO;

    @BeforeEach
    void setUp() {
        testHall = Hall.builder()
                .hallNumber("A1")
                .name("Main Hall")
                .totalRows(10)
                .seatsPerRow(15)
                .description("Main cinema hall")
                .build();

        testHallDTO = HallDTO.builder()
                .id(1L)
                .hallNumber("A1")
                .name("Main Hall")
                .totalRows(10)
                .seatsPerRow(15)
                .description("Main cinema hall")
                .build();
    }

    @Test
    void getHallById_WhenHallExists_ShouldReturnHallDTO() {
        when(hallRepository.findHallWithSeats(1L)).thenReturn(Optional.of(testHall));
        when(cinemaMapper.toDTO(testHall)).thenReturn(testHallDTO);

        HallDTO result = hallService.getHallById(1L);

        assertNotNull(result);
        assertEquals("A1", result.getHallNumber());
        verify(hallRepository).findHallWithSeats(1L);
    }

    @Test
    void createHall_ShouldSaveAndReturnHallDTO() {
        when(cinemaMapper.toEntity(testHallDTO)).thenReturn(testHall);
        when(hallRepository.save(testHall)).thenReturn(testHall);
        when(cinemaMapper.toDTO(testHall)).thenReturn(testHallDTO);

        HallDTO result = hallService.createHall(testHallDTO);

        assertNotNull(result);
        verify(hallRepository).save(testHall);
    }
}