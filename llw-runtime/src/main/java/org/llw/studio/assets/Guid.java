package org.llw.studio.assets;

import java.util.UUID;

/**
 * Utilities for generating and validating asset GUID strings (UUID format).
 */
public final class Guid {
    private Guid() {
    }

    /**
     * @return a new random UUID string suitable for asset meta files
     */
    public static String newGuid() {
        return UUID.randomUUID().toString();
    }

    /**
     * @param value candidate GUID string
     * @return {@code true} if {@code value} is a non-blank valid UUID
     */
    public static boolean isValid(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        try {
            UUID.fromString(value);
            return true;
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }
}
