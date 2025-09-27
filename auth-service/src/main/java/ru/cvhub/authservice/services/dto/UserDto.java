package ru.cvhub.authservice.services.dto;

public record UserDto(
        String email,
        String password
) {}