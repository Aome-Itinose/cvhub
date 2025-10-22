package ru.cvhub.authservice.grpc.interceptor;

import io.grpc.*;
import net.devh.boot.grpc.server.interceptor.GrpcGlobalServerInterceptor;
import org.slf4j.MDC;
import org.slf4j.Marker;
import org.springframework.core.annotation.Order;

import static ru.cvhub.authservice.util.logging.LogUtil.*;

@Order(1)
@GrpcGlobalServerInterceptor
public class GrpcLoggingInterceptor implements ServerInterceptor {
    private static final Marker LOG_MARKER = marker("GRPC", "INTERCEPTOR");

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> serverCall,
            Metadata metadata,
            ServerCallHandler<ReqT, RespT> serverCallHandler
    ) {
        String methodName = serverCall.getMethodDescriptor().getFullMethodName();

        MDC.put("grpc_method", methodName);
        info(LOG_MARKER, "Received gRPC call");

        ServerCall<ReqT, RespT> loggingServerCall = new ForwardingServerCall.SimpleForwardingServerCall<>(serverCall) {
            @Override
            public void close(Status status, Metadata trailers) {
                if (status.isOk()) {
                    info(LOG_MARKER, "gRPC call completed successfully", "status", status);
                } else {
                    if (status.getCode() == Status.Code.INTERNAL) {
                        error(LOG_MARKER, "gRPC call failed", "status", status);
                    } else {
                        warn(LOG_MARKER, "gRPC call failed", "status", status);
                    }
                }
                super.close(status, trailers);
            }
        };

        return serverCallHandler.startCall(loggingServerCall, metadata);
    }
}
