package ru.cvhub.authservice.grpc;

import io.grpc.*;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ErrorHandlingInterceptor implements ServerInterceptor {

    private final List<AbstractGrpcErrorHandler<? extends Throwable>> handlers;

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
        return new ForwardingServerCallListener.SimpleForwardingServerCallListener<>(serverCallHandler.startCall(serverCall, metadata)) {
            @Override
            public void onHalfClose() {
                try {
                    super.onHalfClose();
                } catch (RuntimeException ex) {
                    Status status = mapExceptionToStatus(ex);
                    Metadata metadata = new Metadata();
                    metadata.put(Metadata.Key.of("error_type", Metadata.ASCII_STRING_MARSHALLER), ex.getClass().getSimpleName());
                    serverCall.close(status, metadata);
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

        return Status.UNKNOWN
                .withDescription("Unexpected error: " + (ex.getMessage() != null ? ex.getMessage() : "No message"))
                .withCause(ex);
    }
}
