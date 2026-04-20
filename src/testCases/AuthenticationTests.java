package testCases;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import security.SessionManager;
import security.InputValidator;

/**
 * <p> Title: AuthenticationTests </p>
 *
 * <p> Description: JUnit test class for the authentication and session handling
 * components of the Student Discussion System. Tests cover {@link SessionManager}
 * and {@link InputValidator} — the two classes added in TP3 to address authentication
 * failures (CWE-287), insufficient session expiration (CWE-613), improper input
 * validation (CWE-20), and SQL injection defenses (CWE-89).
 *
 * <p> Each test method is named with a test case ID (TC-Sxx for session tests,
 * TC-Vxx for validation tests) that matches the Manual Tests document so the
 * grader can cross-reference the test code with the written test plan.
 *
 * <p> These tests do not require a database connection — SessionManager and
 * InputValidator are pure Java classes with no I/O dependencies, which makes
 * them fully automatable with JUnit.
 *
 * <p> User Stories covered:
 * <ul>
 *   <li>US-10 – Restrict Access to Authorized Users (hasRole tests)</li>
 *   <li>US-14 – Validate User Input (InputValidator tests)</li>
 * </ul>
 *
 * @author Sai Roy
 * @version 1.00  2026-04-17  Initial JUnit test implementation for TP3
 */
public class AuthenticationTests {

    /**
     * A fresh SessionManager instance created before each test.
     * Using @Before ensures tests are fully independent — no shared state
     * between tests that could cause order-dependent failures.
     */
    private SessionManager session;

    /**
     * Initializes a fresh SessionManager before each test method.
     * Validated by: all TC-S tests rely on this setup.
     */
    @Before
    public void setUp() {
        session = new SessionManager();
    }

    // =========================================================================
    // SESSION MANAGER TESTS (TC-S1 through TC-S10)
    // =========================================================================

    /**
     * TC-S1: Start session with valid username and role.
     * Expected: session is active, username and role stored correctly.
     * Validates: startSession stores the correct values.
     * Covers: US-10 (session required before role check).
     */
    @Test
    public void TC_S1_startSessionValidInputs() {
        session.startSession("sairoy", "Student");
        assertTrue("Session should be active after startSession", session.isLoggedIn());
        assertEquals("Username should match", "sairoy", session.getCurrentUsername());
        assertEquals("Role should match", "Student", session.getCurrentRole());
    }

    /**
     * TC-S2: Start session with null username.
     * Expected: IllegalArgumentException is thrown; session remains inactive.
     * Validates: startSession rejects null username (CWE-287 mitigation).
     */
    @Test(expected = IllegalArgumentException.class)
    public void TC_S2_startSessionNullUsername() {
        // Null username should not be allowed to create a session
        session.startSession(null, "Student");
    }

    /**
     * TC-S3: Start session with empty username.
     * Expected: IllegalArgumentException is thrown.
     * Validates: startSession rejects blank username.
     */
    @Test(expected = IllegalArgumentException.class)
    public void TC_S3_startSessionEmptyUsername() {
        session.startSession("   ", "Student");
    }

    /**
     * TC-S4: Start session with null role.
     * Expected: IllegalArgumentException is thrown.
     * Validates: startSession rejects null role — a session with no role
     * cannot be used for access control and should never be created.
     */
    @Test(expected = IllegalArgumentException.class)
    public void TC_S4_startSessionNullRole() {
        session.startSession("sairoy", null);
    }

    /**
     * TC-S5: Start session with empty role string.
     * Expected: IllegalArgumentException is thrown.
     * Validates: startSession rejects blank role.
     */
    @Test(expected = IllegalArgumentException.class)
    public void TC_S5_startSessionEmptyRole() {
        session.startSession("sairoy", "");
    }

    /**
     * TC-S6: End session clears all fields.
     * Expected: after endSession, isLoggedIn returns false and username/role are null.
     * Validates: endSession properly clears state (CWE-613 mitigation — no stale data).
     */
    @Test
    public void TC_S6_endSessionClearsState() {
        session.startSession("sairoy", "Admin");
        session.endSession();
        assertFalse("Session should be inactive after endSession", session.isLoggedIn());
        assertNull("Username should be null after logout", session.getCurrentUsername());
        assertNull("Role should be null after logout", session.getCurrentRole());
    }

