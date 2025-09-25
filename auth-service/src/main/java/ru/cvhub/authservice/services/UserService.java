package ru.cvhub.authservice.services;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.cvhub.authservice.security.JwtUtil;
import ru.cvhub.authservice.store.entity.Role;
import ru.cvhub.authservice.store.entity.Session;
import ru.cvhub.authservice.store.entity.User;
import ru.cvhub.authservice.store.repository.UserRepository;
import ru.cvhub.authservice.util.exception.UserCreationFailedException;
import ru.cvhub.authservice.util.mapping.Mapper;
import ru.cvhub.authservice.util.validation.UserValidator;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserValidator userValidator;
    private final JwtUtil jwtUtil;
    private final SessionService sessionService;

    @Transactional
    public TokenDto createUser(UserDto userDto) {
        try {
            userValidator.throwIfInvalidContent(userDto)
                    .throwIfExists(userDto);
            User createdUser = userRepository.save(Mapper.toEntity(userDto, passwordEncoder));

            Session userSession = sessionService.createSession(createdUser.getId());
            String accessToken = jwtUtil.generateToken(
                    createdUser.getId().toString(),
                    createdUser.getEmail(),
                    false,
                    createdUser.getRoles().stream().map(Role::getName).toList()
            );

            return new TokenDto(accessToken, userSession.getRefreshToken().toString());
        } catch (DataAccessException e) {
            throw new UserCreationFailedException("Database error during user creation: " + e.getMessage());
        }
    }

    public record UserDto(
            String email,
            String password
    ) {}

    public record TokenDto(
            String accessToken,
            String refreshToken
    ) {}
}
