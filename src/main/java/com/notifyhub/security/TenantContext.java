package com.notifyhub.security;

public class TenantContext {

    private static final ThreadLocal<String> CURRENT = new ThreadLocal<>();

    public static void set(String schema) {
        CURRENT.set(schema);
    }

    public static String get() {
        String value = CURRENT.get();
        return value == null ? "public" : value;
    }

    public static void clear() {
        CURRENT.remove();
    }
}
