package ru.cvhub.authservice.util.exception;

public class InvalidInputException extends PreconditionException {
    public InvalidInputException(String message) {
        super(message);
    }
}
