package security;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/*******
 * <p> Title: SecurityLogger Class </p>
 *
 * <p> Description: A singleton security logger that captures security-relevant
 * events for the Foundations application.  Every event is written to a flat log
 * file (security_events.log) so it persists between runs, and also kept in an
 * in-memory list so unit tests can inspect entries without touching the disk.
 * Events covered include login attempts, access-control violations, authentication
 * failures, password changes, account creation/deletion, role changes, and
 * suspicious input that might signal an injection attempt. </p>
 *
 * <p> Every entry starts with a timestamp and a bracketed tag (e.g. [LOGIN]) so
 * the log can be grep'd by event type if needed. </p>
 *
 * <p> Testing: all public methods are exercised by testCases.SecurityLoggingTests. </p>
 *
 * <p> Copyright: Sudharshan Ramadass &copy; 2026 </p>
 *
 * @author Sudharshan Ramadass
 *
 * @version 1.00	2026-04-15	Initial implementation for TP3
 */
public class SecurityLogger {

    private static final String LOG_FILE        = "security_events.log";
    private static final String TIMESTAMP_FORMAT = "yyyy-MM-dd HH:mm:ss";

    /** Prefix tag for login attempt entries. */
    public static final String TAG_LOGIN          = "[LOGIN]";

    /** Prefix tag for access-control violation entries. */
    public static final String TAG_ACCESS         = "[ACCESS_VIOLATION]";

    /** Prefix tag for authentication failure entries. */
    public static final String TAG_AUTH_FAIL      = "[AUTH_FAILURE]";

    /** Prefix tag for password change entries. */
    public static final String TAG_PASSWORD       = "[PASSWORD_CHANGE]";

    /** Prefix tag for account creation entries. */
    public static final String TAG_ACCOUNT_CREATE = "[ACCOUNT_CREATED]";

    /** Prefix tag for account deletion entries. */
    public static final String TAG_ACCOUNT_DELETE = "[ACCOUNT_DELETED]";

    /** Prefix tag for role change entries. */
    public static final String TAG_ROLE_CHANGE    = "[ROLE_CHANGE]";

    /** Prefix tag for suspicious input entries. */
    public static final String TAG_SUSPICIOUS     = "[SUSPICIOUS_INPUT]";

    private static SecurityLogger instance;

    private final Logger            javaLogger;
    private final List<String>      logEntries;
    private final DateTimeFormatter formatter;


    /*******
     * <p> Method: SecurityLogger() </p>
     *
     * <p> Description: Private constructor — use getInstance() instead.  Sets up
     * the java.util.logging Logger and attaches a FileHandler that appends to
     * security_events.log.  If the file can't be opened the logger still works
     * in memory; it just won't write to disk. </p>
     *
     */
    private SecurityLogger() {
        formatter  = DateTimeFormatter.ofPattern(TIMESTAMP_FORMAT);
        logEntries = new ArrayList<>();
        javaLogger = Logger.getLogger("SecurityLogger");

        // Suppress the default console handler — we only want our file output
        javaLogger.setUseParentHandlers(false);

        try {
            // append = true keeps old entries when the app restarts
            FileHandler fh = new FileHandler(LOG_FILE, true);
            fh.setFormatter(new PlainTextFormatter());
            javaLogger.addHandler(fh);
        } catch (IOException e) {
            System.err.println("SecurityLogger: couldn't open " + LOG_FILE + " — " + e.getMessage());
        }
    }


    /*******
     * <p> Method: getInstance() </p>
     *
     * @return the singleton SecurityLogger
     */
    public static synchronized SecurityLogger getInstance() {
        if (instance == null) {
            instance = new SecurityLogger();
        }
        return instance;
    }


    /*******
     * <p> Method: logLoginAttempt(String username, boolean success) </p>
     *
     * @param username the username submitted during the login attempt
     * @param success  true if the login succeeded, false if it failed
     */
    public void logLoginAttempt(String username, boolean success) {
        String user   = sanitize(username);
        String status = success ? "SUCCESS" : "FAILURE";
        String entry  = buildEntry(TAG_LOGIN, "user=" + user + " result=" + status);
        record(entry, success ? Level.INFO : Level.WARNING);
    }


    /*******
     * <p> Method: logAccessViolation(String username, String attemptedAction) </p>
     *
     * @param username        the user who attempted the unauthorized action
     * @param attemptedAction a short description of what was attempted
     */
    public void logAccessViolation(String username, String attemptedAction) {
        String user   = sanitize(username);
        String action = sanitize(attemptedAction);
        String entry  = buildEntry(TAG_ACCESS, "user=" + user + " action=" + action);
        record(entry, Level.WARNING);
    }


