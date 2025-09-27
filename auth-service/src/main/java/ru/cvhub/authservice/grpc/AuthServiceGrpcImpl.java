package ru.cvhub.authservice.grpc;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.cvhub.authservice.services.facade.AuthFacadeImpl;
import ru.cvhub.authservice.util.mapping.Mapper;

@GrpcService
@RequiredArgsConstructor
public class AuthServiceGrpcImpl extends AuthServiceGrpc.AuthServiceImplBase {
    private final AuthFacadeImpl authFacadeImpl;

    @Override
    public void register(AS.RegisterRequest request, StreamObserver<AS.TokenResponse> responseObserver) {
        AS.TokenResponse response = Mapper.toResponse(authFacadeImpl.register(Mapper.toDto(request)));
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void login(AS.LoginRequest request, StreamObserver<AS.TokenResponse> responseObserver) {
        AS.TokenResponse response = Mapper.toResponse(authFacadeImpl.login(Mapper.toDto(request)));
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void refreshToken(AS.RefreshTokenRequest request, StreamObserver<AS.TokenResponse> responseObserver) {
        AS.TokenResponse response = Mapper.toResponse(authFacadeImpl.refresh(Mapper.toDto(request)));
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
