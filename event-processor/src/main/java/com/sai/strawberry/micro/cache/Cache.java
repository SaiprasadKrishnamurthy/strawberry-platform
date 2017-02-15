package com.sai.strawberry.micro.cache;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * Created by saipkri on 03/02/17.
 */
public final class Cache {

    private Cache() {
    }

    private static final ConcurrentHashMap<String, Object> CACHE = new ConcurrentHashMap<>();

    public static <T> T get(final String key, final Supplier<T> supplier) {
        CACHE.compute(key, (k, v) -> (v == null) ? supplier.get() : v);
        try {
            return (T) CACHE.get(key);
        } catch (ClassCastException c) {
            return null;
        }
    }
}
