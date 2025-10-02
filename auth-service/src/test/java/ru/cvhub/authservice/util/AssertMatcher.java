package ru.cvhub.authservice.util;

import org.assertj.core.api.AbstractObjectAssert;
import org.assertj.core.api.Assertions;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public class AssertMatcher<T> extends AbstractObjectAssert<AssertMatcher<T>, Optional<T>> {

    protected AssertMatcher(Optional<T> actual) {
        super(actual, AssertMatcher.class);
    }

    public static <T> AssertMatcher<T> assertThatOptional(Optional<T> actual) {
        return new AssertMatcher<>(actual);
    }

    public AssertMatcher<T> hasPartialMatch(T expected, String... ignoredFields) {
        isNotNull();
        isPresent();
        extracting(Optional::get)
                .usingRecursiveComparison()
                .ignoringFields(ignoredFields)
                .ignoringCollectionOrder()
                .isEqualTo(expected);
        return this;
    }

    private void isPresent() {
        if (actual.isEmpty()) {
            failWithMessage("Expected Optional to be present, but it was empty");
        }
    }

    public AssertMatcher<T> hasPartialMatch(Map<String, Object> expectedFields) {
        isNotNull();
        isPresent();
        extracting(Optional::get)
                .satisfies(actual -> {
                    for (Map.Entry<String, Object> entry : expectedFields.entrySet()) {
                        try {
                            Field field = actual.getClass().getDeclaredField(entry.getKey());
                            field.setAccessible(true);
                            Object actualValue = field.get(actual);
                            Assertions.assertThat(actualValue).isEqualTo(entry.getValue());
                        } catch (NoSuchFieldException | IllegalAccessException e) {
                            throw new IllegalArgumentException("Error accessing field: " + entry.getKey(), e);
                        }
                    }
                });
        return this;
    }

    // Поддержка Consumer<AbstractObjectAssert>
    public AssertMatcher<T> satisfiesAdditional(Consumer<AbstractObjectAssert<?, T>> check) {
        isNotNull();
        isPresent();
        AbstractObjectAssert<?, T> assertion = extracting(Optional::get);
        check.accept(assertion);
        return this;
    }

    // Поддержка Consumer<T> для удобства
    public AssertMatcher<T> satisfiesAdditionalForObject(Consumer<T> check) {
        isNotNull();
        isPresent();
        extracting(Optional::get).satisfies(check);
        return this;
    }
}