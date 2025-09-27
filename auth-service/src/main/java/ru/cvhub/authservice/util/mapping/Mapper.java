package ru.cvhub.authservice.util.mapping;

import org.springframework.security.crypto.password.PasswordEncoder;
import ru.cvhub.authservice.grpc.AS;
import ru.cvhub.authservice.services.dto.TokenDto;
import ru.cvhub.authservice.services.dto.UserDto;
import ru.cvhub.authservice.store.entity.User;

import java.util.Locale;

public class Mapper {
    public static UserDto toDto(AS.RegisterRequest request) {
        return new UserDto(
                request.getEmail().toLowerCase(Locale.ROOT),
                request.getPassword()
        );
    }

    public static TokenDto toDto(AS.RefreshTokenRequest request) {
        return new TokenDto(
                null,
                request.getRefreshToken()
        );
    }

    public static UserDto toDto(AS.LoginRequest request) {
        return new UserDto(
                request.getEmail().toLowerCase(Locale.ROOT),
                request.getPassword()
        );
    }

    public static User toEntity(UserDto userDto, PasswordEncoder encoder) {
        return new User(
                userDto.email(),
                encoder.encode(userDto.password())
        );
    }

    public static AS.TokenResponse toResponse(TokenDto dto) {
        return AS.TokenResponse.newBuilder()
                .setAccessToken(dto.accessToken())
                .setRefreshToken(dto.refreshToken())
                .build();
    }
}
