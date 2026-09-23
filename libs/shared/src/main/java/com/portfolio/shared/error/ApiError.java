package com.portfolio.shared.error;

import java.time.Instant;
import java.util.Map;

/**
 * Structured API error body for validation and security failures (WP0.6 / WP1.2).
 */
public record ApiError(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        Map<String, String> fieldErrors
) {
}
