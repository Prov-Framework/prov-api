package com.provframework.api;

import java.util.Collection;
import java.util.Objects;
import java.util.stream.Stream;

public class StreamUtils {
    private StreamUtils() {
        // Static class
    }

    public static <T> Stream<T> getNonNullStream(Collection<T> collection) {
        return Stream.ofNullable(collection) // Null list check
        .flatMap(Collection::stream) // Convert to stream
        .filter(Objects::nonNull); // Null element check
    }
}
