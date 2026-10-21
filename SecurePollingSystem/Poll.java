import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;

public class Poll {
    private int pollId;
    private String question;
    private HashMap<String, Integer> options;
    private HashSet<String> voters;
    private boolean isOpen;

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

    public int getPollId() {
        return pollId;
    }

    public String getQuestion() {
        return question;
    }

    public HashMap<String, Integer> getOptions() {
        return options;
    }

    public HashSet<String> getVoters() {
        return voters;
    }

    public boolean isOpen() {
        return isOpen;
    }

    public void setOpen(boolean isOpen) {
        this.isOpen = isOpen;
    }

    /**
     * Casts a vote in the poll.
     * Duplicate prevention logic:
     * - Each poll maintains a HashSet of unique voter IDs (voters).
     * - HashSet provides O(1) average-time lookups and uniqueness guarantees.
     * - We check if the voter already exists and use HashSet.add()'s boolean return
     *   (which returns true if the element was added, false if already present)
     *   to prevent the same voter from voting multiple times in this poll.
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

        // Validation c: Check if voter has already voted in this poll using HashSet
        if (voters.contains(cleanVoterId)) {
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
        // Use the boolean returned by HashSet.add() to record the voter.
        // Returns false if the voterId is already present in the set.
        boolean added = voters.add(cleanVoterId);
        if (!added) {
            throw new DuplicateVoteException("Voter '" + cleanVoterId + "' has already voted in this poll.");
        }

        // Increment the vote count for the matching option
        options.put(matchedOptionKey, options.get(matchedOptionKey) + 1);
    }

    /**
     * Returns the total number of votes cast in this poll.
     */
    public int getTotalVotes() {
        return voters.size();
    }
}
