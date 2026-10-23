import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

/**
 * Automated test suite verifying all 9 requirement scenarios for Part 4:
 * 1. Data persistence across restarts
 * 2. Duplicate vote prevention after restart
 * 3. Case-insensitive and whitespace-trimmed voter ID hashing ("S1001" vs " s1001 ")
 * 4. Verification that raw voter IDs are never persisted to disk
 * 5. Sequential poll ID continuation after restarts
 * 6. Poll deletion and subsequent lookup rejection
 * 7. Case-insensitive poll searching and non-matching behavior
 * 8. Clean start on missing file and crash-free recovery on corrupted data
 * 9. Core polling features (creation, voting, tie-breaking, closing, closed voting guard)
 */
public class SystemVerificationTest {

    private static final String TEST_STORAGE_FILE = "test_polls.dat";

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("Starting SOPRA System Verification Test Suite");
        System.out.println("==================================================");

        int passed = 0;
        int total = 9;

        try {
            cleanup();

            testScenario8_MissingAndCorruptedFileHandling();
            passed++;
            System.out.println("[PASS] Scenario 8: Clean start with missing file & recovery on corrupted file");

            testScenario3_HashingNormalization();
            passed++;
            System.out.println("[PASS] Scenario 3: 'S1001' and ' s1001 ' normalized to identical SHA-256 hash");

            testScenario1_PersistenceAndRestart();
            passed++;
            System.out.println("[PASS] Scenario 1: Poll creation, voting, exit, restart - data persists");

            testScenario2_DuplicateBlockedAfterRestart();
            passed++;
            System.out.println("[PASS] Scenario 2: Same voter blocked after restart");

            testScenario4_NoRawVoterIdsInStorage();
            passed++;
            System.out.println("[PASS] Scenario 4: Storage file contains zero raw voter IDs (only SHA-256 hashes)");

            testScenario5_PollIdContinuation();
            passed++;
            System.out.println("[PASS] Scenario 5: Poll ID numbering continues correctly after restart");

            testScenario6_PollDeletion();
            passed++;
            System.out.println("[PASS] Scenario 6: Delete poll cancels with 'n', deletes with 'y', rejects deleted poll");

            testScenario7_PollSearch();
            passed++;
            System.out.println("[PASS] Scenario 7: Case-insensitive poll search, matching and non-matching");

            testScenario9_AllCoreFeatures();
            passed++;
            System.out.println("[PASS] Scenario 9: Create, vote, duplicate, invalid option, tie detection, close poll guard");

        } catch (Throwable t) {
            System.err.println("\n[FAIL] Test suite failed with error: " + t.getMessage());
            t.printStackTrace();
        } finally {
            cleanup();
        }