    /**
     * TC-S7: isLoggedIn returns false before any session is started.
     * Expected: false immediately after construction.
     * Validates: no phantom session exists at construction time.
     */
    @Test
    public void TC_S7_isLoggedInFalseBeforeLogin() {
        // Fresh SessionManager from setUp — no startSession called
        assertFalse("Should not be logged in before startSession", session.isLoggedIn());
    }

    /**
     * TC-S8: isLoggedIn returns true immediately after a valid startSession.
     * Expected: true.
     * Validates: startSession activates the session correctly.
     */
    @Test
    public void TC_S8_isLoggedInTrueAfterValidLogin() {
        session.startSession("sairoy", "Student");
        assertTrue("Should be logged in after startSession", session.isLoggedIn());
    }

    /**
     * TC-S9: hasRole returns true when the session role matches.
     * Expected: hasRole("Admin") returns true for a session started with "Admin".
     * Validates: role-based access check works correctly (US-10).
     */
    @Test
    public void TC_S9_hasRoleReturnsTrueForCorrectRole() {
        session.startSession("sairoy", "Admin");
        assertTrue("hasRole should return true for the correct role", session.hasRole("Admin"));
    }

    /**
     * TC-S10: hasRole returns false when the session role does not match.
     * Expected: hasRole("Admin") returns false for a session started with "Student".
     * Validates: a student cannot pass an admin role check (US-10, access control).
     */
    @Test
    public void TC_S10_hasRoleReturnsFalseForWrongRole() {
        session.startSession("sairoy", "Student");
        assertFalse("hasRole should return false for a different role", session.hasRole("Admin"));
    }

    /**
     * TC-S11: hasRole returns false when no session is active.
     * Expected: false.
     * Validates: hasRole does not grant access without a valid session.
     */
    @Test
    public void TC_S11_hasRoleReturnsFalseWhenNotLoggedIn() {
        // No startSession called
        assertFalse("hasRole should be false with no active session", session.hasRole("Student"));
    }

    /**
     * TC-S12: Multiple sessions — starting a new session replaces the previous one.
     * Expected: after startSession("alice", "Student") then startSession("bob", "Admin"),
     * the active user is bob with role Admin.
     * Validates: session state is fully overwritten when a new session starts.
     * This is important because the app is single-user — two concurrent sessions
     * would indicate a state management bug.
     */
    @Test
    public void TC_S12_newSessionOverwritesPreviousSession() {
        session.startSession("alice", "Student");
        session.startSession("bob", "Admin");
        assertEquals("Username should be bob after second login", "bob", session.getCurrentUsername());
        assertEquals("Role should be Admin after second login", "Admin", session.getCurrentRole());
    }

    /**
     * TC-S13: endSession on an already-inactive session does not throw.
     * Expected: no exception is thrown; session remains inactive.
     * Validates: endSession is safe to call defensively even when not logged in.
     */
    @Test
    public void TC_S13_endSessionWhenNotLoggedInDoesNotThrow() {
        // No session started — calling endSession should be a no-op
        session.endSession(); // should not throw
        assertFalse("Session should remain inactive", session.isLoggedIn());
    }

    // =========================================================================
    // INPUT VALIDATOR TESTS (TC-V1 through TC-V12)
    // =========================================================================

    /**
     * TC-V1: validateUsername with a normal valid username.
     * Expected: returns null (no error).
     * Validates: valid usernames are accepted (CWE-20).
     */
    @Test
    public void TC_V1_validateUsernameValid() {
        assertNull("Valid username should return null error",
            InputValidator.validateUsername("sairoy"));
    }

    /**
     * TC-V2: validateUsername with null input.
     * Expected: returns a non-null error message.
     * Validates: null is rejected before reaching the database.
     */
    @Test
    public void TC_V2_validateUsernameNull() {
        String result = InputValidator.validateUsername(null);
        assertNotNull("Null username should return error message", result);
        assertTrue("Error should mention null", result.toLowerCase().contains("null"));
    }

