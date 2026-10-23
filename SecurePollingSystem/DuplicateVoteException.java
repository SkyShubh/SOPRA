/**
 * Custom checked exception thrown when a voter attempts to cast multiple votes
 * in the same poll.
 */
public class DuplicateVoteException extends Exception {

    /**
     * Constructs a new DuplicateVoteException with the specified error message.
     *
     * @param message descriptive error message explaining why the vote was rejected
     */
    public DuplicateVoteException(String message) {
        super(message);
    }
}
