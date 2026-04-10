package testCases;

import database.Database;
import entityClasses.User;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Comprehensive test suite for database.Database.
 *
 * Uses reflection to inject an in-memory H2 connection so the real
 * file-based database is never touched. Each test method returns null
 * on PASS or an error description on FAIL, matching the existing
 * TestCases.java convention.
 *
 * Categories:
 *   - Connection / Schema
 *   - Registration and Lookup
 *   - getUserAccountDetails + BVT
 *   - Authentication (login methods)
 *   - OTP + BVT
 *   - Roles
 *   - Profile Getters / Updaters
 *   - Invitation Codes
 *   - Delete and Resilience
 *
 * Traceability: Each test name includes the requirement tag it covers.
 */
public class DatabaseTests {

    private static int passed = 0;
    private static int failed = 0;
    private static List<String> defects = new ArrayList<>();

    public static void main(String[] args) {
        System.out.println("=== Database.java Test Suite ===\n");

        runConnectionSchemaTests();
        runRegistrationLookupTests();
        runGetUserAccountDetailsTests();
        runAuthenticationTests();
        runOtpTests();
        runRoleTests();
        runProfileTests();
        runInvitationTests();
        runDeleteResilienceTests();

        System.out.println("\n=== Results: " + passed + " passed, " + failed + " failed ===");

        if (!defects.isEmpty()) {
            System.out.println("\n=== DEFECTS FOUND ===");
            for (int i = 0; i < defects.size(); i++) {
                System.out.println("  D" + (i + 1) + ": " + defects.get(i));
            }
        }
        System.out.println("\n=== Database Tests Complete ===");
    }

