package com.CinemaManager.Cinema.booking.service;

import com.CinemaManager.Cinema.booking.dto.*;
import java.util.List;

public interface TicketService {
    TicketPurchaseResponseDTO purchaseTickets(PurchaseTicketDTO purchaseDTO);
    List<TicketDTO> reserveTickets(PurchaseTicketDTO purchaseDTO);
    TicketDTO confirmTicket(String ticketNumber);
    TicketDTO cancelTicket(String ticketNumber);
    TicketDTO getTicketById(Long id);
    TicketDTO getTicketByNumber(String ticketNumber);
    List<TicketDTO> getAllTickets();
    List<TicketDTO> getTicketsBySession(Long sessionId);
    List<TicketDTO> getTicketsByUser(Long userId);
    void cancelExpiredReservations();
}