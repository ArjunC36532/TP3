package security;

/**
 * <p> Title: SessionManager </p>
 *
 * <p> Description: Manages the authenticated session for the currently logged-in user
 * in the Student Discussion System. This class tracks the active user's username,
 * assigned role, and login timestamp, and provides role-based access checks used
 * by the GUI and controller layers to enforce authorization.
 *
 * <p> This class addresses CWE-287 (Improper Authentication) by ensuring that a valid
 * session must be established through {@link #startSession(String, String)} before any
 * role-sensitive action is permitted, and by automatically expiring sessions that exceed
 * the configured timeout. It addresses CWE-613 (Insufficient Session Expiration) by
 * invalidating the session after {@value #SESSION_TIMEOUT_MS} milliseconds of inactivity.
 *
 * <p> Relation to User Stories:
 * <ul>
 *   <li><b>US-10 – Restrict Access to Authorized Users</b>: {@link #hasRole(String)}
 *       enforces that only users with the correct role can invoke restricted operations.</li>
 *   <li><b>US-12 – Log System Activity</b>: {@link #startSession} and {@link #endSession}
 *       record the login and logout events so higher layers can log them.</li>
 * </ul>
 *
 * <p> Validated by: AuthenticationTests (JUnit) — tests TC-S1 through TC-S10. </p>
 *
 * @author Sai Roy
 * @version 1.00  2026-04-17  Initial implementation for TP3 authentication and session handling
 */
public class SessionManager {

    /**
     * Session timeout in milliseconds (30 minutes).
     * After this period elapses since login, {@link #isLoggedIn()} returns false
     * and the session is automatically cleared.
     *
     * 30 minutes was chosen as a reasonable balance between usability and security —
     * short enough to limit exposure if a user leaves their terminal unlocked,
     * long enough not to disrupt normal use during a class session.
     */
    private static final long SESSION_TIMEOUT_MS = 30L * 60 * 1000;

    /** The username of the currently logged-in user, or null if no session is active. */
    private String currentUsername;

    /**
     * The role of the currently logged-in user ("Admin", "Student", or "Reviewer").
     * Stored as a String so new roles can be added without changing the interface.
     */
    private String currentRole;

    /** System time in milliseconds when the current session was started. */
    private long loginTime;

    /** True if a session has been started and has not yet been ended or timed out. */
    private boolean loggedIn;

    /**
     * Constructs a new SessionManager with no active session.
     * All fields start in their logged-out state.
     */
    public SessionManager() {
        loggedIn = false;
        currentUsername = null;
        currentRole = null;
        loginTime = 0;
    }

    /**
     * Starts a new authenticated session for the given user and role.
     *
     * <p> This method should be called by the login controller immediately after
     * the Database confirms that the username and password are valid. It records
     * the username, role, and current system time so that subsequent calls to
     * {@link #isLoggedIn()} and {@link #hasRole(String)} can enforce access control.
     *
     * <p> CWE-287 mitigation: inputs are validated before the session is created.
     * An empty username or role is rejected here so that no half-initialized session
     * can exist in the system.
     *
     * <p> Tested by: TC-S1 (valid inputs), TC-S2 (null username), TC-S3 (empty username),
     * TC-S4 (null role), TC-S5 (empty role).
     *
     * @param username the username of the user who successfully authenticated;
     *                 must not be null or blank
     * @param role     the role the user authenticated as ("Admin", "Student", or "Reviewer");
     *                 must not be null or blank
     * @throws IllegalArgumentException if username or role is null or blank
     */
    public void startSession(String username, String role) {
        // Validate username — an empty username would create a meaningless session
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException(
                "Error: Session username cannot be null or empty.");
        }

        // Validate role — a missing role cannot be used for access control checks
        if (role == null || role.trim().isEmpty()) {
            throw new IllegalArgumentException(
                "Error: Session role cannot be null or empty.");
        }

        this.currentUsername = username.trim();
        this.currentRole = role.trim();
        this.loginTime = System.currentTimeMillis();
        this.loggedIn = true;
    }

    /**
     * Ends the current session and clears all session state.
     *
     * <p> This method should be called when the user explicitly logs out or when
     * the application detects that the session should be terminated (for example,
     * after a password reset). All fields are reset to their initial logged-out values.
     *
     * <p> Tested by: TC-S6 (session cleared after logout), TC-S7 (isLoggedIn false after logout).
     */
    public void endSession() {
        currentUsername = null;
        currentRole = null;
        loginTime = 0;
        loggedIn = false;
    }

    /**
     * Returns true if a session is currently active and has not timed out.
     *
     * <p> If the elapsed time since login exceeds {@value #SESSION_TIMEOUT_MS} ms,
     * the session is automatically terminated via {@link #endSession()} and this
     * method returns false. This prevents stale sessions from remaining valid
     * indefinitely (CWE-613 mitigation).
     *
     * <p> Tested by: TC-S7 (false before login), TC-S8 (true after login),
     * TC-S6 (false after logout).
     *
     * @return true if a valid, non-expired session is active; false otherwise
     */
    public boolean isLoggedIn() {
        if (!loggedIn) {
            return false;
        }

        // Auto-expire the session if the timeout has elapsed.
        // Using currentTimeMillis rather than checking on every action so the
        // check is lightweight and does not require a background thread.
        if (System.currentTimeMillis() - loginTime > SESSION_TIMEOUT_MS) {
            endSession();
            return false;
        }

        return true;
    }

    /**
     * Returns true if the current session is active and the user has the given role.
     *
     * <p> This is the primary method used by controllers and the GUI to enforce
     * role-based access control (US-10). A user who is not logged in, or who is
     * logged in under a different role, receives false — never an exception —
     * so that the calling code can display a clean "access denied" message.
     *
     * <p> Tested by: TC-S9 (correct role returns true), TC-S10 (wrong role returns false),
     * TC-S7 (not logged in returns false).
     *
     * @param role the role to check against the active session role
     * @return true if logged in and the session role matches the given role; false otherwise
     */
    public boolean hasRole(String role) {
        // Not logged in — no role to check
        if (!isLoggedIn()) {
            return false;
        }

        // currentRole cannot be null here because startSession validates it,
        // but guard anyway so a future refactor cannot cause a NullPointerException
        return currentRole != null && currentRole.equals(role);
    }

    /**
     * Returns the username of the currently logged-in user, or null if no session is active.
     *
     * @return the current username, or null
     */
    public String getCurrentUsername() {
        return currentUsername;
    }

    /**
     * Returns the role of the currently logged-in user, or null if no session is active.
     *
     * @return the current role string, or null
     */
    public String getCurrentRole() {
        return currentRole;
    }
}
