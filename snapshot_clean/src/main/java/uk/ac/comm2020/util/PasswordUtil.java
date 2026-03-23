package uk.ac.comm2020.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class PasswordUtil {

    private static final String ALGORITHM = "SHA-256";

    private PasswordUtil() {}

    /**
     * Hash a password with SHA-256 and return hex-encoded string.
     * Used for storing in DB and for verification.
     */
    public static String hash(String password) {
        if (password == null) return "";
        try {
            MessageDigest md = MessageDigest.getInstance(ALGORITHM);
            byte[] digest = md.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    /**
     * Verify plain password against stored hash (hex-encoded SHA-256).
     */
    public static boolean verify(String password, String storedHash) {
        if (password == null || storedHash == null || storedHash.isBlank()) return false;
        String computed = hash(password);
        return computed.equalsIgnoreCase(storedHash.trim());
    }
}
