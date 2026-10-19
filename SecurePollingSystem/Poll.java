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
}