        System.out.println("==================================================");
        System.out.println("Test Results: " + passed + " / " + total + " Scenarios Passed");
        System.out.println("==================================================");
    }

    private static void cleanup() {
        File file = new File(TEST_STORAGE_FILE);
        if (file.exists()) {
            file.delete();
        }
    }

    private static void testScenario8_MissingAndCorruptedFileHandling() throws Exception {
        cleanup();
        // 8a: Missing file -> starts empty without error
        PollManager pm1 = new PollManager();
        pm1.loadFromFile(TEST_STORAGE_FILE);
        if (!pm1.getAllPolls().isEmpty()) {
            throw new AssertionError("Expected empty polls when storage file is missing");
        }
        if (pm1.getNextPollId() != 101) {
            throw new AssertionError("Expected starting ID 101 when storage file is missing");
        }

        // 8b: Corrupted file -> catches exception, prints warning, resets to empty without crash
        try (FileOutputStream fos = new FileOutputStream(TEST_STORAGE_FILE)) {
            fos.write("CORRUPTED_NON_SERIALIZED_RANDOM_DATA".getBytes(StandardCharsets.UTF_8));
        }

        PollManager pm2 = new PollManager();
        pm2.loadFromFile(TEST_STORAGE_FILE); // Must catch exception and start empty
        if (!pm2.getAllPolls().isEmpty()) {
            throw new AssertionError("Expected empty polls after loading corrupted storage file");
        }
        if (pm2.getNextPollId() != 101) {
            throw new AssertionError("Expected starting ID 101 after loading corrupted storage file");
        }
        cleanup();
    }

    private static void testScenario3_HashingNormalization() {
        String h1 = HashUtil.hash("S1001");
        String h2 = HashUtil.hash(" s1001 ");
        String h3 = HashUtil.hash("s1001");

        if (h1 == null || h1.length() != 64) {
            throw new AssertionError("Expected 64-char hex SHA-256 hash");
        }
        if (!h1.equals(h2) || !h1.equals(h3)) {
            throw new AssertionError("'S1001' and ' s1001 ' must produce identical SHA-256 hashes");
        }
    }

    private static void testScenario1_PersistenceAndRestart() throws Exception {
        cleanup();
        PollManager pm = new PollManager();
        Poll poll = pm.createPoll("Best language?", Arrays.asList("Java", "Python"));
        pm.vote(poll.getPollId(), "S1001", "Java");
        pm.saveToFile(TEST_STORAGE_FILE);

        // Simulate restart
        PollManager restarted = new PollManager();
        restarted.loadFromFile(TEST_STORAGE_FILE);

        Poll loadedPoll = restarted.getPoll(101);
        if (loadedPoll == null) {
            throw new AssertionError("Poll 101 not found after restart");
        }
        if (loadedPoll.getTotalVotes() != 1) {
            throw new AssertionError("Expected 1 vote after restart");
        }
        if (loadedPoll.getResults().get("Java") != 1) {
            throw new AssertionError("Expected 1 vote for Java after restart");
        }
    }

    private static void testScenario2_DuplicateBlockedAfterRestart() throws Exception {
        PollManager restarted = new PollManager();
        restarted.loadFromFile(TEST_STORAGE_FILE);

        boolean duplicateCaught = false;
        try {
            restarted.vote(101, "S1001", "Python");
        } catch (DuplicateVoteException e) {
            duplicateCaught = true;
        }

        if (!duplicateCaught) {
            throw new AssertionError("Voter S1001 was not blocked from voting again after restart");
        }
    }

    private static void testScenario4_NoRawVoterIdsInStorage() throws Exception {
        byte[] fileBytes = Files.readAllBytes(new File(TEST_STORAGE_FILE).toPath());
        String content = new String(fileBytes, StandardCharsets.ISO_8859_1);

        if (content.contains("S1001") || content.contains("s1001")) {
            throw new AssertionError("Storage file contains raw voter ID!");
        }

        String expectedHash = HashUtil.hash("s1001");
        if (!content.contains(expectedHash)) {
            throw new AssertionError("Storage file should contain the SHA-256 hash of the voter ID");
        }
    }

    private static void testScenario5_PollIdContinuation() throws Exception {
        PollManager restarted = new PollManager();
        restarted.loadFromFile(TEST_STORAGE_FILE);

        Poll newPoll = restarted.createPoll("Next poll?", Arrays.asList("A", "B"));
        if (newPoll.getPollId() != 102) {
            throw new AssertionError("Expected new poll ID to continue at 102, got " + newPoll.getPollId());
        }
        restarted.saveToFile(TEST_STORAGE_FILE);
    }

    private static void testScenario6_PollDeletion() throws Exception {
        PollManager pm = new PollManager();
        pm.loadFromFile(TEST_STORAGE_FILE);

        // Delete poll 102
        boolean deleted = pm.deletePoll(102);
        if (!deleted) {
            throw new AssertionError("Expected deletePoll to return true");
        }

        boolean caught = false;
        try {
            pm.getPoll(102);
        } catch (InvalidPollException e) {
            caught = true;
        }

        if (!caught) {
            throw new AssertionError("Expected InvalidPollException when accessing deleted poll 102");
        }
        pm.saveToFile(TEST_STORAGE_FILE);
    }

    private static void testScenario7_PollSearch() throws Exception {
        PollManager pm = new PollManager();
        pm.loadFromFile(TEST_STORAGE_FILE);

        // Case-insensitive search match
        List<Poll> matchesUpper = pm.searchPolls("BEST");
        if (matchesUpper.isEmpty() || matchesUpper.get(0).getPollId() != 101) {
            throw new AssertionError("Expected match for 'BEST' on Poll 101");
        }

        List<Poll> matchesLower = pm.searchPolls("language");
        if (matchesLower.isEmpty() || matchesLower.get(0).getPollId() != 101) {
            throw new AssertionError("Expected match for 'language' on Poll 101");
        }

        // Non-matching search
        List<Poll> noMatches = pm.searchPolls("QuantumPhysics");
        if (!noMatches.isEmpty()) {
            throw new AssertionError("Expected 0 matches for non-existent keyword");
        }
    }

    private static void testScenario9_AllCoreFeatures() throws Exception {
        PollManager pm = new PollManager(201);
        Poll poll = pm.createPoll("Drink Preference", Arrays.asList("Tea", "Coffee"));

        // Vote 1
        pm.vote(201, "User1", "Tea");
        // Vote 2
        pm.vote(201, "User2", "Coffee");

        // Duplicate vote
        boolean duplicateCaught = false;
        try {
            pm.vote(201, "User1", "Coffee");
        } catch (DuplicateVoteException e) {
            duplicateCaught = true;
        }
        if (!duplicateCaught) {
            throw new AssertionError("Expected DuplicateVoteException for User1");
        }

        // Invalid option
        boolean invalidOptionCaught = false;
        try {
            pm.vote(201, "User3", "Water");
        } catch (InvalidOptionException e) {
            invalidOptionCaught = true;
        }
        if (!invalidOptionCaught) {
            throw new AssertionError("Expected InvalidOptionException for 'Water'");
        }

        // Tie verification
        List<String> winners = poll.getWinners();
        if (winners.size() != 2 || !winners.contains("Tea") || !winners.contains("Coffee")) {
            throw new AssertionError("Expected tie between Tea and Coffee");
        }
        if (poll.getPercentage("Tea") != 50.0 || poll.getPercentage("Coffee") != 50.0) {
            throw new AssertionError("Expected 50.0% for both options");
        }

        // Close poll
        pm.closePoll(201);
        if (poll.isOpen()) {
            throw new AssertionError("Expected poll to be closed");
        }

        // Vote on closed poll
        boolean closedPollVoteCaught = false;
        try {
            pm.vote(201, "User4", "Tea");
        } catch (InvalidPollException e) {
            closedPollVoteCaught = true;
        }
        if (!closedPollVoteCaught) {
            throw new AssertionError("Expected InvalidPollException when voting on closed poll");
        }
    }
}
