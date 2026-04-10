package testCases;

import database.Database;
import entityClasses.User;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Test suite for guiUserLogin.ControllerUserLogin login-path logic.
 *
 * ControllerUserLogin.doLogin() is tightly coupled to JavaFX (reads from
 * static TextField/PasswordField widgets and shows Alert dialogs). Rather
 * than spinning up a full JavaFX toolkit, these tests exercise the exact
 * same Database API calls the controller makes, covering every branch of
 * the doLogin() decision tree:
 *
 *   1. getUserAccountDetails(username) == false  ->  error
 *   2. OTP provided + verifyOtpForCurrentUser()  ->  OTP path
 *   3. OTP empty   + password check              ->  password path
 *   4. Role routing via getNumberOfRoles()
 *
 * This validates that the Database layer the controller depends on behaves
 * correctly for every login outcome, ensuring the controller's branches
 * will produce the right result.
 *
 * Boundary Value Testing is applied to username, password, and OTP inputs.
 */
public class ControllerUserLoginTests {

    private static int passed = 0;
    private static int failed = 0;
    private static List<String> defects = new ArrayList<>();

    public static void main(String[] args) {
        System.out.println("=== ControllerUserLogin Logic-Path Test Suite ===\n");

        runInvalidUsernameTests();
        runPasswordPathTests();
        runOtpPathTests();
        runRoleRoutingTests();
        runBoundaryValueTests();

        System.out.println("\n=== Results: " + passed + " passed, " + failed + " failed ===");

        if (!defects.isEmpty()) {
            System.out.println("\n=== DEFECTS FOUND ===");
            for (int i = 0; i < defects.size(); i++) {
                System.out.println("  D" + (i + 1) + ": " + defects.get(i));
            }
        }
        System.out.println("\n=== ControllerUserLogin Tests Complete ===");
    }

    // ---------------------------------------------------------------
    // Test infrastructure
    // ---------------------------------------------------------------

    private static final String USER_TABLE_DDL =
            "CREATE TABLE IF NOT EXISTS userDB ("
            + "id INT AUTO_INCREMENT PRIMARY KEY, "
            + "userName VARCHAR(255) UNIQUE, "
            + "password VARCHAR(255), "
            + "firstName VARCHAR(255), "
            + "middleName VARCHAR(255), "
            + "lastName VARCHAR(255), "
            + "preferredFirstName VARCHAR(255), "
            + "emailAddress VARCHAR(255), "
            + "adminRole BOOL DEFAULT FALSE, "
            + "newRole1 BOOL DEFAULT FALSE, "
            + "newRole2 BOOL DEFAULT FALSE)";

    private static final String INVITATION_TABLE_DDL =
            "CREATE TABLE IF NOT EXISTS InvitationCodes ("
            + "code VARCHAR(10) PRIMARY KEY, "
            + "emailAddress VARCHAR(255), "
            + "role VARCHAR(10))";

    private static int dbCounter = 0;

    private static Database createTestDatabase() throws Exception {
        Database db = new Database();
        dbCounter++;
        Connection conn = DriverManager.getConnection(
                "jdbc:h2:mem:ctrltest" + dbCounter + ";DB_CLOSE_DELAY=-1", "sa", "");
        Statement stmt = conn.createStatement();

        Field connField = Database.class.getDeclaredField("connection");
        connField.setAccessible(true);
        connField.set(db, conn);

        Field stmtField = Database.class.getDeclaredField("statement");
        stmtField.setAccessible(true);
        stmtField.set(db, stmt);

        stmt.execute(USER_TABLE_DDL);
        stmt.execute(INVITATION_TABLE_DDL);
        return db;
    }

    private static User makeUser(String uname, String pw, boolean admin, boolean r1, boolean r2) {
        return new User(uname, pw, "First", "Mid", "Last", "Pref", uname + "@test.com",
                admin, r1, r2);
    }

    private static void report(String name, String result) {
        if (result == null) {
            passed++;
            System.out.println("  PASS  " + name);
        } else {
            failed++;
            System.out.println("  FAIL  " + name + " - " + result);
        }
    }

