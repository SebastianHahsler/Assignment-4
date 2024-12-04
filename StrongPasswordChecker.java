import java.util.*;

public class StrongPasswordChecker {

    // Initialize sizes
    private static final int M_SEPARATE_CHAINING = 1000;
    private static final int M_LINEAR_PROBING = 20000;

    // Hash for separate chaining
    private List<HashEntry>[] separateChainingTableEarly = new LinkedList[M_SEPARATE_CHAINING];
    private List<HashEntry>[] separateChainingTableCurrent = new LinkedList[M_SEPARATE_CHAINING];

    // Hash for linear probing
    private HashEntry[] linearProbingTableEarly = new HashEntry[M_LINEAR_PROBING];
    private HashEntry[] linearProbingTableCurrent = new HashEntry[M_LINEAR_PROBING];

    // Represent table entry
    static class HashEntry {
        String word;
        int lineNumber;

        HashEntry(String word, int lineNumber) {
            this.word = word;
            this.lineNumber = lineNumber;
        }
    }

    
    private Map<String, Integer> dictionary;

    // Constructor
    public StrongPasswordChecker(Map<String, Integer> dictionary) {
        this.dictionary = dictionary;
        initializeHashTables();
    }

    // Initialize hash
    private void initializeHashTables() {
        for (Map.Entry<String, Integer> entry : dictionary.entrySet()) {
            String word = entry.getKey();
            int lineNumber = entry.getValue();

            // Insert into separate chaining 
            insertSeparateChaining(word, lineNumber, true);  // Early hash function
            insertSeparateChaining(word, lineNumber, false); // Current hash function

            // Insert into linear probing 
            insertLinearProbing(word, lineNumber, true);     // Early hash function
            insertLinearProbing(word, lineNumber, false);    // Current hash function
        }
    }

    // Insert into separate chaining hash table
    private void insertSeparateChaining(String word, int lineNumber, boolean isEarlyHash) {
        int hash = isEarlyHash ? earlyHashCode(word) % M_SEPARATE_CHAINING
                : currentHashCode(word) % M_SEPARATE_CHAINING;
        hash = (hash + M_SEPARATE_CHAINING) % M_SEPARATE_CHAINING; // Ensure positive index

        HashEntry entry = new HashEntry(word, lineNumber);
        List<HashEntry>[] table = isEarlyHash ? separateChainingTableEarly : separateChainingTableCurrent;

        if (table[hash] == null) {
            table[hash] = new LinkedList<>();
        }
        table[hash].add(entry);
    }

    // Insert into linear probing hash table
    private void insertLinearProbing(String word, int lineNumber, boolean isEarlyHash) {
        int hash = isEarlyHash ? earlyHashCode(word) % M_LINEAR_PROBING
                : currentHashCode(word) % M_LINEAR_PROBING;
        hash = (hash + M_LINEAR_PROBING) % M_LINEAR_PROBING; // Ensure positive index

        HashEntry[] table = isEarlyHash ? linearProbingTableEarly : linearProbingTableCurrent;
        int start = hash;

        while (true) {
            if (table[hash] == null) {
                table[hash] = new HashEntry(word, lineNumber);
                break;
            }
            hash = (hash + 1) % M_LINEAR_PROBING; // Linear probing
            if (hash == start) {
                System.out.println("Linear probing table is full.");
                break;
            }
        }
    }

    // Search costs
    private int costSeparateChainingEarly;
    private int costSeparateChainingCurrent;
    private int costLinearProbingEarly;
    private int costLinearProbingCurrent;

    // Lookup method
    private boolean lookup(String word) {
        // Reset costs
        costSeparateChainingEarly = costSeparateChainingCurrent = 0;
        costLinearProbingEarly = costLinearProbingCurrent = 0;

        boolean found = false;

        // Separate Chaining with early hash function
        int hash = earlyHashCode(word) % M_SEPARATE_CHAINING;
        hash = (hash + M_SEPARATE_CHAINING) % M_SEPARATE_CHAINING;
        List<HashEntry> bucket = separateChainingTableEarly[hash];
        if (bucket != null) {
            for (HashEntry entry : bucket) {
                costSeparateChainingEarly++;
                if (entry.word.equals(word)) {
                    found = true;
                    break;
                }
            }
        }

        // Separate Chaining with current hash function
        hash = currentHashCode(word) % M_SEPARATE_CHAINING;
        hash = (hash + M_SEPARATE_CHAINING) % M_SEPARATE_CHAINING;
        bucket = separateChainingTableCurrent[hash];
        if (bucket != null) {
            for (HashEntry entry : bucket) {
                costSeparateChainingCurrent++;
                if (entry.word.equals(word)) {
                    found = true;
                    break;
                }
            }
        }

        // Linear Probing with early hash function
        hash = earlyHashCode(word) % M_LINEAR_PROBING;
        hash = (hash + M_LINEAR_PROBING) % M_LINEAR_PROBING;
        int start = hash;
        HashEntry[] table = linearProbingTableEarly;
        while (table[hash] != null) {
            costLinearProbingEarly++;
            if (table[hash].word.equals(word)) {
                found = true;
                break;
            }
            hash = (hash + 1) % M_LINEAR_PROBING;
            if (hash == start) break;
        }

        // Linear Probing with current hash function
        hash = currentHashCode(word) % M_LINEAR_PROBING;
        hash = (hash + M_LINEAR_PROBING) % M_LINEAR_PROBING;
        start = hash;
        table = linearProbingTableCurrent;
        while (table[hash] != null) {
            costLinearProbingCurrent++;
            if (table[hash].word.equals(word)) {
                found = true;
                break;
            }
            hash = (hash + 1) % M_LINEAR_PROBING;
            if (hash == start) break;
        }

        return found;
    }

    // Check if password is strong
    public boolean isStrongPassword(String password) {
        // Rule (i): At least 8 characters
        if (password.length() < 8) {
            return false;
        }

        // Rule (ii): Not a dictionary word
        boolean foundPassword = lookup(password);

        // Rule (iii): Not a dictionary word followed by a single digit
        boolean foundBaseWord = false;
        if (password.length() > 1 && Character.isDigit(password.charAt(password.length() - 1))) {
            String baseWord = password.substring(0, password.length() - 1);
            foundBaseWord = lookup(baseWord);
        }

        return !(foundPassword || foundBaseWord);
    }

    // Getters for search costs
    public int getCostSeparateChainingEarly() {
        return costSeparateChainingEarly;
    }

    public int getCostSeparateChainingCurrent() {
        return costSeparateChainingCurrent;
    }

    public int getCostLinearProbingEarly() {
        return costLinearProbingEarly;
    }

    public int getCostLinearProbingCurrent() {
        return costLinearProbingCurrent;
    }

    // Early hash function
    public int earlyHashCode(String s) {
        int hash = 0;
        int skip = Math.max(1, s.length() / 8);
        for (int i = 0; i < s.length(); i += skip) {
            hash = (hash * 37) + s.charAt(i);
        }
        return hash;
    }

    // Current hash function
    public int currentHashCode(String s) {
        int hash = 0;
        for (int i = 0; i < s.length(); i++) {
            hash = (hash * 31) + s.charAt(i);
        }
        return hash;
    }
}
