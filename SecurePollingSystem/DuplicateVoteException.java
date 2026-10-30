// Thrown when a voter attempts to vote more than once in the same poll
public class DuplicateVoteException extends Exception {
    public DuplicateVoteException(String message) {
        super(message);
    }
}