    /*******
     * <p> Method: logAuthFailure(String username, String reason) </p>
     *
     * @param username the user the failure is associated with
     * @param reason   a short explanation of why authentication failed
     */
    public void logAuthFailure(String username, String reason) {
        String user  = sanitize(username);
        String rsn   = sanitize(reason);
        String entry = buildEntry(TAG_AUTH_FAIL, "user=" + user + " reason=" + rsn);
        record(entry, Level.WARNING);
    }


    /*******
     * <p> Method: logPasswordChange(String username) </p>
     *
     * @param username the user whose password was changed
     */
    public void logPasswordChange(String username) {
        String user  = sanitize(username);
        String entry = buildEntry(TAG_PASSWORD, "user=" + user);
        record(entry, Level.INFO);
    }


    /*******
     * <p> Method: logAccountCreated(String username, String role) </p>
     *
     * @param username the new account's username
     * @param role     the role assigned at creation (e.g. "Admin", "Role1", "Role2")
     */
    public void logAccountCreated(String username, String role) {
        String user  = sanitize(username);
        String r     = sanitize(role);
        String entry = buildEntry(TAG_ACCOUNT_CREATE, "user=" + user + " role=" + r);
        record(entry, Level.INFO);
    }


    /*******
     * <p> Method: logAccountDeleted(String adminUsername, String deletedUsername) </p>
     *
     * @param adminUsername   the admin who performed the deletion
     * @param deletedUsername the account that was deleted
     */
    public void logAccountDeleted(String adminUsername, String deletedUsername) {
        String admin   = sanitize(adminUsername);
        String deleted = sanitize(deletedUsername);
        String entry   = buildEntry(TAG_ACCOUNT_DELETE, "admin=" + admin + " deleted=" + deleted);
        record(entry, Level.INFO);
    }


    /*******
     * <p> Method: logRoleChange(String adminUsername, String targetUsername, String role, boolean granted) </p>
     *
     * @param adminUsername  the admin making the change
     * @param targetUsername the user whose role is being updated
     * @param role           the role being modified
     * @param granted        true if the role was added, false if it was removed
     */
    public void logRoleChange(String adminUsername, String targetUsername, String role, boolean granted) {
        String admin  = sanitize(adminUsername);
        String target = sanitize(targetUsername);
        String r      = sanitize(role);
        String action = granted ? "GRANTED" : "REVOKED";
        String entry  = buildEntry(TAG_ROLE_CHANGE,
                "admin=" + admin + " target=" + target + " role=" + r + " change=" + action);
        record(entry, Level.INFO);
    }


    /*******
     * <p> Method: logSuspiciousInput(String username, String field, String value) </p>
     *
     * @param username the user who submitted the input (can be null if not logged in)
     * @param field    the name of the field that received the suspicious value
     * @param value    the suspicious value itself
     */
    public void logSuspiciousInput(String username, String field, String value) {
        String user = sanitize(username);
        String f    = sanitize(field);
        // Cap at 80 chars — a crafted input could otherwise be thousands of characters long
        String val  = value != null ? value.substring(0, Math.min(value.length(), 80)) : "<null>";
        String entry = buildEntry(TAG_SUSPICIOUS,
                "user=" + user + " field=" + f + " value=[" + val + "]");
        record(entry, Level.SEVERE);
    }


    /*******
     * <p> Method: getLogEntries() </p>
     *
     * @return unmodifiable list of log entry strings in the order they were added
     */
    public List<String> getLogEntries() {
        return Collections.unmodifiableList(logEntries);
    }


    /*******
     * <p> Method: clearLog() </p>
     *
     * <p> Description: Clears the in-memory log list.  Does not touch the log file
     * on disk — only the in-memory copy is affected. </p>
     *
     */
    public void clearLog() {
        logEntries.clear();
    }


    // Stamps the entry with the current time and the event tag
    private String buildEntry(String tag, String message) {
        String timestamp = LocalDateTime.now().format(formatter);
        return timestamp + " " + tag + " " + message;
    }

    // synchronized so rapid calls from different threads don't interleave entries
    private synchronized void record(String entry, Level level) {
        logEntries.add(entry);
        javaLogger.log(level, entry);
    }

    // Replaces null with a placeholder so log lines are always complete
    private String sanitize(String s) {
        return (s == null) ? "<unknown>" : s;
    }


    /*******
     * <p> Class: PlainTextFormatter </p>
     *
     * <p> Description: Replaces the default java.util.logging formatter, which
     * produces verbose multi-line output.  This one writes each record as a
     * single plain text line. </p>
     */
    private static class PlainTextFormatter extends Formatter {

        /*******
         * <p> Method: format(LogRecord record) </p>
         *
         * @param record the record to format
         * @return the message text with a trailing newline
         */
        @Override
        public String format(LogRecord record) {
            return record.getMessage() + System.lineSeparator();
        }
    }
}
