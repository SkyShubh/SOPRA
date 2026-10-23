# Secure Online Polling & Result Analysis System (SOPRA)

A robust, console-based Java application that allows administrators to create and manage polls, enables voters to cast votes securely, enforces duplicate-vote prevention via cryptographic hashing, performs comprehensive result analysis (including tie detection), and persists all data across restarts.

---

## Features

- **Poll Creation:** Create polls with customizable questions and at least two distinct options.
- **Cryptographic Security:** Hashes voter IDs with SHA-256 before storage to protect voter privacy.
- **Duplicate-Vote Prevention:** Enforces one vote per voter per poll using `HashSet` lookups on hashed IDs.
- **Voter ID Normalization:** Case-insensitive and whitespace-tolerant identification (e.g., `"S1001"` and `" s1001 "` count as the exact same voter).
- **Result Analysis:** Calculates per-option vote counts, percentage distribution, and detects outright winners or ties.
- **Option Vote Counts:** Dedicated view for inspecting raw vote tallies per option.
- **Poll Search:** Search across all polls by question keyword (case-insensitive).
- **Poll Lifecycle Management:** Close active polls or delete polls with explicit confirmation (`y/n`).
- **Data Persistence:** Automatically saves all polls, votes, and ID counters to `polls.dat` on exit and reloads them at startup.
- **Corrupted File Resilience:** Gracefully catches corrupt data files, displays a warning, and initializes a clean system without crashing.
- **Clean Input Validation:** Validates every menu choice and data input; completely immune to Scanner newline-skipping bugs.

---

## Concepts Used

| Concept | Implementation Details |
|---|---|
| `HashMap` / `LinkedHashMap` | Preserves poll order by ID in `PollManager`; stores option-to-vote counts inside `Poll` |
| `HashSet` | Stores 64-character SHA-256 voter ID hashes with $O(1)$ duplicate-detection guarantees |
| **Object-Oriented Programming** | High cohesion, strict encapsulation, and clean domain models (`Poll`, `PollManager`, `HashUtil`, `Main`) |
| **Cryptographic Hashing** | Uses `java.security.MessageDigest` (SHA-256) to ensure voter privacy |
| **Persistence & File I/O** | Java Object Serialization (`ObjectOutputStream`, `ObjectInputStream`) saving state to `polls.dat` |
| **Exception Handling** | Custom checked exception hierarchy with try-catch blocks ensuring application stability |

---

## Project Structure

```
SecurePollingSystem/
├── Main.java                      # Console menu loop and input validation
├── Poll.java                      # Poll data model, voting logic, and percentage calculations
├── PollManager.java               # Poll registry, persistence, search, and lifecycle operations
├── HashUtil.java                  # SHA-256 hashing utility with input normalization
├── DataStorageException.java      # Custom exception for persistence read/write failures
├── InvalidPollException.java      # Thrown when a poll does not exist or is closed
├── DuplicateVoteException.java    # Thrown when a voter attempts to vote more than once
├── InvalidOptionException.java    # Thrown when selecting a non-existent poll option
└── SystemVerificationTest.java    # Automated regression test suite for all 9 scenarios
```

---

## Security: How Voter IDs Are Hashed

1. **Input Normalization:** Before hashing, the raw voter ID string is trimmed of all leading/trailing whitespace and converted to lowercase:
   $$\text{normalized} = \text{voterId.trim().toLowerCase()}$$
   This ensures that inputs like `"S1001"`, `"s1001"`, and `" S1001 "` map to the identical canonical representation.
2. **SHA-256 Digest:** The normalized ID is converted to UTF-8 bytes and hashed using `MessageDigest.getInstance("SHA-256")`:
   $$\text{hash} = \text{SHA-256}(\text{normalized})$$
3. **Hexadecimal String:** The 32-byte digest is formatted as a 64-character lowercase hexadecimal string.
4. **Zero Raw ID Retention:** The raw voter ID is discarded immediately after hashing. Only the SHA-256 hash string is stored in the poll's `voters` `HashSet` and written to disk.

---

## Persistence: How Data Is Saved and Loaded

- **Mechanism:** Java Object Serialization via `ObjectOutputStream` and `ObjectInputStream` into `polls.dat`.
- **Why Serialization Was Chosen:**
  1. *Reliability & Structure:* Serialization natively preserves nested collections (`LinkedHashMap<String, Integer>`, `HashSet<String>`) and object references without brittle text parsing or escaping issues.
  2. *Delimiters Safety:* Poll questions and options can contain arbitrary punctuation, commas, or spaces without breaking custom file formats (such as CSV).
  3. *Security:* The binary format stores only the SHA-256 voter hashes, preventing accidental leakage or tampering of voter data.
