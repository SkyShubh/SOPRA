import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

public class PollManager {
    private HashMap<Integer, Poll> polls;
    private int nextPollId;

    public PollManager() {
        this.polls = new LinkedHashMap<>();
        this.nextPollId = 101; // Default starting ID matching project specifications
    }

    public PollManager(int startingId) {
        this.polls = new LinkedHashMap<>();
        this.nextPollId = startingId;
    }

    public Poll createPoll(String question, List<String> options) {
        int id = nextPollId++;
        Poll poll = new Poll(id, question, options);
        polls.put(id, poll);
        return poll;
    }

    public Poll getPoll(int id) throws InvalidPollException {
        Poll poll = polls.get(id);
        if (poll == null) {
            throw new InvalidPollException("Poll with ID " + id + " does not exist.");
        }
        return poll;
    }

    public HashMap<Integer, Poll> getAllPolls() {
        return polls;
    }
}
