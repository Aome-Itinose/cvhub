package ru.cvhub.authservice.util.exception;

public class ExpiredRefreshTokenException extends RuntimeException {
    public ExpiredRefreshTokenException(String message) {
        super(message);
    }
    public ExpiredRefreshTokenException(){
        super("Expired refresh token");
    }
}
