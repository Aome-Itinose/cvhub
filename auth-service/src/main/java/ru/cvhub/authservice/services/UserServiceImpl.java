package ru.cvhub.authservice.services;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.dao.DataAccessException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.cvhub.authservice.services.dto.UserDto;
import ru.cvhub.authservice.store.entity.User;
import ru.cvhub.authservice.store.repository.UserRepository;
import ru.cvhub.authservice.util.exception.InactiveUserException;
import ru.cvhub.authservice.util.exception.IncorrectPasswordException;
import ru.cvhub.authservice.util.exception.UserCreationFailedException;
import ru.cvhub.authservice.util.exception.UserNotFoundException;
import ru.cvhub.authservice.util.mapping.Mapper;
import ru.cvhub.authservice.util.validation.UserValidator;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserValidator userValidator;

    public @NotNull User registerUser(@NotNull UserDto userDto) {
        try {
            userValidator.throwIfExists(userDto);
            return userRepository.save(Mapper.toEntity(userDto, passwordEncoder));
        } catch (DataAccessException e) {
            throw new UserCreationFailedException("Database error during user creation: " + e.getMessage());
        }
    }

    public @NotNull User loginUser(@NotNull UserDto userDto) {
        Optional<User> maybeUser = userRepository.findByEmail(userDto.email());
        if (maybeUser.isEmpty()) {
            throw new UserNotFoundException();
        }

        User user = maybeUser.get();
        if (!passwordEncoder.matches(userDto.password(), user.passwordHash())) {
            throw new IncorrectPasswordException();
        }

        if (!user.isActive()) {
            throw InactiveUserException.userInactive();
        }
        return user;
    }
}