    /**
     * Simulates the doLogin() decision flow for given inputs.
     * Returns a String describing the outcome:
     *   "ERROR_USER"      - username not found
     *   "ERROR_OTP"       - OTP verification failed
     *   "ERROR_PASSWORD"  - password mismatch
     *   "LOGIN_ADMIN"     - single admin role login
     *   "LOGIN_ROLE1"     - single role1 login
     *   "LOGIN_ROLE2"     - single role2 login
     *   "LOGIN_MULTI"     - multiple roles dispatch
     *   "NO_ROLE"         - no roles matched
     */
    private static String simulateDoLogin(Database db, String username, String password, String otp) {
        if (!db.getUserAccountDetails(username)) {
            return "ERROR_USER";
        }

        String actualPassword;

        if (otp != null && !otp.trim().isEmpty()) {
            if (!db.verifyOtpForCurrentUser(otp)) {
                return "ERROR_OTP";
            }
            actualPassword = db.getCurrentPassword();
        } else {
            actualPassword = db.getCurrentPassword();
            if (!password.equals(actualPassword)) {
                return "ERROR_PASSWORD";
            }
            actualPassword = password;
        }

        User user = new User(username, actualPassword,
                db.getCurrentFirstName(), db.getCurrentMiddleName(),
                db.getCurrentLastName(), db.getCurrentPreferredFirstName(),
                db.getCurrentEmailAddress(), db.getCurrentAdminRole(),
                db.getCurrentNewRole1(), db.getCurrentNewRole2());

        int numberOfRoles = db.getNumberOfRoles(user);

        if (numberOfRoles == 1) {
            if (user.getAdminRole()) {
                return db.loginAdmin(user) ? "LOGIN_ADMIN" : "NO_ROLE";
            } else if (user.getNewRole1()) {
                return db.loginRole1(user) ? "LOGIN_ROLE1" : "NO_ROLE";
            } else if (user.getNewRole2()) {
                return db.loginRole2(user) ? "LOGIN_ROLE2" : "NO_ROLE";
            }
            return "NO_ROLE";
        } else if (numberOfRoles > 1) {
            return "LOGIN_MULTI";
        }
        return "NO_ROLE";
    }

    // ---------------------------------------------------------------
    // Path 1: Invalid username
    // ---------------------------------------------------------------

    private static void runInvalidUsernameTests() {
        System.out.println("--- Path: Invalid Username ---");
        report("Test 1: Non-existent username -> error", test1_NonexistentUser());
        report("Test 2: Empty username -> error", test2_EmptyUsername());
        report("Test 3: Whitespace-only username -> error", test3_WhitespaceUsername());
    }

    private static String test1_NonexistentUser() {
        try {
            Database db = createTestDatabase();
            String result = simulateDoLogin(db, "noSuchUser", "any", "");
            db.closeConnection();
            if (!"ERROR_USER".equals(result)) return "Expected ERROR_USER, got " + result;
            return null;
        } catch (Exception e) { return "Exception: " + e.getMessage(); }
    }

    private static String test2_EmptyUsername() {
        try {
            Database db = createTestDatabase();
            String result = simulateDoLogin(db, "", "any", "");
            db.closeConnection();
            if (!"ERROR_USER".equals(result)) return "Expected ERROR_USER, got " + result;
            return null;
        } catch (Exception e) { return "Exception: " + e.getMessage(); }
    }

    private static String test3_WhitespaceUsername() {
        try {
            Database db = createTestDatabase();
            String result = simulateDoLogin(db, "   ", "any", "");
            db.closeConnection();
            if (!"ERROR_USER".equals(result)) return "Expected ERROR_USER, got " + result;
            return null;
        } catch (Exception e) { return "Exception: " + e.getMessage(); }
    }

    // ---------------------------------------------------------------
    // Path 2 & 3: Password path
    // ---------------------------------------------------------------

    private static void runPasswordPathTests() {
        System.out.println("\n--- Path: Password Authentication ---");
        report("Test 4: Correct password -> login succeeds", test4_CorrectPassword());
        report("Test 5: Wrong password -> error", test5_WrongPassword());
        report("Test 6: Empty password -> error", test6_EmptyPassword());
    }

    private static String test4_CorrectPassword() {
        try {
            Database db = createTestDatabase();
            db.register(makeUser("alice", "secret", true, false, false));
            String result = simulateDoLogin(db, "alice", "secret", "");
            db.closeConnection();
            if (!"LOGIN_ADMIN".equals(result)) return "Expected LOGIN_ADMIN, got " + result;
            return null;
        } catch (Exception e) { return "Exception: " + e.getMessage(); }
    }

