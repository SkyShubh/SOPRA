// Thrown when a poll does not exist or an operation is invalid for its state
public class InvalidPollException extends Exception {
    public InvalidPollException(String message) {
        super(message);
    }
}
