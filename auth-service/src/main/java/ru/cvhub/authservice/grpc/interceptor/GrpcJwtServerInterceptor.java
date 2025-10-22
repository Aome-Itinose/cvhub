package ru.cvhub.authservice.grpc.interceptor;

import com.google.common.base.Strings;
import io.grpc.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Marker;
import org.springframework.stereotype.Component;
import ru.cvhub.authservice.security.JwtUtil;
import ru.cvhub.authservice.util.exception.AuthenticationException;

import java.util.Map;

import static ru.cvhub.authservice.util.logging.LogUtil.*;

@Component
@RequiredArgsConstructor
public class GrpcJwtServerInterceptor implements ServerInterceptor {
    private static final Marker LOG_MARKER = marker("GRPC", "INTERCEPTOR", "SECURITY");

    private final JwtUtil jwtUtil;

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> serverCall,
            Metadata metadata,
            ServerCallHandler<ReqT, RespT> serverCallHandler
    ) {
        info(LOG_MARKER, "Starting authentication for gRPC call");

        String bearerHeader = metadata.get(Metadata.Key.of("Authorization", Metadata.ASCII_STRING_MARSHALLER));
        try {
            JwtUtil.JwtUserDetails userDetails = validateAndGetUserDetails(bearerHeader);
            Context ctx = Context.current().withValue(Context.key("userId"), userDetails.id());

            info(LOG_MARKER, "Authenticated gRPC call", Map.of("userId", userDetails.id()));
            return Contexts.interceptCall(ctx, serverCall, metadata, serverCallHandler);
        } catch (AuthenticationException e) {
            warn(LOG_MARKER, "Unauthenticated gRPC call attempt", "error", e);

            serverCall.close(Status.UNAUTHENTICATED.withDescription(e.getMessage()), new Metadata());
            return new ServerCall.Listener<>() {};
        }
    }

    private JwtUtil.JwtUserDetails validateAndGetUserDetails(String bearerHeader) {
        if (Strings.isNullOrEmpty(bearerHeader) || !bearerHeader.startsWith("Bearer ")) {
            throw AuthenticationException.invalidAccessToken();
        }
        String accessToken = bearerHeader.substring(7);
        return jwtUtil.parseToken(accessToken);
    }


}
