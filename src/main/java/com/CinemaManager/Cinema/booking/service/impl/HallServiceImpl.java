package com.CinemaManager.Cinema.booking.service.impl;

import com.CinemaManager.Cinema.booking.dto.HallDTO;
import com.CinemaManager.Cinema.booking.entity.Hall;
import com.CinemaManager.Cinema.booking.entity.Seat;
import com.CinemaManager.Cinema.booking.exception.ResourceNotFoundException;
import com.CinemaManager.Cinema.booking.mapper.CinemaMapper;
import com.CinemaManager.Cinema.booking.repository.HallRepository;
import com.CinemaManager.Cinema.booking.repository.SeatRepository;
import com.CinemaManager.Cinema.booking.service.HallService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class HallServiceImpl implements HallService {

    private final HallRepository hallRepository;
    private final SeatRepository seatRepository;
    private final CinemaMapper cinemaMapper;

    @Override
    @Transactional
    public HallDTO createHall(HallDTO hallDTO) {
        log.info("Creating new hall: {}", hallDTO.getHallNumber());
        Hall hall = cinemaMapper.toEntity(hallDTO);
        Hall savedHall = hallRepository.save(hall);
        log.info("Hall created with ID: {}", savedHall.getId());
        return cinemaMapper.toDTO(savedHall);
    }

    @Override
    @Transactional(readOnly = true)
    public HallDTO getHallById(Long id) {
        log.debug("Fetching hall with ID: {}", id);
        Hall hall = hallRepository.findHallWithSeats(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hall not found with id: " + id));
        return cinemaMapper.toDTO(hall);
    }

    @Override
    @Transactional(readOnly = true)
    public List<HallDTO> getAllHalls() {
        log.debug("Fetching all halls");
        List<Hall> halls = hallRepository.findAll();
        return halls.stream()
                .map(cinemaMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public HallDTO updateHall(Long id, HallDTO hallDTO) {
        log.info("Updating hall with ID: {}", id);
        Hall existingHall = hallRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hall not found with id: " + id));

        existingHall.setHallNumber(hallDTO.getHallNumber());
        existingHall.setName(hallDTO.getName());
        existingHall.setTotalRows(hallDTO.getTotalRows());
        existingHall.setSeatsPerRow(hallDTO.getSeatsPerRow());
        existingHall.setDescription(hallDTO.getDescription());

        Hall updatedHall = hallRepository.save(existingHall);
        log.info("Hall updated with ID: {}", updatedHall.getId());
        return cinemaMapper.toDTO(updatedHall);
    }

    @Override
    @Transactional
    public void deleteHall(Long id) {
        log.info("Deleting hall with ID: {}", id);
        Hall hall = hallRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hall not found with id: " + id));
        hallRepository.delete(hall);
        log.info("Hall deleted with ID: {}", id);
    }

    @Override
    @Transactional
    public void initializeSeats(Long hallId) {
        log.info("Initializing seats for hall with ID: {}", hallId);
        Hall hall = hallRepository.findById(hallId)
                .orElseThrow(() -> new ResourceNotFoundException("Hall not found with id: " + hallId));

        List<Seat> existingSeats = seatRepository.findByHall(hall);
        seatRepository.deleteAll(existingSeats);

        List<Seat> seats = new ArrayList<>();
        for (int row = 1; row <= hall.getTotalRows(); row++) {
            for (int seatNum = 1; seatNum <= hall.getSeatsPerRow(); seatNum++) {
                Seat seat = Seat.builder()
                        .rowNumber(row)
                        .seatNumber(seatNum)
                        .hall(hall)
                        .build();
                seats.add(seat);
            }
        }
        seatRepository.saveAll(seats);
        log.info("Created {} seats for hall ID: {}", seats.size(), hallId);
    }
}