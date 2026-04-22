package testCases;

import security.SecurityLogger;

import java.util.List;

/*******
 * <p> Title: SecurityLoggingTests </p>
 *
 * <p> Description: A test suite for the SecurityLogger class.  Checks that each
 * logging method makes the right entry in the in-memory log and that
 * edge cases like null usernames, empty strings, and very long values don't make
 * crashes or output that is hard to read.  If a test method passes, it returns null.
 * If it fails, it returns a short error message, which is the same way the other
 * test files do it for this project. </p>
 *
 * <p> At the start of each test, the log is cleared so that entries from a previous
 * test can't get through and give a false result.  We test both the happy path and
 * the failure path of logLoginAttempt, and we hit every public method at least once.
 * The singleton tests at the end show that two calls to getInstance() always return
 * the same object. </p>
 *
 * @author Sudharshan Ramadass
 *
 * @version 1.00	2026-04-15	Initial implementation for TP3
 */
public class SecurityLoggingTests {

    /** Running count of tests that passed. */
    private static int passed = 0;

    /** Running count of tests that failed. */
    private static int failed = 0;

    /*******
     * <p> Method: main(String[] args) </p>
     *
     * <p> Description: Entry point.  Runs every test group in order and prints a
     * summary line with the total pass and fail counts when finished. </p>
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        System.out.println("=== SecurityLogger Test Suite ===\n");

        runLoginAttemptTests();
        runAccessViolationTests();
        runAuthFailureTests();
        runPasswordChangeTests();
        runAccountLifecycleTests();
        runRoleChangeTests();
        runSuspiciousInputTests();
        runBoundaryValueTests();
        runSingletonTests();

        System.out.println("\n=== Results: " + passed + " passed, " + failed + " failed ===");
        System.out.println("=== SecurityLogger Tests Complete ===");
    }


    /*******
     * <p> Method: report(String name, String result) </p>
     *
     * <p> Description: Prints PASS or FAIL for a single test and bumps the
     * appropriate counter.  A null result means the test passed; anything else
     * is treated as an error description. </p>
     *
     * @param name   the test name shown in the output
     * @param result null on PASS, or a short error message on FAIL
     */
    private static void report(String name, String result) {
        if (result == null) {
            passed++;
            System.out.println("  PASS  " + name);
        } else {
            failed++;
            System.out.println("  FAIL  " + name + " - " + result);
        }
    }

    // Clears the log before each test so old entries don't cause false failures
    private static SecurityLogger logger() {
        SecurityLogger sl = SecurityLogger.getInstance();
        sl.clearLog();
        return sl;
    }

    // logLoginAttempt

    private static void runLoginAttemptTests() {
        System.out.println("--- Login Attempt Tests ---");
        report("LT-1: Successful login creates one entry",           lt1_SuccessCreatesEntry());
        report("LT-2: Successful login entry contains tag",          lt2_SuccessContainsTag());
        report("LT-3: Successful login entry contains SUCCESS",      lt3_SuccessContainsSuccess());
        report("LT-4: Failed login entry contains FAILURE",          lt4_FailContainsFailure());
        report("LT-5: Failed login entry contains username",         lt5_FailContainsUsername());
        report("LT-6: Null username recorded as <unknown>",          lt6_NullUsernameHandled());
    }

    /*******
     * <p> LT-1: Calling logLoginAttempt must add exactly one entry to the log. </p>
     * @return {@code null} on PASS, error description on FAIL
     */
    private static String lt1_SuccessCreatesEntry() {
        SecurityLogger sl = logger();
        sl.logLoginAttempt("alice", true);
        List<String> entries = sl.getLogEntries();
        if (entries.size() != 1) return "Expected 1 entry, got " + entries.size();
        return null;
    }

    /*******
     * <p> LT-2: The login entry must contain the LOGIN tag so it can be filtered. </p>
     * @return {@code null} on PASS, error description on FAIL
     */
    private static String lt2_SuccessContainsTag() {
        SecurityLogger sl = logger();
        sl.logLoginAttempt("alice", true);
        String entry = sl.getLogEntries().get(0);
        if (!entry.contains(SecurityLogger.TAG_LOGIN))
            return "Entry missing LOGIN tag: " + entry;
        return null;
    }

