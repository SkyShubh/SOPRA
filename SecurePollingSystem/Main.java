import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

// Main console menu and user input handler
public class Main {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        PollManager pollManager = new PollManager();

        System.out.println("Welcome to the Secure Online Polling & Result Analysis System!");
        pollManager.loadData();

        boolean running = true;

        while (running) {
            printMenu();
            System.out.print("Choice: ");

            String input = scanner.nextLine().trim();
            int choice;
            try {
                choice = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Error: Please enter a valid numerical choice (1-9).");
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
                        handleViewOptionVotes(scanner, pollManager);
                        break;
                    case 5:
                        handleClosePoll(scanner, pollManager);
                        break;
                    case 6:
                        handleDeletePoll(scanner, pollManager);
                        break;
                    case 7:
                        pollManager.listAllPolls();
                        break;
                    case 8:
                        handleSearchPolls(scanner, pollManager);
                        break;
                    case 9:
                        handleExit(pollManager);
                        running = false;
                        break;
                    default:
                        System.out.println("Invalid choice. Please select an option between 1 and 9.");
                        break;
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }

        scanner.close();
    }

    private static void printMenu() {
        System.out.println("\n===== Secure Polling System =====");
        System.out.println("1. Create Poll");
        System.out.println("2. Vote");
        System.out.println("3. View Results (Analysis)");
        System.out.println("4. View Option Vote Counts");
        System.out.println("5. Close Poll");
        System.out.println("6. Delete Poll");
        System.out.println("7. List All Polls");
        System.out.println("8. Search Polls");
        System.out.println("9. Exit");
    }

    private static void handleCreatePoll(Scanner scanner, PollManager pollManager) {
        System.out.print("Question: ");
        String question = scanner.nextLine().trim();

        if (question.isEmpty()) {
            System.out.println("Error: Poll question cannot be empty.");
            return;
        }

        System.out.print("Options (comma separated): ");
        String rawOptions = scanner.nextLine().trim();

        if (rawOptions.isEmpty()) {
            System.out.println("Error: Options cannot be empty.");
            return;
        }

        String[] parts = rawOptions.split(",");
        List<String> options = new ArrayList<>();

        for (String part : parts) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                boolean exists = false;
                for (String existing : options) {
                    if (existing.equalsIgnoreCase(trimmed)) {
                        exists = true;
                        break;
                    }
                }
                if (!exists) {
                    options.add(trimmed);
                }
            }
        }

        if (options.size() < 2) {
            System.out.println("Error: A poll must have at least 2 distinct options.");
            return;
        }

        Poll poll = pollManager.createPoll(question, options);
        System.out.println("Poll created with ID: " + poll.getPollId());
    }

    private static void handleVote(Scanner scanner, PollManager pollManager) {
        try {
            System.out.print("Poll ID: ");
            int pollId = readPositiveInt(scanner, "Poll ID");

            System.out.print("Voter ID: ");
            String voterId = scanner.nextLine().trim();
            if (voterId.isEmpty()) {
                System.out.println("Error: Voter ID cannot be empty.");
                return;
            }

            System.out.print("Option: ");
            String option = scanner.nextLine().trim();
            if (option.isEmpty()) {
                System.out.println("Error: Option cannot be empty.");
                return;
            }

            pollManager.vote(pollId, voterId, option);
            System.out.println("Vote recorded successfully.");
        } catch (NumberFormatException e) {
            System.out.println("Error: Poll ID must be a valid number.");
        } catch (IllegalArgumentException | DuplicateVoteException | InvalidOptionException | InvalidPollException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void handleViewResults(Scanner scanner, PollManager pollManager) {
        try {
            System.out.print("Poll ID: ");
            int pollId = readPositiveInt(scanner, "Poll ID");
            pollManager.displayResults(pollId);
        } catch (NumberFormatException e) {
            System.out.println("Error: Poll ID must be a valid number.");
        } catch (IllegalArgumentException | InvalidPollException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void handleViewOptionVotes(Scanner scanner, PollManager pollManager) {
        try {
            System.out.print("Poll ID: ");
            int pollId = readPositiveInt(scanner, "Poll ID");
            pollManager.displayOptionVotes(pollId);
        } catch (NumberFormatException e) {
            System.out.println("Error: Poll ID must be a valid number.");
        } catch (IllegalArgumentException | InvalidPollException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void handleClosePoll(Scanner scanner, PollManager pollManager) {
        try {
            System.out.print("Poll ID: ");
            int pollId = readPositiveInt(scanner, "Poll ID");
            pollManager.closePoll(pollId);
            System.out.println("Poll " + pollId + " has been closed successfully.");
        } catch (NumberFormatException e) {
            System.out.println("Error: Poll ID must be a valid number.");
        } catch (IllegalArgumentException | InvalidPollException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void handleDeletePoll(Scanner scanner, PollManager pollManager) {
        try {
            System.out.print("Poll ID to delete: ");
            int pollId = readPositiveInt(scanner, "Poll ID");

            Poll poll = pollManager.getPoll(pollId);
            System.out.print("Are you sure you want to delete poll " + pollId + " (\"" + poll.getQuestion() + "\")? (y/n): ");
            String confirmation = scanner.nextLine().trim();

            if (confirmation.equalsIgnoreCase("y") || confirmation.equalsIgnoreCase("yes")) {
                pollManager.deletePoll(pollId);
                System.out.println("Poll " + pollId + " deleted successfully.");
            } else {
                System.out.println("Deletion cancelled.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Error: Poll ID must be a valid number.");
        } catch (IllegalArgumentException | InvalidPollException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void handleSearchPolls(Scanner scanner, PollManager pollManager) {
        System.out.print("Enter search keyword: ");
        String keyword = scanner.nextLine().trim();

        if (keyword.isEmpty()) {
            System.out.println("Error: Search keyword cannot be empty.");
            return;
        }

        List<Poll> matches = pollManager.searchPolls(keyword);
        if (matches.isEmpty()) {
            System.out.println("No polls found matching keyword: \"" + keyword + "\".");
            return;
        }

        System.out.println("\nMatching Polls:");
        System.out.println("------------------------------------------------------------");
        for (Poll poll : matches) {
            String status = poll.isOpen() ? "OPEN" : "CLOSED";
            System.out.println("Poll " + poll.getPollId() + ": " + poll.getQuestion()
                    + " [" + status + "] - Total votes: " + poll.getTotalVotes());
        }
        System.out.println("------------------------------------------------------------");
    }

    private static void handleExit(PollManager pollManager) {
        try {
            pollManager.saveData();
            System.out.println("Data saved successfully.");
        } catch (DataStorageException e) {
            System.out.println("Warning: Unable to save poll data: " + e.getMessage());
        }
        System.out.println("Exiting Secure Polling System. Goodbye!");
    }

    // Reads and validates a positive whole number
    private static int readPositiveInt(Scanner scanner, String fieldName) {
        int value = Integer.parseInt(scanner.nextLine().trim());
        if (value <= 0) {
            throw new IllegalArgumentException(fieldName + " must be a positive integer.");
        }
        return value;
    }
}
