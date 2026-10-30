import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;

// Represents a single poll with question, options, vote tallies, and hashed voters
public class Poll implements Serializable {

    private static final long serialVersionUID = 1L;

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

    public void closePoll() throws InvalidPollException {
        if (!isOpen) {
            throw new InvalidPollException("Poll with ID " + pollId + " is already closed.");
        }
        this.isOpen = false;
    }

    public HashMap<String, Integer> getResults() {
        return options;
    }

    public int getTotalVotes() {
        return voters.size();
    }

    // Calculates percentage of votes for an option
    public double getPercentage(String option) {
        int totalVotes = getTotalVotes();
        if (totalVotes == 0 || option == null) {
            return 0.0;
        }
        for (String opt : options.keySet()) {
            if (opt.equalsIgnoreCase(option.trim())) {
                return ((double) options.get(opt) / totalVotes) * 100.0;
            }
        }
        return 0.0;
    }

    // Finds option(s) with the highest number of votes
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
            for (String opt : options.keySet()) {
                if (options.get(opt) == maxVotes) {
                    winners.add(opt);
                }
            }
        }
        return winners;
    }

    // Casts a vote after validating state, privacy hashing, and duplicate checking
    public void castVote(String voterId, String option)
            throws DuplicateVoteException, InvalidOptionException, InvalidPollException {
        if (!isOpen) {
            throw new InvalidPollException("Poll with ID " + pollId + " is closed for voting.");
        }
        if (voterId == null || voterId.trim().isEmpty()) {
            throw new IllegalArgumentException("Voter ID cannot be empty.");
        }
        if (option == null || option.trim().isEmpty()) {
            throw new InvalidOptionException("Option cannot be empty.");
        }

        String cleanVoterId = voterId.trim();
        String hashedVoterId = HashUtil.hash(cleanVoterId);

        // Check if voter has already voted in this poll
        if (voters.contains(hashedVoterId)) {
            throw new DuplicateVoteException("Voter '" + cleanVoterId + "' has already voted in this poll.");
        }

        // Match option case-insensitively
        String matchedOption = null;
        for (String opt : options.keySet()) {
            if (opt.equalsIgnoreCase(option.trim())) {
                matchedOption = opt;
                break;
            }
        }
        if (matchedOption == null) {
            throw new InvalidOptionException("Option '" + option.trim() + "' is not a valid option for this poll.");
        }

        // Record voter hash and increment count
        voters.add(hashedVoterId);
        options.put(matchedOption, options.get(matchedOption) + 1);
    }
}
