package com.CinemaManager.Cinema.booking.service.impl;

import com.CinemaManager.Cinema.booking.controller.UserController;
import com.CinemaManager.Cinema.booking.dto.UserDTO;
import com.CinemaManager.Cinema.booking.entity.Ticket;
import com.CinemaManager.Cinema.booking.entity.User;
import com.CinemaManager.Cinema.booking.exception.BusinessException;
import com.CinemaManager.Cinema.booking.exception.ResourceNotFoundException;
import com.CinemaManager.Cinema.booking.mapper.CinemaMapper;
import com.CinemaManager.Cinema.booking.repository.UserRepository;
import com.CinemaManager.Cinema.booking.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final CinemaMapper cinemaMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UserDTO createUser(UserDTO userDTO) {
        log.info("Creating new user: {}", userDTO.getEmail());

        // Проверяем, существует ли пользователь с таким email
        if (userRepository.existsByEmail(userDTO.getEmail())) {
            throw new BusinessException("User with email " + userDTO.getEmail() + " already exists");
        }

        // Хэшируем пароль
        userDTO.setPassword(passwordEncoder.encode(userDTO.getPassword()));

        // Устанавливаем роль по умолчанию, если не задана
        if (userDTO.getRole() == null) {
            userDTO.setRole(User.Role.ROLE_USER);
        }

        User user = cinemaMapper.toEntity(userDTO);
        User savedUser = userRepository.save(user);
        log.info("User created with ID: {}", savedUser.getId());

        return cinemaMapper.toDTO(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDTO getUserById(Long id) {
        log.debug("Fetching user with ID: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with id: " + id));
        return cinemaMapper.toDTO(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDTO getUserByEmail(String email) {
        log.debug("Fetching user with email: {}", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with email: " + email));
        return cinemaMapper.toDTO(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDTO> getAllUsers() {
        log.debug("Fetching all users");
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(cinemaMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public UserDTO updateUser(Long id, UserDTO userDTO) {
        log.info("Updating user with ID: {}", id);
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with id: " + id));

        // Проверяем email на уникальность (если изменился)
        if (!existingUser.getEmail().equals(userDTO.getEmail()) &&
                userRepository.existsByEmail(userDTO.getEmail())) {
            throw new BusinessException("User with email " + userDTO.getEmail() + " already exists");
        }

        // Обновляем поля
        existingUser.setFirstName(userDTO.getFirstName());
        existingUser.setLastName(userDTO.getLastName());
        existingUser.setEmail(userDTO.getEmail());
        existingUser.setPhone(userDTO.getPhone());
        existingUser.setRole(userDTO.getRole());

        // Обновляем пароль только если он предоставлен
        if (userDTO.getPassword() != null && !userDTO.getPassword().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        }

        User updatedUser = userRepository.save(existingUser);
        log.info("User updated with ID: {}", updatedUser.getId());

        return cinemaMapper.toDTO(updatedUser);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        log.info("Deleting user with ID: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with id: " + id));
        userRepository.delete(user);
        log.info("User deleted with ID: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public UserController.UserStatsDTO getUserStats(Long userId) {
        log.debug("Fetching stats for user ID: {}", userId);
        User user = userRepository.findUserWithTickets(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with id: " + userId));

        // Рассчитываем статистику
        int totalTickets = user.getTickets().size();
        double totalSpent = user.getTickets().stream()
                .filter(ticket -> ticket.getStatus() == Ticket.TicketStatus.SOLD)
                .mapToDouble(ticket -> ticket.getSession().getPrice())
                .sum();

        // TODO: Реализовать логику для favoriteGenre и ticketsThisMonth
        // Это требует более сложных запросов к репозиторию

        return new UserController.UserStatsDTO(
                user.getId(),
                user.getFullName(),
                totalTickets,
                totalSpent,
                "Action", // Заглушка
                5 // Заглушка
        );
    }
}