package com.portfolio.shared.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/** Constant-time API key comparison helper. */
public final class SecureApiKeyEquals {

    private SecureApiKeyEquals() {
    }

    public static boolean matches(String presented, String expected) {
        if (presented == null || expected == null) {
            return false;
        }
        byte[] left = presented.getBytes(StandardCharsets.UTF_8);
        byte[] right = expected.getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(left, right);
    }
}
