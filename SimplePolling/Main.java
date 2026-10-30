import java.util.ArrayList;
import java.util.Scanner;

public class Main {
    private static PollManager manager = new PollManager();
    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        boolean running = true;
        while (running) {
            printMenu();
            System.out.print("Enter your choice: ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    handleCreatePoll();
                    break;
                case "2":
                    handleVote();
                    break;
                case "3":
                    handleViewResults();
                    break;
                case "4":
                    manager.listAllPolls();
                    break;
                case "5":
                    System.out.println("Goodbye!");
                    running = false;
                    break;
                default:
                    System.out.println("Invalid choice. Please enter 1 to 5.");
            }
        }
    }

    private static void printMenu() {
        System.out.println("\n=== Online Polling System ===");
        System.out.println("1. Create Poll");
        System.out.println("2. Vote");
        System.out.println("3. View Results");
        System.out.println("4. List All Polls");
        System.out.println("5. Exit");
    }

    private static void handleCreatePoll() {
        System.out.print("Enter question: ");
        String question = scanner.nextLine().trim();
        if (question.isEmpty()) {
            System.out.println("Error: Question cannot be empty.");
            return;
        }

        System.out.print("Enter options (comma-separated): ");
        String optionsLine = scanner.nextLine().trim();
        ArrayList<String> options = parseOptions(optionsLine);

        try {
            Poll poll = manager.createPoll(question, options);
            System.out.println("Poll created successfully with ID: " + poll.getPollId());
        } catch (InvalidOptionException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static ArrayList<String> parseOptions(String line) {
        ArrayList<String> list = new ArrayList<String>();
        String[] parts = line.split(",");
        for (String part : parts) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                list.add(trimmed);
            }
        }
        return list;
    }

    private static void handleVote() {
        try {
            System.out.print("Enter Poll ID: ");
            int pollId = Integer.parseInt(scanner.nextLine().trim());

            Poll poll = manager.getPoll(pollId);
            System.out.println("Question: " + poll.getQuestion());
            System.out.println("Options: " + poll.getOptions());

            System.out.print("Enter Voter ID: ");
            String voterId = scanner.nextLine().trim();

            System.out.print("Enter Option: ");
            String option = scanner.nextLine().trim();

            manager.vote(pollId, voterId, option);
            System.out.println("Vote recorded successfully.");
        } catch (NumberFormatException e) {
            System.out.println("Error: Poll ID must be a number.");
        } catch (InvalidPollException e) {
            System.out.println("Error: " + e.getMessage());
        } catch (DuplicateVoteException e) {
            System.out.println("Error: " + e.getMessage());
        } catch (InvalidOptionException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void handleViewResults() {
        try {
            System.out.print("Enter Poll ID: ");
            int pollId = Integer.parseInt(scanner.nextLine().trim());
            manager.showResults(pollId);
        } catch (NumberFormatException e) {
            System.out.println("Error: Poll ID must be a number.");
        } catch (InvalidPollException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}