    private static String test5_WrongPassword() {
        try {
            Database db = createTestDatabase();
            db.register(makeUser("bob", "correctPw", true, false, false));
            String result = simulateDoLogin(db, "bob", "wrongPw", "");
            db.closeConnection();
            if (!"ERROR_PASSWORD".equals(result)) return "Expected ERROR_PASSWORD, got " + result;
            return null;
        } catch (Exception e) { return "Exception: " + e.getMessage(); }
    }

    private static String test6_EmptyPassword() {
        try {
            Database db = createTestDatabase();
            db.register(makeUser("carol", "realPw", true, false, false));
            String result = simulateDoLogin(db, "carol", "", "");
            db.closeConnection();
            if (!"ERROR_PASSWORD".equals(result)) return "Expected ERROR_PASSWORD, got " + result;
            return null;
        } catch (Exception e) { return "Exception: " + e.getMessage(); }
    }

    // ---------------------------------------------------------------
    // Path 4, 5, 6: OTP path
    // ---------------------------------------------------------------

    private static void runOtpPathTests() {
        System.out.println("\n--- Path: OTP Authentication ---");
        report("Test 7: Valid OTP -> login succeeds", test7_ValidOtp());
        report("Test 8: Invalid OTP -> error", test8_InvalidOtp());
        report("Test 9: Expired OTP -> error", test9_ExpiredOtp());
        report("Test 10 (BVT): Whitespace-only OTP uses password path", test10_WhitespaceOtp());
    }

    private static String test7_ValidOtp() {
        try {
            Database db = createTestDatabase();
            db.register(makeUser("otpUser", "pw", false, true, false));
            db.getUserAccountDetails("otpUser");
            String otp = db.createOTP(5);
            String result = simulateDoLogin(db, "otpUser", "ignored", otp);
            db.closeConnection();
            if (!"LOGIN_ROLE1".equals(result)) return "Expected LOGIN_ROLE1, got " + result;
            return null;
        } catch (Exception e) { return "Exception: " + e.getMessage(); }
    }

    private static String test8_InvalidOtp() {
        try {
            Database db = createTestDatabase();
            db.register(makeUser("otpUser2", "pw", false, true, false));
            db.getUserAccountDetails("otpUser2");
            db.createOTP(5);
            String result = simulateDoLogin(db, "otpUser2", "pw", "999999");
            db.closeConnection();
            if (!"ERROR_OTP".equals(result)) return "Expected ERROR_OTP, got " + result;
            return null;
        } catch (Exception e) { return "Exception: " + e.getMessage(); }
    }

    private static String test9_ExpiredOtp() {
        try {
            Database db = createTestDatabase();
            db.register(makeUser("otpUser3", "pw", false, true, false));
            db.getUserAccountDetails("otpUser3");
            String otp = db.createOTP(0);
            Thread.sleep(50);
            String result = simulateDoLogin(db, "otpUser3", "pw", otp);
            db.closeConnection();
            if (!"ERROR_OTP".equals(result)) return "Expected ERROR_OTP, got " + result;
            return null;
        } catch (Exception e) { return "Exception: " + e.getMessage(); }
    }

    private static String test10_WhitespaceOtp() {
        try {
            Database db = createTestDatabase();
            db.register(makeUser("wsOtp", "pw", true, false, false));
            String result = simulateDoLogin(db, "wsOtp", "pw", "   ");
            db.closeConnection();
            if (!"LOGIN_ADMIN".equals(result))
                return "Whitespace OTP should fall through to password path, got " + result;
            return null;
        } catch (Exception e) { return "Exception: " + e.getMessage(); }
    }

    // ---------------------------------------------------------------
    // Role routing
    // ---------------------------------------------------------------

    private static void runRoleRoutingTests() {
        System.out.println("\n--- Path: Role Routing ---");
        report("Test 11: Single admin role routes to admin", test11_SingleAdminRoute());
        report("Test 12: Single role1 routes to role1", test12_SingleRole1Route());
        report("Test 13: Single role2 routes to role2", test13_SingleRole2Route());
        report("Test 14: Multiple roles routes to dispatch", test14_MultiRoleRoute());
        report("Test 15: No roles -> no dispatch", test15_NoRoles());
    }

