package ru.cvhub.authservice.grpc;

import io.grpc.ManagedChannel;
import io.grpc.Server;
import org.junit.jupiter.api.extension.*;

import java.util.ArrayList;
import java.util.List;

public class GrpcCleanupExtension implements BeforeEachCallback, AfterEachCallback {
    private final List<Object> resources = new ArrayList<>();

    public <T> T register(T resource) {
        resources.add(resource);
        return resource;
    }

    @Override
    public void beforeEach(ExtensionContext context) {}

    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        for (int i = resources.size() - 1; i >= 0; i--) {
            Object r = resources.get(i);
            try {
                switch (r) {
                    case Server server -> server.shutdownNow();
                    case ManagedChannel managedChannel -> managedChannel.shutdownNow();
                    case AutoCloseable autoCloseable -> autoCloseable.close();
                    default -> System.out.println("Unknown resource type: " + r.getClass().getName()); //todo log
                }
            } catch (Throwable ignored) {
            }
        }
        resources.clear();
    }
}
