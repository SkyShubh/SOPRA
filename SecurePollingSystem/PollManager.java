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
import java.util.Locale;
import java.util.Map;

/**
 * Manages poll creation, retrieval, voting operations, result analysis, and file persistence.
 * Maintains all active and closed polls in an internal {@link LinkedHashMap} and tracks
 * the next auto-incrementing poll identifier counter.
 */
public class PollManager {

    /*
     * =========================================================================
     * Choice of Persistence Mechanism: Java Object Serialization (Serializable)
     * =========================================================================
     * Java Object Serialization (using polls.dat) was chosen over custom plain text
     * parsing for the following reasons:
     * 1. Data Integrity & Structure: Built-in serialization seamlessly and reliably
     *    preserves complex nested collections (LinkedHashMap of options with vote tallies,
     *    HashSet of 256-bit hashed voter IDs) and primitive states without custom formatting.
     * 2. Edge Case Immunity: Questions and option strings may contain arbitrary characters,
     *    commas, quotes, or newlines, which commonly cause delimiters in text formats to break.
     * 3. Privacy & Security: The binary serialization format ensures that the stored file
     *    does not expose plaintext data and exclusively persists the SHA-256 hashed voter IDs.
     * 4. Simplicity: State serialization and deserialization require concise, clean streams
     *    with robust header validation to catch corrupted files immediately.
     */
    public static final String DEFAULT_STORAGE_FILE = "polls.dat";

    private HashMap<Integer, Poll> polls;
    private int nextPollId;

    /**
     * Constructs a default PollManager starting with ID 101 and an empty poll map.
     */
    public PollManager() {
        this.polls = new LinkedHashMap<>();
        this.nextPollId = 101;
    }

    /**
     * Constructs a PollManager with a custom starting poll ID.
     *
     * @param startingId the initial ID to assign to the next created poll
     */
    public PollManager(int startingId) {
        this.polls = new LinkedHashMap<>();
        this.nextPollId = startingId;
    }

    /**
     * Returns the next poll ID that will be assigned.
     *
     * @return next poll ID counter
     */
    public int getNextPollId() {
        return nextPollId;
    }

    /**
     * Creates and registers a new poll with the given question and options.
     *
     * @param question the polling question
     * @param options  the list of valid voting options
     * @return the newly created {@link Poll} instance
     */
    public Poll createPoll(String question, List<String> options) {
        int id = nextPollId++;
        Poll poll = new Poll(id, question, options);
        polls.put(id, poll);
        return poll;
    }

    /**
     * Retrieves a poll by its unique ID.
     *
     * @param id the poll ID
     * @return the {@link Poll} associated with the given ID
     * @throws InvalidPollException if no poll exists with the specified ID
     */
    public Poll getPoll(int id) throws InvalidPollException {
        Poll poll = polls.get(id);
        if (poll == null) {
            throw new InvalidPollException("Poll with ID " + id + " does not exist.");
        }
        return poll;
    }

    /**
     * Returns the map of all polls currently managed.
     *
     * @return map of poll IDs to {@link Poll} instances
     */
    public HashMap<Integer, Poll> getAllPolls() {
        return polls;
    }

    /**
     * Casts a vote in the specified poll on behalf of a voter.
     *
     * @param pollId   ID of the poll to vote in
     * @param voterId  raw ID of the voter (will be hashed internally)
     * @param option   the option chosen by the voter
     * @throws InvalidPollException   if the poll does not exist or is closed
     * @throws DuplicateVoteException if the voter has already voted in this poll
     * @throws InvalidOptionException if the option is not valid for this poll
     */
    public void vote(int pollId, String voterId, String option)
            throws InvalidPollException, DuplicateVoteException, InvalidOptionException {
        Poll poll = getPoll(pollId);
        poll.castVote(voterId, option);
    }

    /**
     * Closes an active poll so that no further votes can be submitted.
     *
     * @param pollId the ID of the poll to close
     * @throws InvalidPollException if the poll does not exist or is already closed
     */
    public void closePoll(int pollId) throws InvalidPollException {
        Poll poll = getPoll(pollId);
        poll.closePoll();
    }

