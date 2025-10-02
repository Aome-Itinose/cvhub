package ru.cvhub.authservice.util.exception;

public class InactiveUserException extends RuntimeException {
    protected InactiveUserException(String message) {
        super(message);
    }

    public static InactiveUserException userInactive() {
        return new InactiveUserException("Account is inactive");
    }
}
