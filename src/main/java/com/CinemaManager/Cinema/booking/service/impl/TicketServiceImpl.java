package com.CinemaManager.Cinema.booking.service.impl;

import com.CinemaManager.Cinema.booking.dto.*;
import com.CinemaManager.Cinema.booking.entity.*;
import com.CinemaManager.Cinema.booking.exception.BusinessException;
import com.CinemaManager.Cinema.booking.exception.ResourceNotFoundException;
import com.CinemaManager.Cinema.booking.mapper.CinemaMapper;
import com.CinemaManager.Cinema.booking.repository.*;
import com.CinemaManager.Cinema.booking.service.TicketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TicketServiceImpl implements TicketService {

    private final TicketRepository ticketRepository;
    private final SessionRepository sessionRepository;
    private final SeatRepository seatRepository;
    private final UserRepository userRepository;
    private final CinemaMapper cinemaMapper;

    @Override
    @Transactional
    public TicketPurchaseResponseDTO purchaseTickets(PurchaseTicketDTO purchaseDTO) {
        log.info("Processing ticket purchase for session ID: {}, seats: {}",
                purchaseDTO.getSessionId(), purchaseDTO.getSeatIds());

        // Получаем сеанс
        Session session = sessionRepository.findSessionWithMovieAndHall(purchaseDTO.getSessionId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Session not found with id: " + purchaseDTO.getSessionId()));

        // Проверяем, что сеанс еще не начался
        if (session.getStartTime().isBefore(LocalDateTime.now())) {
            throw new BusinessException("Session has already started");
        }

        // Получаем пользователя
        User user = userRepository.findById(purchaseDTO.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with id: " + purchaseDTO.getUserId()));

        // Получаем места
        List<Seat> seats = seatRepository.findByIdIn(purchaseDTO.getSeatIds());
        if (seats.size() != purchaseDTO.getSeatIds().size()) {
            throw new BusinessException("Some seats not found");
        }

        // Проверяем, что все места находятся в том же зале, что и сеанс
        for (Seat seat : seats) {
            if (!seat.getHall().getId().equals(session.getHall().getId())) {
                throw new BusinessException("Seat " + seat.getId() + " is not in the correct hall");
            }
        }

        // Проверяем доступность мест
        boolean areSeatsAvailable = ticketRepository.areSeatsAvailable(
                purchaseDTO.getSessionId(), purchaseDTO.getSeatIds());
        if (!areSeatsAvailable) {
            throw new BusinessException("Some seats are already occupied");
        }

        // Создаем билеты
        List<Ticket> tickets = new ArrayList<>();
        List<TicketPurchaseResponseDTO.TicketInfoDTO> purchasedTicketsInfo = new ArrayList<>();
        double totalAmount = 0.0;

        for (Seat seat : seats) {
            Ticket ticket = Ticket.builder()
                    .session(session)
                    .seat(seat)
                    .user(user)
                    .status(Ticket.TicketStatus.SOLD)
                    .purchaseTime(LocalDateTime.now())
                    .build();

            tickets.add(ticket);
            totalAmount += session.getPrice();

            // Создаем информацию о купленном билете
            TicketPurchaseResponseDTO.TicketInfoDTO ticketInfo =
                    TicketPurchaseResponseDTO.TicketInfoDTO.builder()
                            .movieTitle(session.getMovie().getTitle())
                            .sessionTime(session.getStartTime())
                            .hallName(session.getHall().getName())
                            .rowNumber(seat.getRowNumber())
                            .seatNumber(seat.getSeatNumber())
                            .price(session.getPrice())
                            .build();
            purchasedTicketsInfo.add(ticketInfo);
        }

        // Сохраняем билеты
        List<Ticket> savedTickets = ticketRepository.saveAll(tickets);

        // Генерируем номера билетов
        for (int i = 0; i < savedTickets.size(); i++) {
            Ticket ticket = savedTickets.get(i);
            purchasedTicketsInfo.get(i).setTicketNumber(ticket.getTicketNumber());
        }

        log.info("Successfully purchased {} tickets for session ID: {}",
                savedTickets.size(), purchaseDTO.getSessionId());

        // Возвращаем ответ
        return TicketPurchaseResponseDTO.builder()
                .success(true)
                .message("Tickets purchased successfully")
                .purchaseTime(LocalDateTime.now())
                .totalAmount(totalAmount)
                .purchasedTickets(purchasedTicketsInfo)
                .build();
    }

    @Override
    @Transactional
    public List<TicketDTO> reserveTickets(PurchaseTicketDTO purchaseDTO) {
        log.info("Reserving tickets for session ID: {}, seats: {}",
                purchaseDTO.getSessionId(), purchaseDTO.getSeatIds());

        // Аналогичная логика, но с статусом BOOKED и без purchaseTime
        Session session = sessionRepository.findById(purchaseDTO.getSessionId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Session not found with id: " + purchaseDTO.getSessionId()));

        if (session.getStartTime().isBefore(LocalDateTime.now())) {
            throw new BusinessException("Session has already started");
        }

        User user = userRepository.findById(purchaseDTO.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with id: " + purchaseDTO.getUserId()));

        List<Seat> seats = seatRepository.findByIdIn(purchaseDTO.getSeatIds());
        if (seats.size() != purchaseDTO.getSeatIds().size()) {
            throw new BusinessException("Some seats not found");
        }

        boolean areSeatsAvailable = ticketRepository.areSeatsAvailable(
                purchaseDTO.getSessionId(), purchaseDTO.getSeatIds());
        if (!areSeatsAvailable) {
            throw new BusinessException("Some seats are already occupied");
        }

        List<Ticket> tickets = new ArrayList<>();
        for (Seat seat : seats) {
            Ticket ticket = Ticket.builder()
                    .session(session)
                    .seat(seat)
                    .user(user)
                    .status(Ticket.TicketStatus.BOOKED)
                    .build();
            tickets.add(ticket);
        }

        List<Ticket> savedTickets = ticketRepository.saveAll(tickets);
        log.info("Successfully reserved {} tickets for session ID: {}",
                savedTickets.size(), purchaseDTO.getSessionId());

        return savedTickets.stream()
                .map(cinemaMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public TicketDTO confirmTicket(String ticketNumber) {
        log.info("Confirming ticket: {}", ticketNumber);
        Ticket ticket = ticketRepository.findByTicketNumber(ticketNumber)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Ticket not found with number: " + ticketNumber));

        if (ticket.getStatus() != Ticket.TicketStatus.BOOKED) {
            throw new BusinessException("Only booked tickets can be confirmed");
        }

        if (ticket.getSession().getStartTime().isBefore(LocalDateTime.now())) {
            throw new BusinessException("Session has already started");
        }

        ticket.setStatus(Ticket.TicketStatus.SOLD);
        ticket.setPurchaseTime(LocalDateTime.now());

        Ticket confirmedTicket = ticketRepository.save(ticket);
        log.info("Ticket {} confirmed successfully", ticketNumber);

        return cinemaMapper.toDTO(confirmedTicket);
    }

    @Override
    @Transactional
    public TicketDTO cancelTicket(String ticketNumber) {
        log.info("Canceling ticket: {}", ticketNumber);
        Ticket ticket = ticketRepository.findByTicketNumber(ticketNumber)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Ticket not found with number: " + ticketNumber));

        if (ticket.getStatus() == Ticket.TicketStatus.CANCELLED) {
            throw new BusinessException("Ticket is already cancelled");
        }

        if (ticket.getSession().getStartTime().isBefore(LocalDateTime.now())) {
            throw new BusinessException("Cannot cancel ticket after session has started");
        }

        ticket.setStatus(Ticket.TicketStatus.CANCELLED);

        Ticket cancelledTicket = ticketRepository.save(ticket);
        log.info("Ticket {} cancelled successfully", ticketNumber);

        return cinemaMapper.toDTO(cancelledTicket);
    }

    @Override
    @Transactional(readOnly = true)
    public TicketDTO getTicketById(Long id) {
        log.debug("Fetching ticket with ID: {}", id);
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Ticket not found with id: " + id));
        return cinemaMapper.toDTO(ticket);
    }

    @Override
    @Transactional(readOnly = true)
    public TicketDTO getTicketByNumber(String ticketNumber) {
        log.debug("Fetching ticket with number: {}", ticketNumber);
        Ticket ticket = ticketRepository.findByTicketNumber(ticketNumber)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Ticket not found with number: " + ticketNumber));
        return cinemaMapper.toDTO(ticket);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TicketDTO> getAllTickets() {
        log.debug("Fetching all tickets");
        List<Ticket> tickets = ticketRepository.findAll();
        return tickets.stream()
                .map(cinemaMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TicketDTO> getTicketsBySession(Long sessionId) {
        log.debug("Fetching tickets for session ID: {}", sessionId);
        List<Ticket> tickets = ticketRepository.findBySessionId(sessionId);
        return tickets.stream()
                .map(cinemaMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TicketDTO> getTicketsByUser(Long userId) {
        log.debug("Fetching tickets for user ID: {}", userId);
        List<Ticket> tickets = ticketRepository.findUserTicketsWithDetails(userId);
        return tickets.stream()
                .map(cinemaMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void cancelExpiredReservations() {
        log.info("Canceling expired reservations");
        LocalDateTime expirationTime = LocalDateTime.now().minusMinutes(15); // 15 минут для брони
        int cancelledCount = ticketRepository.cancelExpiredReservations(expirationTime);
        log.info("Cancelled {} expired reservations", cancelledCount);
    }
}