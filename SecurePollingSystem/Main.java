import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        PollManager pollManager = new PollManager();
        boolean running = true;

        System.out.println("Welcome to the Secure Polling System!");

        while (running) {
            System.out.println("\n===== Secure Polling System =====");
            System.out.println("1. Create Poll");
            System.out.println("2. Vote");
            System.out.println("3. View Results");
            System.out.println("4. Close Poll");
            System.out.println("5. List All Polls");
            System.out.println("6. Exit");
            System.out.print("Choice: ");

            String input = scanner.nextLine().trim();

            int choice;
            try {
                choice = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Error: Please enter a valid numerical choice.");
                continue;
            }

            try {
                switch (choice) {
                    case 1:
                        handleCreatePoll(scanner, pollManager);
                        break;
                    case 2:
                        handleVote(scanner, pollManager);
                        break;
                    case 3:
                        handleViewResults(scanner, pollManager);
                        break;
                    case 4:
                        handleClosePoll(scanner, pollManager);
                        break;
                    case 5:
                        handleListAllPolls(pollManager);
                        break;
                    case 6:
                        running = false;
                        System.out.println("Exiting Secure Polling System. Goodbye!");
                        break;
                    default:
                        System.out.println("Invalid choice. Please select an option between 1 and 6.");
                        break;
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }

        scanner.close();
    }

    private static void handleCreatePoll(Scanner scanner, PollManager pollManager) {
        System.out.print("Question: ");
        String question = scanner.nextLine().trim();

        if (question.isEmpty()) {
            throw new IllegalArgumentException("Poll question cannot be empty.");
        }

        System.out.print("Options (comma separated): ");
        String rawOptions = scanner.nextLine().trim();

        if (rawOptions.isEmpty()) {
            throw new IllegalArgumentException("Options cannot be empty.");
        }

        String[] parts = rawOptions.split(",");
        List<String> options = new ArrayList<>();

        for (String part : parts) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                if (!options.contains(trimmed)) {
                    options.add(trimmed);
                }
            }
        }

        if (options.size() < 2) {
            throw new IllegalArgumentException("A poll must have at least 2 distinct options.");
        }

        Poll poll = pollManager.createPoll(question, options);
        System.out.println("Poll created with ID: " + poll.getPollId());
    }

    private static void handleVote(Scanner scanner, PollManager pollManager) {
        try {
            System.out.print("Poll ID: ");
            String pollIdStr = scanner.nextLine().trim();
            int pollId = Integer.parseInt(pollIdStr);

            System.out.print("Voter ID: ");
            String voterId = scanner.nextLine().trim();

            System.out.print("Option: ");
            String option = scanner.nextLine().trim();

            pollManager.vote(pollId, voterId, option);
            System.out.println("Vote recorded successfully.");
        } catch (NumberFormatException | InputMismatchException e) {
            System.out.println("Error: Poll ID must be a valid number.");
        } catch (DuplicateVoteException | InvalidOptionException | InvalidPollException | IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void handleViewResults(Scanner scanner, PollManager pollManager) {
        try {
            System.out.print("Poll ID: ");
            String pollIdStr = scanner.nextLine().trim();
            int pollId = Integer.parseInt(pollIdStr);

            pollManager.displayResults(pollId);
        } catch (NumberFormatException | InputMismatchException e) {
            System.out.println("Error: Poll ID must be a valid number.");
        } catch (InvalidPollException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void handleClosePoll(Scanner scanner, PollManager pollManager) {
        try {
            System.out.print("Poll ID: ");
            String pollIdStr = scanner.nextLine().trim();
            int pollId = Integer.parseInt(pollIdStr);

            pollManager.closePoll(pollId);
            System.out.println("Poll " + pollId + " has been closed successfully.");
        } catch (NumberFormatException | InputMismatchException e) {
            System.out.println("Error: Poll ID must be a valid number.");
        } catch (InvalidPollException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void handleListAllPolls(PollManager pollManager) {
        pollManager.listAllPolls();
    }
}
