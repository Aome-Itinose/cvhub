package ru.cvhub.authservice.grpc;

import java.util.UUID;

public class TestFixtures {
    public static final UUID TEST_EXISTING_USER_ID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
    public static final UUID TEST_EXISTING_ANOTHER_USER_ID = UUID.fromString("123e4567-e89b-12d3-a456-426614174002");
    public static final UUID TEST_EXISTING_INACTIVE_USER_ID = UUID.fromString("123e4567-e89b-12d3-a456-426614174001");

    public static final String TEST_EXISTING_USER_EMAIL = "eva-tester@domain.zone";
    public static final String TEST_EXISTING_ANOTHER_USER_EMAIL = "another-tester@domain.zone";
    public static final String TEST_EXISTING_INACTIVE_USER_EMAIL = "inactive-tester@doman.zone";
    public static final String TEST_VALID_EMAIL = "sometester@domain.zone";

    public static final String TEST_VALID_PASSWORD = "C0mpl3x#Password!";

    public static final UUID TEST_VALID_REFRESH_TOKEN = UUID.fromString("d290f1ee-6c54-4b01-90e6-d701748f0851");
    public static final UUID TEST_EXPIRED_REFRESH_TOKEN = UUID.fromString("d290f1ee-6c54-4b01-90e6-d701748f0852");
    public static final UUID TEST_INACTIVE_USERS_REFRESH_TOKEN = UUID.fromString("d290f1ee-6c54-4b01-90e6-d701748f0853");

}
