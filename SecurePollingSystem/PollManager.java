import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

// Manages poll operations, voting, display, and serialization
public class PollManager {

    public static final String DEFAULT_STORAGE_FILE = "polls.dat";

    private HashMap<Integer, Poll> polls;
    private int nextPollId;

    public PollManager() {
        this.polls = new LinkedHashMap<>();
        this.nextPollId = 101;
    }

    public PollManager(int startingId) {
        this.polls = new LinkedHashMap<>();
        this.nextPollId = startingId;
    }

    public int getNextPollId() {
        return nextPollId;
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

    public boolean deletePoll(int pollId) throws InvalidPollException {
        if (!polls.containsKey(pollId)) {
            throw new InvalidPollException("Poll with ID " + pollId + " does not exist.");
        }
        polls.remove(pollId);
        return true;
    }

    public List<Poll> searchPolls(String keyword) {
        List<Poll> matches = new ArrayList<>();
        if (keyword == null || keyword.trim().isEmpty()) {
            return matches;
        }
        String lowerKeyword = keyword.trim().toLowerCase();
        for (Poll poll : polls.values()) {
            if (poll.getQuestion() != null && poll.getQuestion().toLowerCase().contains(lowerKeyword)) {
                matches.add(poll);
            }
        }
        return matches;
    }

    public HashMap<String, Integer> getResults(int pollId) throws InvalidPollException {
        return getPoll(pollId).getResults();
    }

    // Displays option names and vote counts with column alignment
    public void displayOptionVotes(int pollId) throws InvalidPollException {
        Poll poll = getPoll(pollId);
        System.out.println("\nOption Vote Counts for Poll " + poll.getPollId() + ": " + poll.getQuestion());
        System.out.println("-----------------------------------------");

        HashMap<String, Integer> results = poll.getResults();
        int pad = 8;
        for (String opt : results.keySet()) {
            if (opt.length() > pad) pad = opt.length();
        }

        for (String opt : results.keySet()) {
            System.out.println(String.format("%-" + pad + "s: %d votes", opt, results.get(opt)));
        }
        System.out.println("-----------------------------------------");
    }

    // Displays vote count, percentage, and winner or tie outcome
    public void displayResults(int pollId) throws InvalidPollException {
        Poll poll = getPoll(pollId);

        String status = poll.isOpen() ? "OPEN" : "CLOSED";
        System.out.println("\nPoll " + poll.getPollId() + ": " + poll.getQuestion() + "   [" + status + "]");
        System.out.println("-----------------------------------------");

        HashMap<String, Integer> results = poll.getResults();
        int pad = 8;
        for (String opt : results.keySet()) {
            if (opt.length() > pad) pad = opt.length();
        }

        for (String opt : results.keySet()) {
            double percentage = poll.getPercentage(opt);
            System.out.println(String.format("%-" + pad + "s: %d votes  (%.1f%%)",
                    opt, results.get(opt), percentage));
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

    public void saveData() throws DataStorageException {
        saveToFile(DEFAULT_STORAGE_FILE);
    }

    public void saveToFile(String filename) throws DataStorageException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename))) {
            oos.writeInt(nextPollId);
            oos.writeObject(polls);
        } catch (IOException e) {
            throw new DataStorageException("Failed to save polls to file '" + filename + "': " + e.getMessage(), e);
        }
    }

    public void loadData() {
        loadFromFile(DEFAULT_STORAGE_FILE);
    }

    @SuppressWarnings("unchecked")
    public void loadFromFile(String filename) {
        File file = new File(filename);
        if (!file.exists()) {
            return;
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            this.nextPollId = ois.readInt();
            HashMap<Integer, Poll> loadedPolls = (HashMap<Integer, Poll>) ois.readObject();
            this.polls = (loadedPolls != null) ? loadedPolls : new LinkedHashMap<>();
        } catch (Exception e) {
            System.out.println("Warning: Corrupted or invalid data file detected ('" + filename + "'). Starting with an empty system.");
            this.polls = new LinkedHashMap<>();
            this.nextPollId = 101;
        }
    }
}
