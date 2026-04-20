package security;

/**
 * <p> Title: InputValidator </p>
 *
 * <p> Description: Provides static input validation methods for authentication-related
 * fields in the Student Discussion System. All login and registration inputs pass
 * through this class before reaching the Database layer, ensuring that null, blank,
 * or potentially dangerous inputs are caught early.
 *
 * <p> This class addresses CWE-89 (SQL Injection) by detecting and rejecting inputs
 * that contain characters commonly used in injection attacks (single quotes, double
 * dashes, semicolons). The Database layer also uses PreparedStatements, so this
 * validator is a defense-in-depth measure — not a replacement for parameterized queries.
 *
 * <p> This class addresses CWE-20 (Improper Input Validation) by enforcing non-null,
 * non-blank rules on all required fields before any database operation is attempted.
 *
 * <p> Validated by: AuthenticationTests (JUnit) — tests TC-V1 through TC-V8. </p>
 *
 * @author Sai Roy
 * @version 1.00  2026-04-17  Initial implementation for TP3 authentication and session handling
 */
public class InputValidator {

    /**
     * Private constructor — this class is a utility class with only static methods
     * and should never be instantiated directly.
     */
    private InputValidator() {
        // Not instantiable
    }

    /**
     * Validates a username field for login or registration.
     *
     * <p> A valid username must:
     * <ul>
     *   <li>Not be null</li>
     *   <li>Not be blank (whitespace-only counts as blank)</li>
     *   <li>Not contain SQL injection characters: {@code '}, {@code --}, {@code ;}</li>
     * </ul>
     *
     * <p> The injection check is a defense-in-depth measure. The Database class uses
     * PreparedStatements (parameterized queries) which already prevent SQL injection,
     * but checking here catches obviously malicious input before it reaches the DB layer
     * and allows a clearer error message to be shown to the user.
     *
     * <p> Tested by: TC-V1 (valid), TC-V2 (null), TC-V3 (blank), TC-V4 (injection chars).
     *
     * @param username the username string to validate
     * @return null if valid; an error message string if invalid
     */
    public static String validateUsername(String username) {
        if (username == null) {
            return "Error: Username cannot be null.";
        }
        if (username.trim().isEmpty()) {
            return "Error: Username cannot be empty. Please enter your username.";
        }

        // Defense-in-depth: reject obvious injection patterns even though
        // the DB layer uses PreparedStatements. This gives a cleaner error
        // message and prevents garbage from reaching the database at all.
        if (containsInjectionChars(username)) {
            return "Error: Username contains invalid characters.";
        }

        return null; // valid
    }

    /**
     * Validates a password field for login or account creation.
     *
     * <p> A valid password must:
     * <ul>
     *   <li>Not be null</li>
     *   <li>Not be blank (whitespace-only counts as blank)</li>
     * </ul>
     *
     * <p> Passwords are not checked for injection characters here because the Database
     * layer uses PreparedStatements and a user's password may legitimately contain
     * special characters. The only requirement at the input layer is that the field
     * is not empty.
     *
     * <p> Tested by: TC-V5 (valid), TC-V6 (null), TC-V7 (blank).
     *
     * @param password the password string to validate
     * @return null if valid; an error message string if invalid
     */
    public static String validatePassword(String password) {
        if (password == null) {
            return "Error: Password cannot be null.";
        }
        if (password.trim().isEmpty()) {
            return "Error: Password cannot be empty. Please enter your password.";
        }
        return null; // valid
    }

    /**
     * Validates an OTP (one-time password) string entered by the user during
     * password reset or two-factor authentication.
     *
     * <p> A valid OTP input must:
     * <ul>
     *   <li>Not be null</li>
     *   <li>Not be blank</li>
     *   <li>Consist of exactly 6 numeric digits</li>
     * </ul>
     *
     * <p> Tested by: TC-V8 (valid 6-digit OTP), TC-V8b (wrong length), TC-V8c (non-numeric).
     *
     * @param otp the OTP string entered by the user
     * @return null if valid; an error message string if invalid
     */
    public static String validateOTP(String otp) {
        if (otp == null) {
            return "Error: OTP cannot be null.";
        }
        String trimmed = otp.trim();
        if (trimmed.isEmpty()) {
            return "Error: OTP cannot be empty. Please enter the 6-digit code.";
        }

        // OTPs in this system are always 6-digit numeric strings (see Database.generateOTP)
        // Any other format is either a typo or a manipulation attempt
        if (!trimmed.matches("\\d{6}")) {
            return "Error: OTP must be exactly 6 digits.";
        }

        return null; // valid
    }

    /**
     * Returns true if the given string contains characters commonly used in
     * SQL injection attacks: single quote ({@code '}), double-dash ({@code --}),
     * or semicolon ({@code ;}).
     *
     * <p> This is a conservative check. It may reject some unusual but technically
     * valid usernames (for example, a username containing an apostrophe). The policy
     * decision here is to prioritize security over supporting edge-case usernames,
     * consistent with the system's focus on safe input handling (US-14).
     *
     * @param input the string to check
     * @return true if injection characters are present; false otherwise
     */
    private static boolean containsInjectionChars(String input) {
        // Check for the three most common SQL injection entry points.
        // Single quotes are used to break out of string literals.
        // Double-dashes start SQL comments that can hide injected clauses.
        // Semicolons terminate a statement and allow a second statement to follow.
        return input.contains("'") || input.contains("--") || input.contains(";");
    }
}
