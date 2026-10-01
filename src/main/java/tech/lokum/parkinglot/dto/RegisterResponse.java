package tech.lokum.parkinglot.dto;

public record RegisterResponse(
        Long id,
        String username,
        String email,
        String role
) {
}
