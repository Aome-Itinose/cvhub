package ru.cvhub.authservice.util.mapping;

import org.springframework.security.crypto.password.PasswordEncoder;
import ru.cvhub.authservice.grpc.AS;
import ru.cvhub.authservice.services.UserService;
import ru.cvhub.authservice.store.entity.User;

import java.util.Locale;

public class Mapper {
    public static UserService.UserDto toDto(AS.RegisterRequest request) {
        return new UserService.UserDto(
                request.getEmail().toLowerCase(Locale.ROOT),
                request.getPassword()
        );
    }

    public static User toEntity(UserService.UserDto userDto, PasswordEncoder encoder) {
        return new User(
                userDto.email(),
                encoder.encode(userDto.password())
        );
    }

    public static AS.TokenResponse toResponse(UserService.TokenDto dto) {
        return AS.TokenResponse.newBuilder()
                .setAccessToken(dto.accessToken())
                .setRefreshToken(dto.refreshToken())
                .build();
    }
}
