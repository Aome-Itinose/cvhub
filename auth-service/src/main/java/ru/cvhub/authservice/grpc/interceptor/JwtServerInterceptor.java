package ru.cvhub.authservice.grpc.interceptor;

import com.google.common.base.Strings;
import io.grpc.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.cvhub.authservice.security.JwtUtil;
import ru.cvhub.authservice.util.exception.AuthenticationException;

@Component
@RequiredArgsConstructor
public class JwtServerInterceptor implements ServerInterceptor {

    private final JwtUtil jwtUtil;

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> serverCall,
            Metadata metadata,
            ServerCallHandler<ReqT, RespT> serverCallHandler
    ) {
        String bearerHeader = metadata.get(Metadata.Key.of("Authorization", Metadata.ASCII_STRING_MARSHALLER));
        try {
            JwtUtil.JwtUserDetails userDetails = validateAndGetUserDetails(bearerHeader);
            Context ctx = Context.current().withValue(Context.key("userId"), userDetails.id());

            return Contexts.interceptCall(ctx, serverCall, metadata, serverCallHandler);
        } catch (AuthenticationException e) {
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