    /*******
     * <p> LT-3: A successful login entry must contain the word SUCCESS. </p>
     * @return {@code null} on PASS, error description on FAIL
     */
    private static String lt3_SuccessContainsSuccess() {
        SecurityLogger sl = logger();
        sl.logLoginAttempt("bob", true);
        String entry = sl.getLogEntries().get(0);
        if (!entry.contains("SUCCESS")) return "Entry missing SUCCESS: " + entry;
        return null;
    }

    /*******
     * <p> LT-4: A failed login entry must contain the word FAILURE. </p>
     * @return {@code null} on PASS, error description on FAIL
     */
    private static String lt4_FailContainsFailure() {
        SecurityLogger sl = logger();
        sl.logLoginAttempt("bob", false);
        String entry = sl.getLogEntries().get(0);
        if (!entry.contains("FAILURE")) return "Entry missing FAILURE: " + entry;
        return null;
    }

    /*******
     * <p> LT-5: The login entry must contain the username so events can be traced. </p>
     * @return {@code null} on PASS, error description on FAIL
     */
    private static String lt5_FailContainsUsername() {
        SecurityLogger sl = logger();
        sl.logLoginAttempt("carol", false);
        String entry = sl.getLogEntries().get(0);
        if (!entry.contains("carol")) return "Entry missing username: " + entry;
        return null;
    }

    /*******
     * <p> LT-6 (BVT): A null username must not throw an exception and must be
     * recorded as &lt;unknown&gt;. </p>
     * @return {@code null} on PASS, error description on FAIL
     */
    private static String lt6_NullUsernameHandled() {
        SecurityLogger sl = logger();
        try {
            sl.logLoginAttempt(null, false);
        } catch (Exception e) {
            return "Threw exception on null username: " + e.getMessage();
        }
        String entry = sl.getLogEntries().get(0);
        if (!entry.contains("<unknown>")) return "Null username not shown as <unknown>: " + entry;
        return null;
    }

    // logAccessViolation

    private static void runAccessViolationTests() {
        System.out.println("\n--- Access Violation Tests ---");
        report("AV-1: Entry created with correct tag",       av1_EntryCreated());
        report("AV-2: Entry contains the attempted action",  av2_ContainsAction());
    }

    /*******
     * <p> AV-1: logAccessViolation must produce one entry with the ACCESS_VIOLATION tag. </p>
     * @return {@code null} on PASS, error description on FAIL
     */
    private static String av1_EntryCreated() {
        SecurityLogger sl = logger();
        sl.logAccessViolation("eve", "deleteOtherUserPost");
        List<String> entries = sl.getLogEntries();
        if (entries.size() != 1) return "Expected 1 entry, got " + entries.size();
        if (!entries.get(0).contains(SecurityLogger.TAG_ACCESS))
            return "Missing ACCESS_VIOLATION tag: " + entries.get(0);
        return null;
    }

    /*******
     * <p> AV-2: The access-violation entry must include the attempted action text. </p>
     * @return {@code null} on PASS, error description on FAIL
     */
    private static String av2_ContainsAction() {
        SecurityLogger sl = logger();
        sl.logAccessViolation("eve", "accessAdminPanel");
        String entry = sl.getLogEntries().get(0);
        if (!entry.contains("accessAdminPanel")) return "Entry missing action: " + entry;
        return null;
    }

    // logAuthFailure

    private static void runAuthFailureTests() {
        System.out.println("\n--- Auth Failure Tests ---");
        report("AF-1: Entry created with AUTH_FAILURE tag", af1_EntryCreated());
        report("AF-2: Entry contains the reason",            af2_ContainsReason());
    }

    /*******
     * <p> AF-1: logAuthFailure must produce one entry with the AUTH_FAILURE tag. </p>
     * @return {@code null} on PASS, error description on FAIL
     */
    private static String af1_EntryCreated() {
        SecurityLogger sl = logger();
        sl.logAuthFailure("dave", "expired OTP");
        if (sl.getLogEntries().size() != 1) return "Expected 1 entry";
        if (!sl.getLogEntries().get(0).contains(SecurityLogger.TAG_AUTH_FAIL))
            return "Missing AUTH_FAILURE tag";
        return null;
    }

    /*******
     * <p> AF-2: The auth-failure entry must include the reason text. </p>
     * @return {@code null} on PASS, error description on FAIL
     */
    private static String af2_ContainsReason() {
        SecurityLogger sl = logger();
        sl.logAuthFailure("dave", "expired OTP");
        String entry = sl.getLogEntries().get(0);
        if (!entry.contains("expired OTP")) return "Entry missing reason: " + entry;
        return null;
    }

