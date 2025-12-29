package com.CinemaManager.Cinema.booking.service;

import com.CinemaManager.Cinema.booking.dto.HallDTO;

import java.util.List;

public interface HallService {
    HallDTO createHall(HallDTO hallDTO);
    HallDTO getHallById(Long id);
    List<HallDTO> getAllHalls();
    HallDTO updateHall(Long id, HallDTO hallDTO);
    void deleteHall(Long id);
    void initializeSeats(Long hallId);
}