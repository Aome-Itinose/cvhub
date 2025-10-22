package ru.cvhub.authservice.util.exception;

public final class InactiveUserException extends RuntimeException {
    private InactiveUserException(String message) {
        super(message);
    }

    public static InactiveUserException userInactive() {
        return new InactiveUserException("Account is inactive");
    }
}