    // logPasswordChange

    private static void runPasswordChangeTests() {
        System.out.println("\n--- Password Change Tests ---");
        report("PC-1: Entry created with PASSWORD_CHANGE tag", pc1_EntryCreated());
        report("PC-2: Entry contains the username",             pc2_ContainsUsername());
    }

    /*******
     * <p> PC-1: logPasswordChange must produce one entry with the PASSWORD_CHANGE tag. </p>
     * @return {@code null} on PASS, error description on FAIL
     */
    private static String pc1_EntryCreated() {
        SecurityLogger sl = logger();
        sl.logPasswordChange("frank");
        if (sl.getLogEntries().size() != 1) return "Expected 1 entry";
        if (!sl.getLogEntries().get(0).contains(SecurityLogger.TAG_PASSWORD))
            return "Missing PASSWORD_CHANGE tag";
        return null;
    }

    /*******
     * <p> PC-2: The password-change entry must contain the username. </p>
     * @return {@code null} on PASS, error description on FAIL
     */
    private static String pc2_ContainsUsername() {
        SecurityLogger sl = logger();
        sl.logPasswordChange("frank");
        if (!sl.getLogEntries().get(0).contains("frank")) return "Missing username in entry";
        return null;
    }

    // logAccountCreated / logAccountDeleted

    private static void runAccountLifecycleTests() {
        System.out.println("\n--- Account Lifecycle Tests ---");
        report("AC-1: Account created entry has correct tag", ac1_CreatedTag());
        report("AC-2: Account created entry contains role",   ac2_CreatedContainsRole());
        report("AC-3: Account deleted entry has correct tag", ac3_DeletedTag());
        report("AC-4: Account deleted entry contains both usernames", ac4_DeletedBothUsers());
    }

    /*******
     * <p> AC-1: logAccountCreated must produce one entry with ACCOUNT_CREATED tag. </p>
     * @return {@code null} on PASS, error description on FAIL
     */
    private static String ac1_CreatedTag() {
        SecurityLogger sl = logger();
        sl.logAccountCreated("grace", "Role1");
        if (!sl.getLogEntries().get(0).contains(SecurityLogger.TAG_ACCOUNT_CREATE))
            return "Missing ACCOUNT_CREATED tag";
        return null;
    }

    /*******
     * <p> AC-2: The account-created entry must include the assigned role. </p>
     * @return {@code null} on PASS, error description on FAIL
     */
    private static String ac2_CreatedContainsRole() {
        SecurityLogger sl = logger();
        sl.logAccountCreated("grace", "Role1");
        if (!sl.getLogEntries().get(0).contains("Role1")) return "Entry missing role";
        return null;
    }

    /*******
     * <p> AC-3: logAccountDeleted must produce one entry with ACCOUNT_DELETED tag. </p>
     * @return {@code null} on PASS, error description on FAIL
     */
    private static String ac3_DeletedTag() {
        SecurityLogger sl = logger();
        sl.logAccountDeleted("adminUser", "targetUser");
        if (!sl.getLogEntries().get(0).contains(SecurityLogger.TAG_ACCOUNT_DELETE))
            return "Missing ACCOUNT_DELETED tag";
        return null;
    }

    /*******
     * <p> AC-4: The account-deleted entry must contain both the admin and the deleted username. </p>
     * @return {@code null} on PASS, error description on FAIL
     */
    private static String ac4_DeletedBothUsers() {
        SecurityLogger sl = logger();
        sl.logAccountDeleted("adminUser", "targetUser");
        String entry = sl.getLogEntries().get(0);
        if (!entry.contains("adminUser"))  return "Missing admin username: " + entry;
        if (!entry.contains("targetUser")) return "Missing deleted username: " + entry;
        return null;
    }

    // logRoleChange

    private static void runRoleChangeTests() {
        System.out.println("\n--- Role Change Tests ---");
        report("RC-1: Role granted entry has ROLE_CHANGE tag",  rc1_GrantedTag());
        report("RC-2: Role granted entry contains GRANTED",     rc2_GrantedText());
        report("RC-3: Role revoked entry contains REVOKED",     rc3_RevokedText());
        report("RC-4: Entry contains admin and target username", rc4_BothUsernames());
    }

