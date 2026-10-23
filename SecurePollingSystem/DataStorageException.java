/**
 * Custom checked exception thrown when an error occurs during saving or loading
 * poll data to/from persistent storage.
 */
public class DataStorageException extends Exception {

    /**
     * Constructs a new DataStorageException with the specified error message.
     *
     * @param message descriptive error message explaining the failure
     */
    public DataStorageException(String message) {
        super(message);
    }

    /**
     * Constructs a new DataStorageException with the specified error message and underlying cause.
     *
     * @param message descriptive error message explaining the failure
     * @param cause   the underlying exception causing the failure
     */
    public DataStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
