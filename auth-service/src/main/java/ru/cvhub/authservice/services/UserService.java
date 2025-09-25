package ru.cvhub.authservice.services;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.cvhub.authservice.security.JwtUtil;
import ru.cvhub.authservice.store.entity.Role;
import ru.cvhub.authservice.store.entity.User;
import ru.cvhub.authservice.store.repository.UserRepository;
import ru.cvhub.authservice.util.exception.UserCreationFailedException;
import ru.cvhub.authservice.util.mapping.UserMapper;
import ru.cvhub.authservice.util.validation.UserValidator;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserValidator userValidator;
    private final JwtUtil jwtUtil;

    @Transactional
    public Object createUser(UserDto userDto) {
        String token;
        try {
            userValidator.throwIfInvalidContent(userDto)
                    .throwIfExists(userDto);
            User createdUser = userRepository.save(UserMapper.toEntity(userDto, passwordEncoder));
            token = jwtUtil.generateToken(
                    createdUser.getId().toString(),
                    createdUser.getEmail(),
                    false,
                    createdUser.getRoles().stream().map(Role::getName).toList()
            );
        } catch (DataAccessException e) {
            throw new UserCreationFailedException("Database error during user creation: " + e.getMessage());
        }
        return token;
    }

    public record UserDto(
            String email,
            String password
    ) {}
}
