import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a single poll within the Secure Online Polling System.
 * Encapsulates the poll question, available voting options and their respective vote counts,
 * the set of cryptographically hashed voter IDs to enforce duplicate-vote prevention securely,
 * and the open/closed status of the poll.
 * Implements {@link Serializable} for file persistence.
 */
public class Poll implements Serializable {

    private static final long serialVersionUID = 1L;

    private int pollId;
    private String question;
    private HashMap<String, Integer> options;
    private HashSet<String> voters;
    private boolean isOpen;

    /**
     * Constructs a new Poll with the specified ID, question, and candidate options.
     * Initializes all candidate options with zero votes and marks the poll as open.
     *
     * @param pollId     unique integer identifier for this poll
     * @param question   the polling question text
     * @param optionList list of available options for this poll
     */
    public Poll(int pollId, String question, List<String> optionList) {
        this.pollId = pollId;
        this.question = question;
        this.options = new LinkedHashMap<>();
        if (optionList != null) {
            for (String option : optionList) {
                if (option != null && !option.trim().isEmpty()) {
                    this.options.put(option.trim(), 0);
                }
            }
        }
        this.voters = new HashSet<>();
        this.isOpen = true;
    }

    /**
     * Returns the unique identifier of this poll.
     *
     * @return poll ID
     */
    public int getPollId() {
        return pollId;
    }

    /**
     * Returns the question associated with this poll.
     *
     * @return poll question
     */
    public String getQuestion() {
        return question;
    }

    /**
     * Returns the map of option names and their current vote counts.
     *
     * @return map of options to vote counts
     */
    public HashMap<String, Integer> getOptions() {
        return options;
    }

    /**
     * Returns the set of hashed voter IDs that have cast a vote in this poll.
     *
     * @return set of SHA-256 voter ID hashes
     */
    public HashSet<String> getVoters() {
        return voters;
    }

    /**
     * Checks if this poll is currently open for voting.
     *
     * @return true if the poll is open, false otherwise
     */
    public boolean isOpen() {
        return isOpen;
    }

    /**
     * Sets the open/closed status of this poll.
     *
     * @param isOpen true to open the poll, false to close it
     */
    public void setOpen(boolean isOpen) {
        this.isOpen = isOpen;
    }

    /**
     * Closes the poll so that no further votes can be cast.
     *
     * @throws InvalidPollException if the poll is already closed
     */
    public void closePoll() throws InvalidPollException {
        if (!isOpen) {
            throw new InvalidPollException("Poll with ID " + pollId + " is already closed.");
        }
        this.isOpen = false;
    }

    /**
     * Returns the option-to-vote-count mapping for this poll.
     *
     * @return map containing each option and its current vote tally
     */
    public HashMap<String, Integer> getResults() {
        return options;
    }

