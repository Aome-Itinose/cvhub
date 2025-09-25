package ru.cvhub.authservice.util.exception;

public class WeakPasswordException extends PreconditionException {
    public WeakPasswordException(String message) {
        super(message);
    }
}
