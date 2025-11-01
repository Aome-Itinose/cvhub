package ru.cvhub.authservice.util.exception;

public final class InvalidInputException extends PreconditionException {
    private InvalidInputException(String message) {
        super(message);
    }

    public static InvalidInputException invalidRefreshToken() {
        return new InvalidInputException("Invalid refresh token format");
    }

    public static InvalidInputException invalidEmailFormat() {
        return new InvalidInputException("Invalid email format");
    }

    public static InvalidInputException blankEmail() {
        return new InvalidInputException("Email must be provided");
    }

    public static InvalidInputException invalidPasswordFormat() {
        return new InvalidInputException("Password must be at least 8 characters. " +
                "Contains at least one uppercase letter, one lowercase letter, one digit and one special character");
    }

    public static InvalidInputException blankPassword() {
        return new InvalidInputException("Password must be provided");
    }
}
