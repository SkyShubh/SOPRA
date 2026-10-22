import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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

    public void vote(int pollId, String voterId, String option)
            throws InvalidPollException, DuplicateVoteException, InvalidOptionException {
        Poll poll = getPoll(pollId);
        poll.castVote(voterId, option);
    }

    public void closePoll(int pollId) throws InvalidPollException {
        Poll poll = getPoll(pollId);
        poll.closePoll();
    }

    public HashMap<String, Integer> getResults(int pollId) throws InvalidPollException {
        Poll poll = getPoll(pollId);
        return poll.getResults();
    }

    public void displayResults(int pollId) throws InvalidPollException {
        Poll poll = getPoll(pollId);

        String status = poll.isOpen() ? "OPEN" : "CLOSED";
        System.out.println("\nPoll " + poll.getPollId() + ": " + poll.getQuestion() + "   [" + status + "]");
        System.out.println("-----------------------------------------");

        HashMap<String, Integer> results = poll.getResults();
        int padWidth = 8;
        for (String opt : results.keySet()) {
            if (opt.length() > padWidth) {
                padWidth = opt.length();
            }
        }

        for (Map.Entry<String, Integer> entry : results.entrySet()) {
            double percentage = poll.getPercentage(entry.getKey());
            System.out.println(String.format(Locale.US, "%-" + padWidth + "s: %d votes  (%.1f%%)",
                    entry.getKey(), entry.getValue(), percentage));
        }

        System.out.println("-----------------------------------------");
        System.out.println("Total votes: " + poll.getTotalVotes());

        List<String> winners = poll.getWinners();
        if (winners.isEmpty()) {
            System.out.println("No votes cast yet.");
        } else if (winners.size() == 1) {
            System.out.println("Winner: " + winners.get(0));
        } else {
            System.out.println("Result: Tie between " + String.join(", ", winners));
        }
    }

    public void listAllPolls() {
        if (polls.isEmpty()) {
            System.out.println("No polls have been created yet.");
            return;
        }

        System.out.println("\nAll Polls:");
        System.out.println("------------------------------------------------------------");
        for (Poll poll : polls.values()) {
            String status = poll.isOpen() ? "OPEN" : "CLOSED";
            System.out.println("Poll " + poll.getPollId() + ": " + poll.getQuestion()
                    + " [" + status + "] - Total votes: " + poll.getTotalVotes());
        }
        System.out.println("------------------------------------------------------------");
    }
}