    // ---------------------------------------------------------------
    // Test infrastructure: create a Database with in-memory H2
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
                "jdbc:h2:mem:testdb" + dbCounter + ";DB_CLOSE_DELAY=-1", "sa", "");
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

    // ---------------------------------------------------------------
    // Connection / Schema
    // ---------------------------------------------------------------

    private static void runConnectionSchemaTests() {
        System.out.println("--- Connection / Schema ---");
        report("Test 1: Fresh DB is empty", test1_FreshDbIsEmpty());
        report("Test 2: Fresh DB has zero users", test2_FreshDbZeroUsers());
    }

    private static String test1_FreshDbIsEmpty() {
        try {
            Database db = createTestDatabase();
            if (!db.isDatabaseEmpty()) return "isDatabaseEmpty() should return true on fresh DB";
            db.closeConnection();
            return null;
        } catch (Exception e) { return "Exception: " + e.getMessage(); }
    }

    private static String test2_FreshDbZeroUsers() {
        try {
            Database db = createTestDatabase();
            if (db.getNumberOfUsers() != 0) return "getNumberOfUsers() should be 0, got " + db.getNumberOfUsers();
            db.closeConnection();
            return null;
        } catch (Exception e) { return "Exception: " + e.getMessage(); }
    }

    // ---------------------------------------------------------------
    // Registration and Lookup
    // ---------------------------------------------------------------

    private static void runRegistrationLookupTests() {
        System.out.println("\n--- Registration and Lookup ---");
        report("Test 3: Register user, doesUserExist returns true", test3_RegisterUserExists());
        report("Test 4: doesUserExist returns false for unknown", test4_UnknownUserDoesNotExist());
        report("Test 5: getNumberOfUsers after registration", test5_UserCountAfterRegister());
        report("Test 6: DB not empty after registration", test6_DbNotEmptyAfterRegister());
        report("Test 7: Duplicate username throws SQLException", test7_DuplicateUsernameThrows());
    }

    private static String test3_RegisterUserExists() {
        try {
            Database db = createTestDatabase();
            db.register(makeUser("alice", "pass123", true, false, false));
            if (!db.doesUserExist("alice")) return "doesUserExist should be true after register";
            db.closeConnection();
            return null;
        } catch (Exception e) { return "Exception: " + e.getMessage(); }
    }

    private static String test4_UnknownUserDoesNotExist() {
        try {
            Database db = createTestDatabase();
            if (db.doesUserExist("noSuchUser")) return "doesUserExist should be false for unknown";
            db.closeConnection();
            return null;
        } catch (Exception e) { return "Exception: " + e.getMessage(); }
    }

    private static String test5_UserCountAfterRegister() {
        try {
            Database db = createTestDatabase();
            db.register(makeUser("u1", "p", true, false, false));
            db.register(makeUser("u2", "p", false, true, false));
            if (db.getNumberOfUsers() != 2) return "Expected 2, got " + db.getNumberOfUsers();
            db.closeConnection();
            return null;
        } catch (Exception e) { return "Exception: " + e.getMessage(); }
    }

    private static String test6_DbNotEmptyAfterRegister() {
        try {
            Database db = createTestDatabase();
            db.register(makeUser("bob", "pw", true, false, false));
            if (db.isDatabaseEmpty()) return "isDatabaseEmpty should be false after register";
            db.closeConnection();
            return null;
        } catch (Exception e) { return "Exception: " + e.getMessage(); }
    }

    private static String test7_DuplicateUsernameThrows() {
        try {
            Database db = createTestDatabase();
            db.register(makeUser("dup", "pw1", true, false, false));
            try {
                db.register(makeUser("dup", "pw2", false, true, false));
                return "Expected SQLException for duplicate username";
            } catch (SQLException e) {
                // expected
            }
            db.closeConnection();
            return null;
        } catch (Exception e) { return "Exception: " + e.getMessage(); }
    }

    // ---------------------------------------------------------------
    // getUserAccountDetails + BVT
    // ---------------------------------------------------------------

    private static void runGetUserAccountDetailsTests() {
        System.out.println("\n--- getUserAccountDetails + BVT ---");
        report("Test 8: Valid username populates fields", test8_ValidUsernameDetails());
        report("Test 9: Non-existent username returns false", test9_NonexistentDetails());
        report("Test 10 (BVT): Empty string username", test10_EmptyStringUsername());
        report("Test 11 (BVT): Whitespace-only username", test11_WhitespaceUsername());
        report("Test 12 (BVT): Single-char username works", test12_SingleCharUsername());
    }

    private static String test8_ValidUsernameDetails() {
        try {
            Database db = createTestDatabase();
            db.register(new User("carol", "secret", "Carol", "M", "Smith",
                    "CarolPref", "carol@x.com", true, true, false));
            if (!db.getUserAccountDetails("carol")) return "getUserAccountDetails should return true";
            if (!"carol".equals(db.getCurrentUsername())) return "Username mismatch";
            if (!"secret".equals(db.getCurrentPassword())) return "Password mismatch";
            if (!"Carol".equals(db.getCurrentFirstName())) return "FirstName mismatch";
            if (!"M".equals(db.getCurrentMiddleName())) return "MiddleName mismatch";
            if (!"Smith".equals(db.getCurrentLastName())) return "LastName mismatch";
            if (!"CarolPref".equals(db.getCurrentPreferredFirstName())) return "PrefFirstName mismatch";
            if (!"carol@x.com".equals(db.getCurrentEmailAddress())) return "Email mismatch";
            if (!db.getCurrentAdminRole()) return "Admin role should be true";
            if (!db.getCurrentNewRole1()) return "Role1 should be true";
            if (db.getCurrentNewRole2()) return "Role2 should be false";
            db.closeConnection();
            return null;
        } catch (Exception e) { return "Exception: " + e.getMessage(); }
    }

    private static String test9_NonexistentDetails() {
        try {
            Database db = createTestDatabase();
            if (db.getUserAccountDetails("ghost")) return "Should return false for non-existent";
            db.closeConnection();
            return null;
        } catch (Exception e) { return "Exception: " + e.getMessage(); }
    }

    private static String test10_EmptyStringUsername() {
        try {
            Database db = createTestDatabase();
            if (db.getUserAccountDetails("")) return "Should return false for empty string";
            db.closeConnection();
            return null;
        } catch (Exception e) { return "Exception: " + e.getMessage(); }
    }

    private static String test11_WhitespaceUsername() {
        try {
            Database db = createTestDatabase();
            if (db.getUserAccountDetails("   ")) return "Should return false for whitespace";
            db.closeConnection();
            return null;
        } catch (Exception e) { return "Exception: " + e.getMessage(); }
    }

    private static String test12_SingleCharUsername() {
        try {
            Database db = createTestDatabase();
            db.register(makeUser("X", "pw", false, true, false));
            if (!db.doesUserExist("X")) return "Single-char user should exist";
            if (!db.getUserAccountDetails("X")) return "Details should load for single-char user";
            if (!"X".equals(db.getCurrentUsername())) return "Username should be X";
            db.closeConnection();
            return null;
        } catch (Exception e) { return "Exception: " + e.getMessage(); }
    }

    // ---------------------------------------------------------------
    // Authentication
    // ---------------------------------------------------------------

    private static void runAuthenticationTests() {
        System.out.println("\n--- Authentication ---");
        report("Test 13: loginAdmin valid credentials", test13_LoginAdminValid());
        report("Test 14: loginAdmin wrong password", test14_LoginAdminWrongPw());
        report("Test 15: loginRole1 and loginRole2", test15_LoginRoles());
        report("Test 16: getCurrentPassword matches registered", test16_CurrentPasswordMatch());
    }

    private static String test13_LoginAdminValid() {
        try {
            Database db = createTestDatabase();
            User u = makeUser("admin1", "adminPw", true, false, false);
            db.register(u);
            if (!db.loginAdmin(u)) return "loginAdmin should return true for valid admin";
            db.closeConnection();
            return null;
        } catch (Exception e) { return "Exception: " + e.getMessage(); }
    }

    private static String test14_LoginAdminWrongPw() {
        try {
            Database db = createTestDatabase();
            db.register(makeUser("admin2", "correctPw", true, false, false));
            User fake = makeUser("admin2", "wrongPw", true, false, false);
            if (db.loginAdmin(fake)) return "loginAdmin should return false for wrong password";
            db.closeConnection();
            return null;
        } catch (Exception e) { return "Exception: " + e.getMessage(); }
    }

    private static String test15_LoginRoles() {
        try {
            Database db = createTestDatabase();
            User student = makeUser("stu", "sp", false, true, false);
            db.register(student);
            if (!db.loginRole1(student)) return "loginRole1 should return true";
            if (db.loginRole2(student)) return "loginRole2 should return false (not a reviewer)";

            User reviewer = makeUser("rev", "rp", false, false, true);
            db.register(reviewer);
            if (!db.loginRole2(reviewer)) return "loginRole2 should return true for reviewer";
            if (db.loginRole1(reviewer)) return "loginRole1 should return false for reviewer-only";
            db.closeConnection();
            return null;
        } catch (Exception e) { return "Exception: " + e.getMessage(); }
    }

    private static String test16_CurrentPasswordMatch() {
        try {
            Database db = createTestDatabase();
            db.register(makeUser("pwUser", "mySecret", true, false, false));
            db.getUserAccountDetails("pwUser");
            if (!"mySecret".equals(db.getCurrentPassword()))
                return "getCurrentPassword mismatch: " + db.getCurrentPassword();
            db.closeConnection();
            return null;
        } catch (Exception e) { return "Exception: " + e.getMessage(); }
    }

    // ---------------------------------------------------------------
    // OTP + BVT
    // ---------------------------------------------------------------

    private static void runOtpTests() {
        System.out.println("\n--- OTP + BVT ---");
        report("Test 17: Valid OTP verifies", test17_ValidOtp());
        report("Test 18: Invalid OTP fails", test18_InvalidOtp());
        report("Test 19 (BVT): Null OTP fails", test19_NullOtp());
        report("Test 20 (BVT): Empty OTP fails", test20_EmptyOtp());
        report("Test 21: Expired OTP fails", test21_ExpiredOtp());
        report("Test 22: OTP single-use", test22_OtpSingleUse());
    }

    private static String test17_ValidOtp() {
        try {
            Database db = createTestDatabase();
            String otp = db.createOTP(5);
            if (otp == null || otp.isEmpty()) return "createOTP returned null/empty";
            if (!db.verifyOtpForCurrentUser(otp)) return "Valid OTP should verify true";
            db.closeConnection();
            return null;
        } catch (Exception e) { return "Exception: " + e.getMessage(); }
    }

    private static String test18_InvalidOtp() {
        try {
            Database db = createTestDatabase();
            db.createOTP(5);
            if (db.verifyOtpForCurrentUser("000000")) return "Wrong OTP should verify false";
            db.closeConnection();
            return null;
        } catch (Exception e) { return "Exception: " + e.getMessage(); }
    }

    private static String test19_NullOtp() {
        try {
            Database db = createTestDatabase();
            db.createOTP(5);
            if (db.verifyOtpForCurrentUser(null)) return "Null OTP should return false";
            db.closeConnection();
            return null;
        } catch (Exception e) { return "Exception: " + e.getMessage(); }
    }

    private static String test20_EmptyOtp() {
        try {
            Database db = createTestDatabase();
            db.createOTP(5);
            if (db.verifyOtpForCurrentUser("")) return "Empty OTP should return false";
            db.closeConnection();
            return null;
        } catch (Exception e) { return "Exception: " + e.getMessage(); }
    }

    private static String test21_ExpiredOtp() {
        try {
            Database db = createTestDatabase();
            String otp = db.createOTP(0);
            Thread.sleep(50);
            if (db.verifyOtpForCurrentUser(otp)) return "Expired OTP should return false";
            db.closeConnection();
            return null;
        } catch (Exception e) { return "Exception: " + e.getMessage(); }
    }

    private static String test22_OtpSingleUse() {
        try {
            Database db = createTestDatabase();
            String otp = db.createOTP(5);
            if (!db.verifyOtpForCurrentUser(otp)) return "First use should succeed";
            if (db.verifyOtpForCurrentUser(otp)) return "Second use should fail (single-use)";
            db.closeConnection();
            return null;
        } catch (Exception e) { return "Exception: " + e.getMessage(); }
    }

    // ---------------------------------------------------------------
    // Roles
    // ---------------------------------------------------------------

    private static void runRoleTests() {
        System.out.println("\n--- Roles ---");
        report("Test 23: getNumberOfRoles various combos", test23_NumberOfRoles());
        report("Test 24: updateUserRole changes DB", test24_UpdateUserRole());
    }

    private static String test23_NumberOfRoles() {
        try {
            Database db = createTestDatabase();
            User none = makeUser("noRole", "p", false, false, false);
            if (db.getNumberOfRoles(none) != 0) return "Expected 0 roles";

            User one = makeUser("oneRole", "p", true, false, false);
            if (db.getNumberOfRoles(one) != 1) return "Expected 1 role";

            User two = makeUser("twoRoles", "p", true, true, false);
            if (db.getNumberOfRoles(two) != 2) return "Expected 2 roles";

            User three = makeUser("allRoles", "p", true, true, true);
            if (db.getNumberOfRoles(three) != 3) return "Expected 3 roles";
            db.closeConnection();
            return null;
        } catch (Exception e) { return "Exception: " + e.getMessage(); }
    }

    private static String test24_UpdateUserRole() {
        try {
            Database db = createTestDatabase();
            db.register(makeUser("roleUser", "p", false, false, false));
            if (!db.updateUserRole("roleUser", "Admin", "true"))
                return "updateUserRole Admin should succeed";
            db.getUserAccountDetails("roleUser");
            if (!db.getCurrentAdminRole()) return "Admin role should be true after update";

            if (!db.updateUserRole("roleUser", "Role1", "true"))
                return "updateUserRole Role1 should succeed";
            db.getUserAccountDetails("roleUser");
            if (!db.getCurrentNewRole1()) return "Role1 should be true after update";

            if (!db.updateUserRole("roleUser", "Role2", "true"))
                return "updateUserRole Role2 should succeed";
            db.getUserAccountDetails("roleUser");
            if (!db.getCurrentNewRole2()) return "Role2 should be true after update";

            if (db.updateUserRole("roleUser", "BadRole", "true"))
                return "Invalid role name should return false";
            db.closeConnection();
            return null;
        } catch (Exception e) { return "Exception: " + e.getMessage(); }
    }

    // ---------------------------------------------------------------
    // Profile Getters / Updaters
    // ---------------------------------------------------------------

    private static void runProfileTests() {
        System.out.println("\n--- Profile Getters / Updaters ---");
        report("Test 25: firstName get/update roundtrip", test25_FirstNameRoundtrip());
        report("Test 26: getPreferredFirstName bug", test26_PreferredFirstNameBug());
        report("Test 27: emailAddress get/update roundtrip", test27_EmailRoundtrip());
        report("Test 28: middleName get/update roundtrip", test28_MiddleNameRoundtrip());
        report("Test 29: lastName get/update roundtrip", test29_LastNameRoundtrip());
        report("Test 30: updatePassword roundtrip", test30_UpdatePassword());
    }

    private static String test25_FirstNameRoundtrip() {
        try {
            Database db = createTestDatabase();
            db.register(new User("fn1", "p", "OldFirst", "", "", "", "e@e.com",
                    true, false, false));
            if (!"OldFirst".equals(db.getFirstName("fn1")))
                return "Initial first name wrong: " + db.getFirstName("fn1");
            db.updateFirstName("fn1", "NewFirst");
            if (!"NewFirst".equals(db.getFirstName("fn1")))
                return "Updated first name wrong: " + db.getFirstName("fn1");
            db.closeConnection();
            return null;
        } catch (Exception e) { return "Exception: " + e.getMessage(); }
    }

    private static String test26_PreferredFirstNameBug() {
        try {
            Database db = createTestDatabase();
            db.register(new User("pfn1", "p", "ActualFirst", "", "", "PrefName",
                    "e@e.com", true, false, false));
            String pref = db.getPreferredFirstName("pfn1");
            if ("PrefName".equals(pref)) {
                return null; // correct behavior
            }
            defects.add("Database.getPreferredFirstName() returns '" + pref
                    + "' instead of 'PrefName'. Line 740 uses rs.getString(\"firstName\") "
                    + "instead of rs.getString(\"preferredFirstName\").");
            return "Bug: getPreferredFirstName returns '" + pref + "' instead of 'PrefName'";
        } catch (Exception e) { return "Exception: " + e.getMessage(); }
    }

    private static String test27_EmailRoundtrip() {
        try {
            Database db = createTestDatabase();
            db.register(makeUser("em1", "p", true, false, false));
            if (!"em1@test.com".equals(db.getEmailAddress("em1")))
                return "Initial email wrong";
            db.updateEmailAddress("em1", "new@test.com");
            if (!"new@test.com".equals(db.getEmailAddress("em1")))
                return "Updated email wrong: " + db.getEmailAddress("em1");
            db.closeConnection();
            return null;
        } catch (Exception e) { return "Exception: " + e.getMessage(); }
    }

    private static String test28_MiddleNameRoundtrip() {
        try {
            Database db = createTestDatabase();
            db.register(new User("mn1", "p", "F", "OldMid", "L", "Pf",
                    "e@e.com", true, false, false));
            if (!"OldMid".equals(db.getMiddleName("mn1")))
                return "Initial middle name wrong: " + db.getMiddleName("mn1");
            db.updateMiddleName("mn1", "NewMid");
            if (!"NewMid".equals(db.getMiddleName("mn1")))
                return "Updated middle name wrong: " + db.getMiddleName("mn1");
            db.closeConnection();
            return null;
        } catch (Exception e) { return "Exception: " + e.getMessage(); }
    }

    private static String test29_LastNameRoundtrip() {
        try {
            Database db = createTestDatabase();
            db.register(new User("ln1", "p", "F", "M", "OldLast", "Pf",
                    "e@e.com", true, false, false));
            if (!"OldLast".equals(db.getLastName("ln1")))
                return "Initial last name wrong: " + db.getLastName("ln1");
            db.updateLastName("ln1", "NewLast");
            if (!"NewLast".equals(db.getLastName("ln1")))
                return "Updated last name wrong: " + db.getLastName("ln1");
            db.closeConnection();
            return null;
        } catch (Exception e) { return "Exception: " + e.getMessage(); }
    }

    private static String test30_UpdatePassword() {
        try {
            Database db = createTestDatabase();
            db.register(makeUser("pwu", "oldPw", true, false, false));
            db.updatePassword("pwu", "newPw");
            db.getUserAccountDetails("pwu");
            if (!"newPw".equals(db.getCurrentPassword()))
                return "Password should be 'newPw', got: " + db.getCurrentPassword();
            User u = makeUser("pwu", "newPw", true, false, false);
            if (!db.loginAdmin(u)) return "loginAdmin should succeed with new password";
            db.closeConnection();
            return null;
        } catch (Exception e) { return "Exception: " + e.getMessage(); }
    }

    // ---------------------------------------------------------------
    // Invitation Codes
    // ---------------------------------------------------------------

    private static void runInvitationTests() {
        System.out.println("\n--- Invitation Codes ---");
        report("Test 31: Generate and retrieve invitation", test31_InvitationGenRetrieve());
        report("Test 32: Remove invitation after use", test32_RemoveInvitation());
        report("Test 33: getNumberOfInvitations tracking", test33_InvitationCount());
    }

    private static String test31_InvitationGenRetrieve() {
        try {
            Database db = createTestDatabase();
            String code = db.generateInvitationCode("inv@test.com", "Admin");
            if (code == null || code.isEmpty()) return "Code should not be null/empty";
            String role = db.getRoleGivenAnInvitationCode(code);
            if (!"Admin".equals(role)) return "Role mismatch: " + role;
            String email = db.getEmailAddressUsingCode(code);
            if (!"inv@test.com".equals(email)) return "Email mismatch: " + email;
            db.closeConnection();
            return null;
        } catch (Exception e) { return "Exception: " + e.getMessage(); }
    }

    private static String test32_RemoveInvitation() {
        try {
            Database db = createTestDatabase();
            String code = db.generateInvitationCode("rm@test.com", "Role1");
            db.removeInvitationAfterUse(code);
            String role = db.getRoleGivenAnInvitationCode(code);
            if (!"".equals(role)) return "Role should be empty after removal, got: " + role;
            db.closeConnection();
            return null;
        } catch (Exception e) { return "Exception: " + e.getMessage(); }
    }

    private static String test33_InvitationCount() {
        try {
            Database db = createTestDatabase();
            if (db.getNumberOfInvitations() != 0) return "Should start at 0";
            db.generateInvitationCode("a@b.com", "Admin");
            db.generateInvitationCode("c@d.com", "Role1");
            if (db.getNumberOfInvitations() != 2) return "Expected 2, got " + db.getNumberOfInvitations();
            db.closeConnection();
            return null;
        } catch (Exception e) { return "Exception: " + e.getMessage(); }
    }

    // ---------------------------------------------------------------
    // Delete and Resilience
    // ---------------------------------------------------------------

    private static void runDeleteResilienceTests() {
        System.out.println("\n--- Delete and Resilience ---");
        report("Test 34: deleteUser removes user", test34_DeleteUser());
        report("Test 35: getUserList reflects DB state", test35_GetUserList());
    }

    private static String test34_DeleteUser() {
        try {
            Database db = createTestDatabase();
            db.register(makeUser("delMe", "p", true, false, false));
            if (!db.doesUserExist("delMe")) return "User should exist before delete";
            boolean deleted = db.deleteUser("delMe");
            if (!deleted) return "deleteUser should return true";
            if (db.doesUserExist("delMe")) return "User should not exist after delete";
            db.closeConnection();
            return null;
        } catch (Exception e) { return "Exception: " + e.getMessage(); }
    }

    private static String test35_GetUserList() {
        try {
            Database db = createTestDatabase();
            List<String> list = db.getUserList();
            if (list == null) return "getUserList should not be null";
            if (list.size() != 1) return "Should have 1 entry (<Select a User>), got " + list.size();

            db.register(makeUser("listU1", "p", true, false, false));
            db.register(makeUser("listU2", "p", false, true, false));
            list = db.getUserList();
            if (list.size() != 3) return "Expected 3 (header + 2 users), got " + list.size();
            if (!"<Select a User>".equals(list.get(0))) return "First entry should be placeholder";
            db.closeConnection();
            return null;
        } catch (Exception e) { return "Exception: " + e.getMessage(); }
    }
}
