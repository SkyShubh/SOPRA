import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Utility class providing cryptographic hashing functionality for voter IDs.
 * Uses SHA-256 with input normalization (trimming and lowercasing) to protect voter privacy
 * while maintaining deterministic duplicate vote detection.
 */
public final class HashUtil {

    /**
     * Private constructor to prevent instantiation of utility class.
     */
    private HashUtil() {
        // Utility class; no instances allowed.
    }

    /**
     * Hashes an input string using SHA-256 and returns a 64-character lowercase hexadecimal string.
     * The input is trimmed of whitespace and converted to lowercase prior to hashing
     * to ensure case-insensitive and whitespace-tolerant voter identification.
     *
     * @param input the raw string to hash (e.g., voter ID)
     * @return 64-character hex string representing the SHA-256 digest
     * @throws IllegalArgumentException if the input string is null
     */
    public static String hash(String input) {
        if (input == null) {
            throw new IllegalArgumentException("Input string to hash cannot be null.");
        }

        String normalized = input.trim().toLowerCase();

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(normalized.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder(64);
            for (byte b : hashBytes) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            // NoSuchAlgorithmException is checked; wrap in RuntimeException because SHA-256
            // is guaranteed to be present in every Java SE runtime environment.
            throw new RuntimeException("SHA-256 algorithm not available in this Java runtime environment.", e);
        }
    }
}
