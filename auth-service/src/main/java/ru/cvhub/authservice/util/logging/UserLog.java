package ru.cvhub.authservice.util.logging;

import com.fasterxml.jackson.annotation.JsonFormat;
import ru.cvhub.authservice.store.entity.User;

import java.time.Instant;
import java.util.UUID;

public record UserLog(
        UUID id,
        @JsonFormat(
                shape = JsonFormat.Shape.STRING,
                pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSSXXX",
                timezone = "Europe/Moscow"
        )
        Instant createdAt,
        @JsonFormat(
                shape = JsonFormat.Shape.STRING,
                pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSSXXX",
                timezone = "Europe/Moscow"
        )
        Instant updatedAt,
        Boolean isActive
) implements EntityLog {
    public static UserLog from(User user) {
        return new UserLog(
                user.id(), user.createdAt(), user.updatedAt(), user.isActive()
        );
    }

    @Override
    public String getClassName() {
        return User.class.getName();
    }
}
