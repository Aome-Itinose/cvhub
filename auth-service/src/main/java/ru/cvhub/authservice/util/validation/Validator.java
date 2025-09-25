package ru.cvhub.authservice.util.validation;

public interface Validator<R, T> {
    R throwIfInvalidContent(T obj) throws RuntimeException;
    R throwIfExists(T obj) throws RuntimeException;
}
