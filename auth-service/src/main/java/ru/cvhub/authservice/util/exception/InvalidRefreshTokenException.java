package ru.cvhub.authservice.util.exception;

public class InvalidRefreshTokenException extends RuntimeException {
    public InvalidRefreshTokenException(String message) {
        super(message);
    }
    public InvalidRefreshTokenException(){
        super("Invalid refresh token");
    }
}
