import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class Poll {
    private int pollId;
    private String question;
    private ArrayList<String> options;
    private HashMap<String, Integer> votes;
    private HashSet<String> voters;

    public Poll(int pollId, String question, ArrayList<String> options) {
        this.pollId = pollId;
        this.question = question;
        this.options = options;
        this.votes = new HashMap<String, Integer>();
        this.voters = new HashSet<String>();
        for (String opt : options) {
            votes.put(opt, 0);
        }
    }

    public int getPollId() {
        return pollId;
    }

    public String getQuestion() {
        return question;
    }

    public ArrayList<String> getOptions() {
        return options;
    }

    public int getTotalVotes() {
        return voters.size();
    }

    public void vote(String voterId, String option) throws DuplicateVoteException, InvalidOptionException {
        if (!votes.containsKey(option)) {
            throw new InvalidOptionException("Invalid option: " + option);
        }
        if (voters.contains(voterId)) {
            throw new DuplicateVoteException("Voter " + voterId + " has already voted in this poll.");
        }
        voters.add(voterId);
        int count = votes.get(option);
        votes.put(option, count + 1);
    }

    public void printResults() {
        System.out.println("\nResults for Poll " + pollId + ": " + question);
        int total = getTotalVotes();
        System.out.println("Total votes: " + total);
        if (total == 0) {
            System.out.println("No votes yet.");
            return;
        }
        printPercentages(total);
        printWinner();
    }

    private void printPercentages(int total) {
        for (String opt : options) {
            int count = votes.get(opt);
            double pct = ((double) count / total) * 100.0;
            double rounded = Math.round(pct * 10.0) / 10.0;
            System.out.println(opt + ": " + count + " votes (" + rounded + "%)");
        }
    }

    private void printWinner() {
        int maxVotes = -1;
        for (String opt : options) {
            int count = votes.get(opt);
            if (count > maxVotes) {
                maxVotes = count;
            }
        }
        ArrayList<String> winners = new ArrayList<String>();
        for (String opt : options) {
            if (votes.get(opt) == maxVotes) {
                winners.add(opt);
            }
        }
        if (winners.size() == 1) {
            System.out.println("Winner: " + winners.get(0));
        } else {
            System.out.println("It is a tie between: " + winners);
        }
    }
}

