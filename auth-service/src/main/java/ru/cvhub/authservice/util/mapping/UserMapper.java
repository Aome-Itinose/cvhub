package ru.cvhub.authservice.util.mapping;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import ru.cvhub.authservice.grpc.AS;
import ru.cvhub.authservice.services.UserService;
import ru.cvhub.authservice.store.entity.User;

import java.util.Locale;

@Component
public class UserMapper {
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

}
