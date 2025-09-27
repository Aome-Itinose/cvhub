package ru.cvhub.authservice.services.dto;

public record TokenDto(
        String accessToken,
        String refreshToken
) {}