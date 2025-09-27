package ru.cvhub.authservice.util.validation;

import org.jetbrains.annotations.NotNull;

public interface Validator<R, T> {
    void validate(@NotNull T obj) throws RuntimeException;
}
