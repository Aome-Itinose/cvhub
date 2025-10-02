package ru.cvhub.authservice.util.exception;

public class InternalException extends RuntimeException {
    public InternalException() {
        super("Internal server error");
    }
}
