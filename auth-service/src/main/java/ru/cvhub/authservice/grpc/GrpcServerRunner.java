package ru.cvhub.authservice.grpc;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Profile("!test")
public class GrpcServerRunner implements CommandLineRunner {
    private AuthServiceGrpcImpl authServiceImpl;
    private ErrorHandlingInterceptor errorHandlingInterceptor;

    @Override
    public void run(String... args) throws Exception {
        Server server = ServerBuilder.forPort(9090)
                .addService(authServiceImpl)
                .intercept(errorHandlingInterceptor)
                .build();
        server.start();
        server.awaitTermination();
    }


}