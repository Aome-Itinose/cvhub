package ru.cvhub.authservice.grpc;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.cvhub.authservice.services.facade.AuthFacade;

@GrpcService
@RequiredArgsConstructor
public class AuthServiceGrpcImpl extends AuthServiceGrpc.AuthServiceImplBase {
    private final AuthFacade authFacade;

    @Override
    public void register(AS.RegisterRequest request, StreamObserver<AS.TokenResponse> responseObserver) {
        AS.TokenResponse response = authFacade.register(request);
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void login(AS.LoginRequest request, StreamObserver<AS.TokenResponse> responseObserver) {
        // TODO implement
        responseObserver.onError(new UnsupportedOperationException("Not implemented"));
    }

    @Override
    public void refreshToken(AS.RefreshTokenRequest request, StreamObserver<AS.TokenResponse> responseObserver) {
        // TODO implement
        responseObserver.onError(new UnsupportedOperationException("Not implemented"));
    }
}
