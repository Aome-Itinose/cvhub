package ru.cvhub.authservice.util.exception;

public class ExpiredRefreshTokenException extends RuntimeException {
    public ExpiredRefreshTokenException(){
        super("Refresh token has expired");
    }
}
