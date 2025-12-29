package com.CinemaManager.Cinema.booking.service.impl;

import com.CinemaManager.Cinema.booking.dto.TicketDTO;
import com.CinemaManager.Cinema.booking.entity.Ticket;
import com.CinemaManager.Cinema.booking.exception.ResourceNotFoundException;
import com.CinemaManager.Cinema.booking.mapper.CinemaMapper;
import com.CinemaManager.Cinema.booking.repository.TicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private CinemaMapper cinemaMapper;

    @InjectMocks
    private TicketServiceImpl ticketService;

    private Ticket ticket;
    private TicketDTO ticketDTO;

    @BeforeEach
    void setUp() {
        ticket = Ticket.builder()
                .ticketNumber("TKT-12345")
                .status(Ticket.TicketStatus.SOLD)
                .purchaseTime(LocalDateTime.now())
                .build();

        ticketDTO = TicketDTO.builder()
                .id(1L)
                .ticketNumber("TKT-12345")
                .status(Ticket.TicketStatus.SOLD)
                .purchaseTime(LocalDateTime.now())
                .build();
    }

    @Test
    void getTicketById_WhenTicketExists_ShouldReturnTicketDTO() {
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(cinemaMapper.toDTO(ticket)).thenReturn(ticketDTO);

        TicketDTO result = ticketService.getTicketById(1L);

        assertNotNull(result);
        assertEquals("TKT-12345", result.getTicketNumber());
        verify(ticketRepository).findById(1L);
    }

    @Test
    void getAllTickets_ShouldReturnAllTickets() {
        Ticket ticket2 = Ticket.builder().ticketNumber("TKT-67890").build();
        TicketDTO ticketDTO2 = TicketDTO.builder().id(2L).ticketNumber("TKT-67890").build();

        when(ticketRepository.findAll()).thenReturn(Arrays.asList(ticket, ticket2));
        when(cinemaMapper.toDTO(ticket)).thenReturn(ticketDTO);
        when(cinemaMapper.toDTO(ticket2)).thenReturn(ticketDTO2);

        List<TicketDTO> result = ticketService.getAllTickets();

        assertEquals(2, result.size());
        verify(ticketRepository).findAll();
    }
}