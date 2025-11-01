package ru.cvhub.authservice.util.logging;

import com.fasterxml.jackson.annotation.JsonProperty;
import net.logstash.logback.argument.StructuredArguments;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.util.Arrays;
import java.util.Map;

public class LogUtil {
    private static final Logger log = LoggerFactory.getLogger(LogUtil.class);

    public static void info(
            @NotNull Marker marker,
            @NotNull String message,
            @NotNull String key,
            @NotNull Object body
    ) {
        info(marker, message, Map.of(key, body));
    }

    public static void info(
            @NotNull Marker marker,
            @NotNull String message,
            @NotNull Map<String, Object> objects
    ) {
        Map<String, Payload> payloads = toPayloadMap(objects);
        log.info(marker, message, StructuredArguments.value("payloads", payloads));
    }

    public static void info(
            @NotNull Marker marker,
            @NotNull String message
    ) {
        log.info(marker, message);
    }

    public static void error(
            @NotNull Marker marker,
            @NotNull String message,
            @NotNull String key,
            @NotNull Object body
    ) {
        error(marker, message, Map.of(key, body));
    }

    public static void error(
            @NotNull Marker marker,
            @NotNull String message,
            @NotNull Map<String, Object> objects
    ) {
        Map<String, Payload> payloads = toPayloadMap(objects);
        log.error(marker, message, StructuredArguments.value("payloads", payloads));
    }

    public static void warn(
            @NotNull Marker marker,
            @NotNull String message,
            @NotNull String key,
            @NotNull Object body
    ) {
        warn(marker, message, Map.of(key, body));
    }

    public static void warn(
            @NotNull Marker marker,
            @NotNull String message,
            @NotNull Map<String, Object> objects
    ) {
        Map<String, Payload> payloads = toPayloadMap(objects);
        log.warn(marker, message, StructuredArguments.value("payloads", payloads));
    }

    private static @NotNull Map<String, Payload> toPayloadMap(
            @NotNull Map<String, Object> objects
    ) {
        return objects.entrySet().stream()
                .collect(
                        java.util.stream.Collectors.toMap(
                                Map.Entry::getKey,
                                entry -> new Payload(entry.getValue())
                        )
                );
    }

    public static Marker marker(String... markerNames) {
        return Arrays.stream(markerNames)
                .map(MarkerFactory::getMarker)
                .reduce((left, right) -> {
                    left.add(right);
                    return left;
                })
                .orElseThrow(() -> new IllegalArgumentException("At least one marker name required"));

    }

    public static String hideSensitiveData(String data) {
        if (data == null || data.length() <= 4) {
            return "****";
        }
        int visibleChars = 4;
        String maskedPart = "*".repeat(data.length() - visibleChars);
        return maskedPart + data.substring(data.length() - visibleChars);
    }

    private static String getClassName(
            @NotNull Object obj
    ) {
        if (obj instanceof EntityLog entityLog) {
            return entityLog.getClassName();
        } else {
            return obj.getClass().getName();
        }
    }

    private record Payload(
            @JsonProperty("class_name")
            String className,
            @JsonProperty("body")
            Object body
    ) {
        public Payload(Object obj) {
            this(getClassName(obj), obj);
        }
    }
}
