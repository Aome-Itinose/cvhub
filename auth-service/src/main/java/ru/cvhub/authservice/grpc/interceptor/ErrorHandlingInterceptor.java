package ru.cvhub.authservice.grpc.interceptor;

import io.grpc.*;
import jakarta.annotation.PostConstruct;
import net.devh.boot.grpc.server.interceptor.GrpcGlobalServerInterceptor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.core.annotation.Order;

import java.util.List;

@Order(2)
@GrpcGlobalServerInterceptor
public class ErrorHandlingInterceptor implements ServerInterceptor {

    private final List<AbstractGrpcErrorHandler<? extends Throwable>> handlers;
    private final AbstractGrpcErrorHandler<Throwable> defaultExceptionHandler;

    public ErrorHandlingInterceptor(
            List<AbstractGrpcErrorHandler<? extends Throwable>> handlers,
            @Qualifier("defaultExceptionHandler") AbstractGrpcErrorHandler<Throwable> defaultExceptionHandler
    ) {
        this.handlers = handlers;
        this.defaultExceptionHandler = defaultExceptionHandler;
        this.handlers.add(defaultExceptionHandler);
    }

    @PostConstruct
    public void sortHandlers() {
        handlers.sort(AnnotationAwareOrderComparator.INSTANCE);
    }

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> serverCall,
            Metadata metadata,
            ServerCallHandler<ReqT, RespT> serverCallHandler
    ) {
        ServerCall<ReqT, RespT> forwardingServerCall = new ForwardingServerCall.SimpleForwardingServerCall<>(serverCall) {
            @Override
            public void close(Status status, Metadata trailers) {
                super.close(status, trailers);
            }
        };

        return new ForwardingServerCallListener.SimpleForwardingServerCallListener<>(serverCallHandler.startCall(forwardingServerCall, metadata)) {
            @Override
            public void onHalfClose() {
                try {
                    super.onHalfClose();
                } catch (RuntimeException ex) {
                    Status status = mapExceptionToStatus(ex);
                    Metadata metadata = new Metadata();
                    metadata.put(Metadata.Key.of("error_type", Metadata.ASCII_STRING_MARSHALLER), ex.getClass().getSimpleName());
                    forwardingServerCall.close(status, metadata);
                }
            }
        };
    }

    private Status mapExceptionToStatus(Throwable ex) {
        if (ex instanceof StatusRuntimeException) {
            return ((StatusRuntimeException) ex).getStatus();
        }

        for (AbstractGrpcErrorHandler<? extends Throwable> handler : handlers) {
            if (handler.canHandle(ex)) {
                @SuppressWarnings("unchecked")
                AbstractGrpcErrorHandler<Throwable> typedHandler = (AbstractGrpcErrorHandler<Throwable>) handler;
                return typedHandler.handle(ex);
            }
        }

        return defaultExceptionHandler.handle(ex);
    }
}
