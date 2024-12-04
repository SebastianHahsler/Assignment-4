import java.util.*;
import java.io.*;

public class Main {
    public static void main(String[] args) {
        // Load dictionary
        Map<String, Integer> dictionary = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader("wordlist.10000.txt"))) {
            String line;
            int lineNumber = 1;
            while ((line = br.readLine()) != null) {
                dictionary.put(line.trim(), lineNumber++);
            }
        } catch (IOException e) {
            System.err.println("Error reading dictionary file: " + e.getMessage());
            return;
        }

        // StrongPasswordChecker instance
        StrongPasswordChecker checker = new StrongPasswordChecker(dictionary);

        // Testcases 
        String[] testPasswords = {
                "account8",
                "accountability",
                "9a$D#qW7!uX&Lv3zT",
                "B@k45*W!c$Y7#zR9P",
                "X$8vQ!mW#3Dz&Yr4K5"
        };

        System.out.println("=== Predefined Password Tests ===");
        for (String password : testPasswords) {
            testPassword(password, checker);
        }

        // User loop
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print("Enter a password to check (type 'exit' to quit): ");
            String inputPassword = scanner.nextLine();
            if (inputPassword.equalsIgnoreCase("exit")) {
                System.out.println("Exiting program.");
                break;
            }
            testPassword(inputPassword, checker);
        }

        scanner.close();
    }

    // Test user password and display results
    private static void testPassword(String password, StrongPasswordChecker checker) {
        System.out.println("\nChecking password: " + password);
        boolean isStrong = checker.isStrongPassword(password);
        System.out.println("Password: " + password + " | Is strong: " + isStrong);
        System.out.println("Search costs:");
        System.out.println("  Separate Chaining with earlyHashCode: " + checker.getCostSeparateChainingEarly());
        System.out.println("  Separate Chaining with currentHashCode: " + checker.getCostSeparateChainingCurrent());
        System.out.println("  Linear Probing with earlyHashCode: " + checker.getCostLinearProbingEarly());
        System.out.println("  Linear Probing with currentHashCode: " + checker.getCostLinearProbingCurrent());
    }
}
