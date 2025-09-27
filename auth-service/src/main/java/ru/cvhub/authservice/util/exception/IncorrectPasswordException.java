package ru.cvhub.authservice.util.exception;

public class IncorrectPasswordException extends RuntimeException {
    public IncorrectPasswordException(String message) {
        super(message);
    }
    public IncorrectPasswordException(Throwable cause) {
        super(cause);
    }
    public IncorrectPasswordException(){
        super("Incorrect password");
    }
}
