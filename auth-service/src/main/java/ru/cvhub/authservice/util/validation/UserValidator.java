package ru.cvhub.authservice.util.validation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.cvhub.authservice.services.UserService;
import ru.cvhub.authservice.store.repository.UserRepository;
import ru.cvhub.authservice.util.exception.EmailAlreadyExistsException;
import ru.cvhub.authservice.util.exception.InvalidInputException;
import ru.cvhub.authservice.util.exception.PreconditionException;
import ru.cvhub.authservice.util.exception.WeakPasswordException;

@Component
@RequiredArgsConstructor
public class UserValidator implements Validator<UserValidator, UserService.UserDto> {
    private final UserRepository userRepository;

    @Override
    public UserValidator throwIfInvalidContent(UserService.UserDto obj) throws PreconditionException {
        if (obj.email() == null || obj.email().isBlank()) {
            throw new InvalidInputException("Email must be provided");
        }
        if (!obj.email().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new InvalidInputException("Invalid email format");
        }
        if (obj.password() == null || obj.password().isBlank()) {
            throw new InvalidInputException("Password must be at least 8 characters");
        }
        if (obj.password().length() < 8 ||
                !obj.password().matches("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$")) {
            throw new WeakPasswordException("Password must contain letters, numbers, and special characters");
        }
        return this;
    }

    @Override
    public UserValidator throwIfExists(UserService.UserDto obj) throws EmailAlreadyExistsException {
        if (userRepository.existsByEmail(obj.email())) {
            throw new EmailAlreadyExistsException("Email already exists");
        }
        return this;
    }
}