    private static String test11_SingleAdminRoute() {
        try {
            Database db = createTestDatabase();
            db.register(makeUser("sAdmin", "pw", true, false, false));
            String result = simulateDoLogin(db, "sAdmin", "pw", "");
            db.closeConnection();
            if (!"LOGIN_ADMIN".equals(result)) return "Expected LOGIN_ADMIN, got " + result;
            return null;
        } catch (Exception e) { return "Exception: " + e.getMessage(); }
    }

    private static String test12_SingleRole1Route() {
        try {
            Database db = createTestDatabase();
            db.register(makeUser("sRole1", "pw", false, true, false));
            String result = simulateDoLogin(db, "sRole1", "pw", "");
            db.closeConnection();
            if (!"LOGIN_ROLE1".equals(result)) return "Expected LOGIN_ROLE1, got " + result;
            return null;
        } catch (Exception e) { return "Exception: " + e.getMessage(); }
    }

    private static String test13_SingleRole2Route() {
        try {
            Database db = createTestDatabase();
            db.register(makeUser("sRole2", "pw", false, false, true));
            String result = simulateDoLogin(db, "sRole2", "pw", "");
            db.closeConnection();
            if (!"LOGIN_ROLE2".equals(result)) return "Expected LOGIN_ROLE2, got " + result;
            return null;
        } catch (Exception e) { return "Exception: " + e.getMessage(); }
    }

    private static String test14_MultiRoleRoute() {
        try {
            Database db = createTestDatabase();
            db.register(makeUser("multi", "pw", true, true, false));
            String result = simulateDoLogin(db, "multi", "pw", "");
            db.closeConnection();
            if (!"LOGIN_MULTI".equals(result)) return "Expected LOGIN_MULTI, got " + result;
            return null;
        } catch (Exception e) { return "Exception: " + e.getMessage(); }
    }

    private static String test15_NoRoles() {
        try {
            Database db = createTestDatabase();
            db.register(makeUser("noRole", "pw", false, false, false));
            String result = simulateDoLogin(db, "noRole", "pw", "");
            db.closeConnection();
            if (!"NO_ROLE".equals(result)) return "Expected NO_ROLE, got " + result;
            return null;
        } catch (Exception e) { return "Exception: " + e.getMessage(); }
    }

    // ---------------------------------------------------------------
    // Boundary Value Tests
    // ---------------------------------------------------------------

    private static void runBoundaryValueTests() {
        System.out.println("\n--- Boundary Value Tests ---");
        report("Test 16 (BVT): Single-char username login", test16_SingleCharLogin());
        report("Test 17 (BVT): Long username login", test17_LongUsernameLogin());
        report("Test 18 (BVT): Null OTP treated as password path", test18_NullOtpFallback());
    }

    private static String test16_SingleCharLogin() {
        try {
            Database db = createTestDatabase();
            db.register(makeUser("A", "p", true, false, false));
            String result = simulateDoLogin(db, "A", "p", "");
            db.closeConnection();
            if (!"LOGIN_ADMIN".equals(result)) return "Expected LOGIN_ADMIN, got " + result;
            return null;
        } catch (Exception e) { return "Exception: " + e.getMessage(); }
    }

    private static String test17_LongUsernameLogin() {
        try {
            Database db = createTestDatabase();
            String longName = "u".repeat(200);
            db.register(makeUser(longName, "pw", false, true, false));
            String result = simulateDoLogin(db, longName, "pw", "");
            db.closeConnection();
            if (!"LOGIN_ROLE1".equals(result)) return "Expected LOGIN_ROLE1, got " + result;
            return null;
        } catch (Exception e) { return "Exception: " + e.getMessage(); }
    }

    private static String test18_NullOtpFallback() {
        try {
            Database db = createTestDatabase();
            db.register(makeUser("nullOtp", "pw", true, false, false));
            String result = simulateDoLogin(db, "nullOtp", "pw", null);
            db.closeConnection();
            if (!"LOGIN_ADMIN".equals(result))
                return "Null OTP should fall through to password path, got " + result;
            return null;
        } catch (Exception e) { return "Exception: " + e.getMessage(); }
    }
}
