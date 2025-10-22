package ru.cvhub.authservice.grpc.interceptor;

import io.grpc.Status;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractGrpcErrorHandler<T extends Throwable> {
    private final Class<T> exceptionClass;

    public AbstractGrpcErrorHandler(@NotNull Class<T> exceptionClass) {
        this.exceptionClass = exceptionClass;
    }

    /**
     * Проверяет, может ли этот handler обработать исключение.
     *
     * @param throwable Исключение.
     * @return true, если исключение соответствует типу T.
     */
    public boolean canHandle(Throwable throwable) {
        return exceptionClass.isInstance(throwable);
    }

    /**
     * Обрабатывает исключение, модифицируя Status.Builder.
     *
     * @param throwable Исключение (гарантированно типа T).
     * @return Status.
     */
    public abstract Status handle(T throwable);
}