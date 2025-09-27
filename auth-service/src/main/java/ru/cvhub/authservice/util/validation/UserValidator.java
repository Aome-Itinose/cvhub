package ru.cvhub.authservice.util.validation;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import ru.cvhub.authservice.services.dto.UserDto;
import ru.cvhub.authservice.store.repository.UserRepository;
import ru.cvhub.authservice.util.exception.EmailAlreadyExistsException;
import ru.cvhub.authservice.util.exception.InvalidInputException;
import ru.cvhub.authservice.util.exception.PreconditionException;
import ru.cvhub.authservice.util.exception.WeakPasswordException;

@Component
@RequiredArgsConstructor
public class UserValidator implements Validator<UserValidator, UserDto> {
    private final static String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@(.+)$";
    private final static String PASSWORD_REGEX = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$";

    private final UserRepository userRepository;

    @Override
    public void validate(@NotNull UserDto obj) throws PreconditionException {
        throwIfInvalidContent(obj);
        throwIfExists(obj);
    }

    private void throwIfInvalidContent(UserDto obj) throws PreconditionException {
        if (obj.email() == null || obj.email().isBlank()) {
            throw new InvalidInputException("Email must be provided");
        }
        if (!obj.email().matches(EMAIL_REGEX)) {
            throw new InvalidInputException("Invalid email format");
        }
        if (obj.password() == null || obj.password().isBlank()) {
            throw new InvalidInputException("Password must be at least 8 characters");
        }/*
        if (obj.password().length() < 8 ||
                !obj.password().matches(PASSWORD_REGEX)) {
            throw new WeakPasswordException("Password must contain letters, numbers, and special characters");
        }*/
    }

    private void throwIfExists(UserDto obj) throws EmailAlreadyExistsException {
        if (userRepository.existsByEmail(obj.email())) {
            throw new EmailAlreadyExistsException("Email already exists");
        }
    }
}
