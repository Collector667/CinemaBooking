package com.CinemaManager.Cinema.booking.controller;

import com.CinemaManager.Cinema.booking.dto.*;
import com.CinemaManager.Cinema.booking.service.TicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<TicketDTO>>> getAllTickets() {
        List<TicketDTO> tickets = ticketService.getAllTickets();
        return ResponseEntity.ok(ApiResponse.success(tickets));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TicketDTO>> getTicketById(@PathVariable Long id) {
        TicketDTO ticket = ticketService.getTicketById(id);
        return ResponseEntity.ok(ApiResponse.success(ticket));
    }

    @PostMapping("/purchase")
    public ResponseEntity<ApiResponse<TicketPurchaseResponseDTO>> purchaseTickets(
            @Valid @RequestBody PurchaseTicketDTO purchaseDTO) {
        TicketPurchaseResponseDTO response = ticketService.purchaseTickets(purchaseDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Билеты успешно куплены", response));
    }

    @PostMapping("/reserve")
    public ResponseEntity<ApiResponse<List<TicketDTO>>> reserveTickets(
            @Valid @RequestBody PurchaseTicketDTO purchaseDTO) {
        List<TicketDTO> reservedTickets = ticketService.reserveTickets(purchaseDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Билеты успешно забронированы", reservedTickets));
    }

    @PostMapping("/{ticketNumber}/confirm")
    public ResponseEntity<ApiResponse<TicketDTO>> confirmTicket(
            @PathVariable String ticketNumber) {
        TicketDTO ticket = ticketService.confirmTicket(ticketNumber);
        return ResponseEntity.ok(ApiResponse.success("Билет подтвержден", ticket));
    }

    @PostMapping("/{ticketNumber}/cancel")
    public ResponseEntity<ApiResponse<TicketDTO>> cancelTicket(
            @PathVariable String ticketNumber) {
        TicketDTO ticket = ticketService.cancelTicket(ticketNumber);
        return ResponseEntity.ok(ApiResponse.success("Билет отменен", ticket));
    }

    @GetMapping("/by-session/{sessionId}")
    public ResponseEntity<ApiResponse<List<TicketDTO>>> getTicketsBySession(
            @PathVariable Long sessionId) {
        List<TicketDTO> tickets = ticketService.getTicketsBySession(sessionId);
        return ResponseEntity.ok(ApiResponse.success(tickets));
    }

    @GetMapping("/by-user/{userId}")
    public ResponseEntity<ApiResponse<List<TicketDTO>>> getTicketsByUser(
            @PathVariable Long userId) {
        List<TicketDTO> tickets = ticketService.getTicketsByUser(userId);
        return ResponseEntity.ok(ApiResponse.success(tickets));
    }

    @GetMapping("/by-number/{ticketNumber}")
    public ResponseEntity<ApiResponse<TicketDTO>> getTicketByNumber(
            @PathVariable String ticketNumber) {
        TicketDTO ticket = ticketService.getTicketByNumber(ticketNumber);
        return ResponseEntity.ok(ApiResponse.success(ticket));
    }
}