    /*******
     * <p> RC-1: logRoleChange must produce one entry with the ROLE_CHANGE tag. </p>
     * @return {@code null} on PASS, error description on FAIL
     */
    private static String rc1_GrantedTag() {
        SecurityLogger sl = logger();
        sl.logRoleChange("admin1", "user1", "Admin", true);
        if (!sl.getLogEntries().get(0).contains(SecurityLogger.TAG_ROLE_CHANGE))
            return "Missing ROLE_CHANGE tag";
        return null;
    }

    /*******
     * <p> RC-2: A role grant must include the word GRANTED. </p>
     * @return {@code null} on PASS, error description on FAIL
     */
    private static String rc2_GrantedText() {
        SecurityLogger sl = logger();
        sl.logRoleChange("admin1", "user1", "Admin", true);
        if (!sl.getLogEntries().get(0).contains("GRANTED")) return "Missing GRANTED text";
        return null;
    }

    /*******
     * <p> RC-3: A role revocation must include the word REVOKED. </p>
     * @return {@code null} on PASS, error description on FAIL
     */
    private static String rc3_RevokedText() {
        SecurityLogger sl = logger();
        sl.logRoleChange("admin1", "user1", "Role1", false);
        if (!sl.getLogEntries().get(0).contains("REVOKED")) return "Missing REVOKED text";
        return null;
    }

    /*******
     * <p> RC-4: The role-change entry must contain both the admin and the target username. </p>
     * @return {@code null} on PASS, error description on FAIL
     */
    private static String rc4_BothUsernames() {
        SecurityLogger sl = logger();
        sl.logRoleChange("superAdmin", "regularUser", "Role2", true);
        String entry = sl.getLogEntries().get(0);
        if (!entry.contains("superAdmin"))   return "Missing admin: " + entry;
        if (!entry.contains("regularUser"))  return "Missing target: " + entry;
        return null;
    }

    // logSuspiciousInput

    private static void runSuspiciousInputTests() {
        System.out.println("\n--- Suspicious Input Tests ---");
        report("SI-1: Entry has SUSPICIOUS_INPUT tag",            si1_Tag());
        report("SI-2: Entry contains field name",                  si2_FieldName());
        report("SI-3: Long value truncated to max 80 chars",       si3_Truncation());
        report("SI-4: Null value does not throw exception",         si4_NullValue());
    }

    /*******
     * <p> SI-1: logSuspiciousInput must produce an entry with the SUSPICIOUS_INPUT tag. </p>
     * @return {@code null} on PASS, error description on FAIL
     */
    private static String si1_Tag() {
        SecurityLogger sl = logger();
        sl.logSuspiciousInput("attacker", "username", "' OR '1'='1");
        if (!sl.getLogEntries().get(0).contains(SecurityLogger.TAG_SUSPICIOUS))
            return "Missing SUSPICIOUS_INPUT tag";
        return null;
    }

    /*******
     * <p> SI-2: The suspicious-input entry must include the field name. </p>
     * @return {@code null} on PASS, error description on FAIL
     */
    private static String si2_FieldName() {
        SecurityLogger sl = logger();
        sl.logSuspiciousInput("attacker", "postTitle", "<script>alert(1)</script>");
        String entry = sl.getLogEntries().get(0);
        if (!entry.contains("postTitle")) return "Entry missing field name: " + entry;
        return null;
    }

    /*******
     * <p> SI-3 (BVT): A 200-character value must be truncated to 80 characters in the log. </p>
     * @return {@code null} on PASS, error description on FAIL
     */
    private static String si3_Truncation() {
        SecurityLogger sl = logger();
        String longVal = "x".repeat(200);
        sl.logSuspiciousInput("user", "field", longVal);
        String entry = sl.getLogEntries().get(0);
        // The full 200-char value must not appear; the 80-char truncation must be present
        if (entry.contains(longVal)) return "Value was not truncated";
        if (!entry.contains("x".repeat(80))) return "Truncated value not in entry";
        return null;
    }

    /*******
     * <p> SI-4 (BVT): A null suspicious value must be handled gracefully without throwing. </p>
     * @return {@code null} on PASS, error description on FAIL
     */
    private static String si4_NullValue() {
        SecurityLogger sl = logger();
        try {
            sl.logSuspiciousInput("user", "field", null);
        } catch (Exception e) {
            return "Threw exception on null value: " + e.getMessage();
        }
        if (sl.getLogEntries().isEmpty()) return "No entry created";
        return null;
    }