- **Saved State:**
  - `nextPollId` counter (ensures IDs do not repeat or collide after restarting).
  - All polls with their questions, options, vote tallies, hashed voter ID sets, and open/closed status.
- **Fault-Tolerant Loading:**
  - *First Run / Missing File:* Starts with a clean, empty state without logging an error.
  - *Corrupted File:* Catches `StreamCorruptedException`, `IOException`, and `ClassNotFoundException`, logs a friendly warning, and starts with an empty system without crashing.

---

## Exceptions Reference

| Exception | Extends | Thrown When |
|---|---|---|
| `InvalidPollException` | `Exception` | Poll ID is not found, or an operation is attempted on a closed poll |
| `DuplicateVoteException` | `Exception` | A voter tries to vote more than once in the same poll |
| `InvalidOptionException` | `Exception` | The selected option does not match any valid candidate option |
| `DataStorageException` | `Exception` | An I/O error occurs while writing data to persistent storage |

---

## How to Compile and Run

### Prerequisites
- Java Development Kit (JDK 8 or higher, tested with JDK 25 LTS).

### Compile
```bash
# Navigate to the source folder
cd SecurePollingSystem

# Compile all source files
javac *.java
```

### Run Application
```bash
java Main
```

### Run Automated Verification Test Suite
```bash
java SystemVerificationTest
```

---

## Sample Run

```text
Welcome to the Secure Online Polling & Result Analysis System!

===== Secure Polling System =====
1. Create Poll
2. Vote
3. View Results (Analysis)
4. View Option Vote Counts
5. Close Poll
6. Delete Poll
7. List All Polls
8. Search Polls
9. Exit
Choice: 1
Question: Best programming language?
Options (comma separated): Java, Python, C++
Poll created with ID: 101

===== Secure Polling System =====
1. Create Poll
2. Vote
3. View Results (Analysis)
4. View Option Vote Counts
5. Close Poll
6. Delete Poll
7. List All Polls
8. Search Polls
9. Exit
Choice: 2
Poll ID: 101
Voter ID: S1001
Option: Java
Vote recorded successfully.

===== Secure Polling System =====
1. Create Poll
2. Vote
3. View Results (Analysis)
4. View Option Vote Counts
5. Close Poll
6. Delete Poll
7. List All Polls
8. Search Polls
9. Exit
Choice: 2
Poll ID: 101
Voter ID:  s1001 
Option: Python
Error: Voter 's1001' has already voted in this poll.

===== Secure Polling System =====
1. Create Poll
2. Vote
3. View Results (Analysis)
4. View Option Vote Counts
5. Close Poll
6. Delete Poll
7. List All Polls
8. Search Polls
9. Exit
Choice: 3
Poll ID: 101

Poll 101: Best programming language?   [OPEN]
-----------------------------------------
Java    : 1 votes  (100.0%)
Python  : 0 votes  (0.0%)
C++     : 0 votes  (0.0%)
-----------------------------------------
Total votes: 1
Winner: Java

===== Secure Polling System =====
1. Create Poll
2. Vote
3. View Results (Analysis)
4. View Option Vote Counts
5. Close Poll
6. Delete Poll
7. List All Polls
8. Search Polls
9. Exit
Choice: 4
Poll ID: 101

Option Vote Counts for Poll 101: Best programming language?
-----------------------------------------
Java    : 1 votes
Python  : 0 votes
C++     : 0 votes
-----------------------------------------

===== Secure Polling System =====
1. Create Poll
2. Vote
3. View Results (Analysis)
4. View Option Vote Counts
5. Close Poll
6. Delete Poll
7. List All Polls
8. Search Polls
9. Exit
Choice: 8
Enter search keyword: language

Matching Polls:
------------------------------------------------------------
Poll 101: Best programming language? [OPEN] - Total votes: 1
------------------------------------------------------------

===== Secure Polling System =====
1. Create Poll
2. Vote
3. View Results (Analysis)
4. View Option Vote Counts
5. Close Poll
6. Delete Poll
7. List All Polls
8. Search Polls
9. Exit
Choice: 9
Data saved successfully.
Exiting Secure Polling System. Goodbye!
```
