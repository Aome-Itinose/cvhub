package ru.cvhub.authservice.grpc;

import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.cvhub.authservice.grpc.interceptor.JwtServerInterceptor;

@GrpcService(interceptors = {JwtServerInterceptor.class})
public class PrivateAuthServiceGrpcImpl extends PrivateAuthServiceGrpc.PrivateAuthServiceImplBase {
    @Override
    public void isAuthenticated(PAS.EmptyMessage request, StreamObserver<PAS.AuthStatusResponse> responseObserver) {
        responseObserver.onNext(
                PAS.AuthStatusResponse.newBuilder()
                        .setIsAuthenticated(true)
                        .build()
        );
        responseObserver.onCompleted();
    }
}