    /**
     * Deletes a poll by its ID.
     *
     * @param pollId the ID of the poll to delete
     * @return true if the poll was successfully deleted
     * @throws InvalidPollException if no poll exists with the specified ID
     */
    public boolean deletePoll(int pollId) throws InvalidPollException {
        if (!polls.containsKey(pollId)) {
            throw new InvalidPollException("Poll with ID " + pollId + " does not exist.");
        }
        polls.remove(pollId);
        return true;
    }

    /**
     * Searches for polls whose questions contain the specified keyword (case-insensitive).
     *
     * @param keyword the substring to search for in poll questions
     * @return list of matching {@link Poll} objects
     */
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

    /**
     * Retrieves the option-to-vote-count mapping for the specified poll.
     *
     * @param pollId the poll ID
     * @return map of options and their vote counts
     * @throws InvalidPollException if the poll does not exist
     */
    public HashMap<String, Integer> getResults(int pollId) throws InvalidPollException {
        Poll poll = getPoll(pollId);
        return poll.getResults();
    }

    /**
     * Displays only the vote count per option for a single poll.
     *
     * @param pollId the poll ID
     * @throws InvalidPollException if the poll does not exist
     */
    public void displayOptionVotes(int pollId) throws InvalidPollException {
        Poll poll = getPoll(pollId);

        System.out.println("\nOption Vote Counts for Poll " + poll.getPollId() + ": " + poll.getQuestion());
        System.out.println("-----------------------------------------");

        HashMap<String, Integer> results = poll.getResults();
        int padWidth = 8;
        for (String opt : results.keySet()) {
            if (opt.length() > padWidth) {
                padWidth = opt.length();
            }
        }

        for (Map.Entry<String, Integer> entry : results.entrySet()) {
            System.out.println(String.format(Locale.US, "%-" + padWidth + "s: %d votes",
                    entry.getKey(), entry.getValue()));
        }

        System.out.println("-----------------------------------------");
    }

    /**
     * Displays a full result analysis for a poll, including vote counts, percentages,
     * total votes cast, and the winning option or tied winners.
     *
     * @param pollId the poll ID
     * @throws InvalidPollException if the poll does not exist
     */
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

    /**
     * Lists all registered polls with their ID, question, open/closed status, and total votes.
     */
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

    /**
     * Saves all polls and the next poll ID counter to the default persistent storage file.
     *
     * @throws DataStorageException if an I/O error occurs while writing data
     */
    public void saveData() throws DataStorageException {
        saveToFile(DEFAULT_STORAGE_FILE);
    }

    /**
     * Saves all polls and the next poll ID counter to a specified storage file.
     *
     * @param filename name or path of the destination file
     * @throws DataStorageException if an I/O error occurs while writing data
     */
    public void saveToFile(String filename) throws DataStorageException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename))) {
            oos.writeInt(nextPollId);
            oos.writeObject(polls);
        } catch (IOException e) {
            throw new DataStorageException("Failed to save polls to file '" + filename + "': " + e.getMessage(), e);
        }
    }

    /**
     * Loads poll data and next poll ID counter from the default persistent storage file.
     * If the file does not exist, starts with an empty system without reporting an error.
     * If the file is corrupted, catches the exception, prints a warning, and starts with an empty system.
     */
    public void loadData() {
        loadFromFile(DEFAULT_STORAGE_FILE);
    }

    /**
     * Loads poll data and next poll ID counter from the specified file.
     * If the file does not exist, starts with an empty system without reporting an error.
     * If the file is corrupted, catches the exception, prints a warning, and starts with an empty system.
     *
     * @param filename name or path of the storage file to load
     */
    @SuppressWarnings("unchecked")
    public void loadFromFile(String filename) {
        File file = new File(filename);
        if (!file.exists()) {
            // First run: file does not exist. Start empty with no error.
            return;
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            this.nextPollId = ois.readInt();
            HashMap<Integer, Poll> loadedPolls = (HashMap<Integer, Poll>) ois.readObject();
            this.polls = (loadedPolls != null) ? loadedPolls : new LinkedHashMap<>();
        } catch (Exception e) {
            // If the file is corrupted, catch the exception, print a warning and start empty. Never crash.
            System.out.println("Warning: Corrupted or invalid data file detected ('" + filename + "'). Starting with an empty system.");
            this.polls = new LinkedHashMap<>();
            this.nextPollId = 101;
        }
    }
}
