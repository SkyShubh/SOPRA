import java.util.ArrayList;
import java.util.HashMap;

public class PollManager {
    private HashMap<Integer, Poll> polls;
    private int nextPollId;

    public PollManager() {
        this.polls = new HashMap<Integer, Poll>();
        this.nextPollId = 1;
    }

    public Poll createPoll(String question, ArrayList<String> options) throws InvalidOptionException {
        if (options.size() < 2) {
            throw new InvalidOptionException("A poll must have at least 2 options.");
        }
        Poll poll = new Poll(nextPollId, question, options);
        polls.put(nextPollId, poll);
        nextPollId++;
        return poll;
    }

    public Poll getPoll(int pollId) throws InvalidPollException {
        Poll poll = polls.get(pollId);
        if (poll == null) {
            throw new InvalidPollException("Poll with ID " + pollId + " does not exist.");
        }
        return poll;
    }

    public void vote(int pollId, String voterId, String option)
            throws InvalidPollException, DuplicateVoteException, InvalidOptionException {
        Poll poll = getPoll(pollId);
        poll.vote(voterId, option);
    }

    public void showResults(int pollId) throws InvalidPollException {
        Poll poll = getPoll(pollId);
        poll.printResults();
    }

    public void listAllPolls() {
        if (polls.isEmpty()) {
            System.out.println("No polls available.");
            return;
        }
        System.out.println("\nAll Polls:");
        for (int id : polls.keySet()) {
            Poll poll = polls.get(id);
            System.out.println("Poll ID " + poll.getPollId() + ": " + poll.getQuestion());
        }
    }
}