    // Boundary Value Tests

    private static void runBoundaryValueTests() {
        System.out.println("\n--- Boundary Value Tests ---");
        report("BVT-1: clearLog empties the in-memory list",          bvt1_ClearLog());
        report("BVT-2: Multiple events appear in insertion order",    bvt2_OrderPreserved());
        report("BVT-3: Empty string username logged without crash",    bvt3_EmptyUsername());
        report("BVT-4: Null action in logAccessViolation handled",    bvt4_NullAction());
    }

    /*******
     * <p> BVT-1: After calling clearLog(), getLogEntries() must return an empty list. </p>
     * @return {@code null} on PASS, error description on FAIL
     */
    private static String bvt1_ClearLog() {
        SecurityLogger sl = SecurityLogger.getInstance();
        sl.logLoginAttempt("user1", true);
        sl.logLoginAttempt("user2", false);
        sl.clearLog();
        if (!sl.getLogEntries().isEmpty()) return "Log not empty after clearLog()";
        return null;
    }

    /*******
     * <p> BVT-2: When multiple events are logged they must appear in insertion order. </p>
     * @return {@code null} on PASS, error description on FAIL
     */
    private static String bvt2_OrderPreserved() {
        SecurityLogger sl = logger();
        sl.logLoginAttempt("first", true);
        sl.logPasswordChange("second");
        sl.logAccessViolation("third", "action");
        List<String> entries = sl.getLogEntries();
        if (entries.size() != 3) return "Expected 3 entries, got " + entries.size();
        if (!entries.get(0).contains("first"))  return "First entry out of order";
        if (!entries.get(1).contains("second")) return "Second entry out of order";
        if (!entries.get(2).contains("third"))  return "Third entry out of order";
        return null;
    }

    /*******
     * <p> BVT-3 (BVT): An empty-string username must be logged without throwing an exception. </p>
     * @return {@code null} on PASS, error description on FAIL
     */
    private static String bvt3_EmptyUsername() {
        SecurityLogger sl = logger();
        try {
            sl.logLoginAttempt("", false);
        } catch (Exception e) {
            return "Threw exception on empty username: " + e.getMessage();
        }
        if (sl.getLogEntries().isEmpty()) return "No entry created for empty username";
        return null;
    }

    /*******
     * <p> BVT-4 (BVT): A null action in logAccessViolation must be handled without crashing. </p>
     * @return {@code null} on PASS, error description on FAIL
     */
    private static String bvt4_NullAction() {
        SecurityLogger sl = logger();
        try {
            sl.logAccessViolation("user", null);
        } catch (Exception e) {
            return "Threw exception on null action: " + e.getMessage();
        }
        if (sl.getLogEntries().isEmpty()) return "No entry created";
        return null;
    }

    // Singleton Tests

    private static void runSingletonTests() {
        System.out.println("\n--- Singleton Tests ---");
        report("SNG-1: Two getInstance() calls return same object", sng1_SameInstance());
        report("SNG-2: Log entry from one reference visible in other", sng2_SharedState());
    }

    /*******
     * <p> SNG-1: getInstance() must always return the exact same object reference. </p>
     * @return {@code null} on PASS, error description on FAIL
     */
    private static String sng1_SameInstance() {
        SecurityLogger a = SecurityLogger.getInstance();
        SecurityLogger b = SecurityLogger.getInstance();
        if (a != b) return "getInstance() returned different objects";
        return null;
    }

    /*******
     * <p> SNG-2: An entry added via one reference must be visible through a second reference,
     * confirming that both point to the same underlying state. </p>
     * @return {@code null} on PASS, error description on FAIL
     */
    private static String sng2_SharedState() {
        SecurityLogger a = SecurityLogger.getInstance();
        a.clearLog();
        a.logLoginAttempt("sharedUser", true);

        SecurityLogger b = SecurityLogger.getInstance();
        if (b.getLogEntries().isEmpty()) return "Second reference sees empty log — not a singleton";
        if (!b.getLogEntries().get(0).contains("sharedUser"))
            return "Entry from first reference not visible in second";
        return null;
    }
}
