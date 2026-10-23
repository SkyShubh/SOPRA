/**
 * Custom checked exception thrown when a requested poll does not exist or
 * when an operation cannot be performed on a closed poll.
 */
public class InvalidPollException extends Exception {

    /**
     * Constructs a new InvalidPollException with the specified error message.
     *
     * @param message descriptive error message explaining why the poll operation is invalid
     */
    public InvalidPollException(String message) {
        super(message);
    }
}
