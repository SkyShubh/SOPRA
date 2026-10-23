/**
 * Custom checked exception thrown when a user selects an option that does not exist
 * within the specified poll.
 */
public class InvalidOptionException extends Exception {

    /**
     * Constructs a new InvalidOptionException with the specified error message.
     *
     * @param message descriptive error message explaining why the option is invalid
     */
    public InvalidOptionException(String message) {
        super(message);
    }
}