    /**
     * TC-V3: validateUsername with blank/whitespace input.
     * Expected: returns a non-null error message mentioning "empty".
     * Validates: blank username is rejected (CWE-20).
     */
    @Test
    public void TC_V3_validateUsernameBlank() {
        String result = InputValidator.validateUsername("   ");
        assertNotNull("Blank username should return error message", result);
        assertTrue("Error should mention empty", result.toLowerCase().contains("empty"));
    }

    /**
     * TC-V4: validateUsername with SQL injection characters (single quote).
     * Expected: returns a non-null error message.
     * Validates: injection characters are rejected before reaching the database (CWE-89).
     */
    @Test
    public void TC_V4_validateUsernameInjectionChars() {
        String result = InputValidator.validateUsername("admin' OR '1'='1");
        assertNotNull("Injection username should return error message", result);
    }

    /**
     * TC-V4b: validateUsername with double-dash SQL comment injection.
     * Expected: returns a non-null error message.
     * Validates: double-dash injection pattern is blocked (CWE-89).
     */
    @Test
    public void TC_V4b_validateUsernameDoubleDashInjection() {
        String result = InputValidator.validateUsername("admin--");
        assertNotNull("Double-dash injection should return error message", result);
    }

    /**
     * TC-V5: validatePassword with a valid non-empty password.
     * Expected: returns null (no error).
     * Validates: valid passwords pass through correctly.
     */
    @Test
    public void TC_V5_validatePasswordValid() {
        assertNull("Valid password should return null error",
            InputValidator.validatePassword("MyP@ssw0rd"));
    }

    /**
     * TC-V6: validatePassword with null input.
     * Expected: returns a non-null error message.
     * Validates: null password is rejected (CWE-20).
     */
    @Test
    public void TC_V6_validatePasswordNull() {
        String result = InputValidator.validatePassword(null);
        assertNotNull("Null password should return error message", result);
    }

    /**
     * TC-V7: validatePassword with blank/whitespace input.
     * Expected: returns a non-null error message mentioning "empty".
     * Validates: blank password is rejected (CWE-20).
     */
    @Test
    public void TC_V7_validatePasswordBlank() {
        String result = InputValidator.validatePassword("   ");
        assertNotNull("Blank password should return error message", result);
        assertTrue("Error should mention empty", result.toLowerCase().contains("empty"));
    }

    /**
     * TC-V8: validateOTP with a valid 6-digit OTP.
     * Expected: returns null (no error).
     * Validates: correctly formatted OTPs are accepted.
     */
    @Test
    public void TC_V8_validateOTPValid() {
        assertNull("Valid 6-digit OTP should return null error",
            InputValidator.validateOTP("123456"));
    }

    /**
     * TC-V8b: validateOTP with wrong length (5 digits).
     * Expected: returns a non-null error message mentioning "6 digits".
     * Validates: OTPs of the wrong length are rejected.
     */
    @Test
    public void TC_V8b_validateOTPWrongLength() {
        String result = InputValidator.validateOTP("12345");
        assertNotNull("5-digit OTP should return error message", result);
        assertTrue("Error should mention 6 digits", result.contains("6"));
    }

    /**
     * TC-V8c: validateOTP with non-numeric characters.
     * Expected: returns a non-null error message.
     * Validates: alphabetic OTP input is rejected.
     */
    @Test
    public void TC_V8c_validateOTPNonNumeric() {
        String result = InputValidator.validateOTP("abc123");
        assertNotNull("Non-numeric OTP should return error message", result);
    }

    /**
     * TC-V9: validateOTP with null input.
     * Expected: returns a non-null error message.
     * Validates: null OTP is rejected.
     */
    @Test
    public void TC_V9_validateOTPNull() {
        String result = InputValidator.validateOTP(null);
        assertNotNull("Null OTP should return error message", result);
    }

    /**
     * TC-V10: validateOTP with empty string.
     * Expected: returns a non-null error message mentioning "empty".
     * Validates: empty OTP is rejected.
     */
    @Test
    public void TC_V10_validateOTPEmpty() {
        String result = InputValidator.validateOTP("");
        assertNotNull("Empty OTP should return error message", result);
        assertTrue("Error should mention empty", result.toLowerCase().contains("empty"));
    }
}
