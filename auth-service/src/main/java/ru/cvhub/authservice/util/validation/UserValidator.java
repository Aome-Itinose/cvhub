package ru.cvhub.authservice.util.validation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.cvhub.authservice.services.dto.UserDto;
import ru.cvhub.authservice.store.repository.UserRepository;
import ru.cvhub.authservice.util.exception.EmailAlreadyExistsException;
import ru.cvhub.authservice.util.exception.InvalidInputException;
import ru.cvhub.authservice.util.exception.PreconditionException;

@Component
@RequiredArgsConstructor
public class UserValidator implements Validator<UserValidator, UserDto> {
    private final static String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@(.+)$";
    private final static String PASSWORD_REGEX = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&#])[A-Za-z\\d@$!%*?&#]{8,}$";

    private final UserRepository userRepository;

    public void throwIfInvalidContent(UserDto obj) throws PreconditionException {
        if (obj.email() == null || obj.email().isBlank()) {
            throw InvalidInputException.blankEmail();
        }
        if (!obj.email().matches(EMAIL_REGEX)) {
            throw InvalidInputException.invalidEmailFormat();
        }
        if (obj.password() == null || obj.password().isBlank()) {
            throw InvalidInputException.blankPassword();
        }
        if (!obj.password().matches(PASSWORD_REGEX)) {
            throw InvalidInputException.invalidPasswordFormat();
        }
    }

    public void throwIfExists(UserDto obj) throws EmailAlreadyExistsException {
        if (userRepository.existsByEmail(obj.email())) {
            throw new EmailAlreadyExistsException("Email already exists");
        }
    }
}