    /**
     * Calculates the vote percentage for a specific option based on total votes cast.
     * Percentage calculation logic:
     * - If total votes is 0, returns 0.0 to prevent division by zero.
     * - Otherwise calculates: (optionVotes / totalVotes) * 100.0.
     * - Performs case-insensitive matching to find the option's vote count.
     *
     * @param option the candidate option name
     * @return percentage of total votes received (0.0 to 100.0)
     */
    public double getPercentage(String option) {
        int totalVotes = getTotalVotes();
        if (totalVotes == 0 || option == null) {
            return 0.0;
        }
        String cleanOption = option.trim();
        for (Map.Entry<String, Integer> entry : options.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(cleanOption)) {
                return ((double) entry.getValue() / totalVotes) * 100.0;
            }
        }
        return 0.0;
    }

    /**
     * Finds the winning option(s) with the highest number of votes.
     * Tie-detection logic:
     * - If total votes is 0, returns an empty list.
     * - Iterates over all vote counts in the options map to find the maximum count.
     * - Gathers all options sharing this maximum count into the winners list.
     * - If more than one option shares this maximum count, all tied options are returned.
     *
     * @return list of winning option names, or empty list if no votes have been cast
     */
    public List<String> getWinners() {
        List<String> winners = new ArrayList<>();
        if (getTotalVotes() == 0) {
            return winners;
        }

        int maxVotes = -1;
        for (int votes : options.values()) {
            if (votes > maxVotes) {
                maxVotes = votes;
            }
        }

        if (maxVotes > 0) {
            for (Map.Entry<String, Integer> entry : options.entrySet()) {
                if (entry.getValue() == maxVotes) {
                    winners.add(entry.getKey());
                }
            }
        }

        return winners;
    }

    /**
     * Casts a vote in the poll with voter privacy protection and duplicate prevention.
     * Security and duplicate prevention logic:
     * - Verifies that the poll is currently open.
     * - Validates that the voter ID is not null or blank.
     * - Securely hashes the normalized (trimmed and lowercase) voter ID with SHA-256 via {@link HashUtil}.
     * - Checks if the SHA-256 hash already exists in the voters {@link HashSet}.
     * - Ensures the chosen option exists in the poll options map (case-insensitive).
     * - Records the hashed voter ID using {@code HashSet.add()} which guarantees O(1) duplicate prevention.
     * - Increments the vote tally for the matched option.
     *
     * @param voterId the raw voter ID string provided by the user
     * @param option  the option selected by the voter
     * @throws DuplicateVoteException   if this voter has already voted in this poll
     * @throws InvalidOptionException   if the option does not exist in this poll
     * @throws InvalidPollException     if the poll is closed
     * @throws IllegalArgumentException if voterId or option is empty
     */
    public void castVote(String voterId, String option)
            throws DuplicateVoteException, InvalidOptionException, InvalidPollException {
        // Validation a: Poll must be open
        if (!isOpen) {
            throw new InvalidPollException("Poll with ID " + pollId + " is closed for voting.");
        }

        // Validation b: Voter ID cannot be null or empty
        if (voterId == null || voterId.trim().isEmpty()) {
            throw new IllegalArgumentException("Voter ID cannot be empty.");
        }
        String cleanVoterId = voterId.trim();

        // Secure Voter ID Hashing:
        // Hash the voter ID with SHA-256. HashUtil normalizes (trim + lowercase)
        // so "S1001", "s1001", and " S1001 " yield the exact same SHA-256 hash.
        String hashedVoterId = HashUtil.hash(cleanVoterId);

        // Validation c: Check if voter has already voted in this poll using HashSet
        if (voters.contains(hashedVoterId)) {
            throw new DuplicateVoteException("Voter '" + cleanVoterId + "' has already voted in this poll.");
        }

        // Validation d: Option must exist in options HashMap (case-insensitive & trimmed)
        if (option == null || option.trim().isEmpty()) {
            throw new InvalidOptionException("Option cannot be empty.");
        }
        String cleanOption = option.trim();
        String matchedOptionKey = null;
        for (String existingOption : options.keySet()) {
            if (existingOption.equalsIgnoreCase(cleanOption)) {
                matchedOptionKey = existingOption;
                break;
            }
        }
        if (matchedOptionKey == null) {
            throw new InvalidOptionException("Option '" + cleanOption + "' is not a valid option for this poll.");
        }

        // Duplicate prevention logic:
        // Use the boolean returned by HashSet.add() to record the hashed voter ID.
        // Returns false if the hashed ID is already present in the set.
        boolean added = voters.add(hashedVoterId);
        if (!added) {
            throw new DuplicateVoteException("Voter '" + cleanVoterId + "' has already voted in this poll.");
        }

        // Increment the vote count for the matching option
        options.put(matchedOptionKey, options.get(matchedOptionKey) + 1);
    }

    /**
     * Returns the total number of votes cast in this poll.
     *
     * @return total vote count
     */
    public int getTotalVotes() {
        return voters.size();
    }
}
