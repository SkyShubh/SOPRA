import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

// Hashes voter IDs with SHA-256 to protect privacy while preventing duplicate votes
public final class HashUtil {

    private HashUtil() {}

    public static String hash(String input) {
        if (input == null) {
            throw new IllegalArgumentException("Input string to hash cannot be null.");
        }

        // Trim and lowercase so "S1001" and " s1001 " produce the exact same hash
        String normalized = input.trim().toLowerCase();

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(normalized.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not supported.", e);
        }
    }
}
