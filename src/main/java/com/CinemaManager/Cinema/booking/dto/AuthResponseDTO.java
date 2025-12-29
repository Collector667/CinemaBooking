package com.CinemaManager.Cinema.booking.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponseDTO {
    private String token;
    private UserDTO user;
    private Long expiresIn;

    @Builder.Default
    private String tokenType = "Bearer";
}