package guiTools;

import java.util.regex.Pattern;

/*******
 * <p> Title: InputValidator Class </p>
 *
 * <p> Description: A centralized, static utility providing defense-in-depth validation and
 * sanitization for all user input in the Foundations application. Every user-facing controller
 * delegates to these methods before persisting data or passing it to storage/database layers.
 *
 * <p> This class addresses OWASP Top-10 injection risks by:
 * <ul>
 *   <li>Enforcing maximum lengths on every text field to prevent buffer-style abuse</li>
 *   <li>Stripping HTML/script tags to mitigate cross-site scripting (XSS) in rendered content</li>
 *   <li>Detecting common SQL injection patterns as a defense-in-depth layer (the primary defense
 *       is the use of {@code PreparedStatement} in {@link database.Database})</li>
 *   <li>Whitelisting characters for structured fields (names, emails, codes)</li>
 *   <li>Validating format and complexity for passwords, OTPs, and invitation codes</li>
 * </ul>
 *
 * <p> Each public validation method returns {@code null} when the input is valid, or a
 * human-readable error message when it is not. Sanitization methods return the cleaned string. </p>
 *
 * <p> Copyright: Arjun Chaudhary &copy; 2026 </p>
 *
 * @author Arjun Chaudhary
 *
 * @version 1.00	2026-04-11 Initial implementation for TP3 injection defenses
 */
public class InputValidator {

	/** Maximum length for a person's name field (first, middle, last, preferred). */
	public static final int MAX_NAME_LENGTH = 100;

	/** Maximum length for an email address (RFC 5321). */
	public static final int MAX_EMAIL_LENGTH = 254;

	/** Maximum length for a post title. */
	public static final int MAX_POST_TITLE_LENGTH = 200;

	/** Maximum length for a post or reply body. */
	public static final int MAX_POST_BODY_LENGTH = 5000;

	/** Maximum length for a discussion thread name. */
	public static final int MAX_THREAD_NAME_LENGTH = 50;

	/** Maximum length for a search keyword. */
	public static final int MAX_SEARCH_KEYWORD_LENGTH = 100;

	/** Maximum length for a username (matches UserNameRecognizer upper bound). */
	public static final int MAX_USERNAME_LENGTH = 16;

	/** Maximum length for a password field. */
	public static final int MAX_PASSWORD_LENGTH = 128;

	private static final Pattern HTML_TAG_PATTERN = Pattern.compile("<[^>]*>");

	private static final Pattern SCRIPT_PATTERN = Pattern.compile(
			"(?i)<\\s*script[^>]*>.*?<\\s*/\\s*script\\s*>|" +
			"(?i)<\\s*script[^>]*>|" +
			"(?i)<\\s*iframe[^>]*>.*?<\\s*/\\s*iframe\\s*>|" +
			"(?i)<\\s*iframe[^>]*>|" +
			"(?i)<\\s*img[^>]*onerror[^>]*>|" +
			"(?i)<\\s*svg[^>]*onload[^>]*>");

	private static final Pattern SQL_INJECTION_PATTERN = Pattern.compile(
			"(?i)(';?\\s*(OR|AND)\\s)|" +
			"(?i)(;\\s*(DROP|DELETE|INSERT|UPDATE|ALTER|CREATE|TRUNCATE)\\s)|" +
			"(?i)(UNION\\s+(ALL\\s+)?SELECT)|" +
			"(?i)(--\\s)|" +
			"(/\\*.*\\*/)|" +
			"(?i)(\\bEXEC(UTE)?\\s)|" +
			"(?i)(\\bxp_)");

	private static final Pattern EMAIL_PATTERN = Pattern.compile(
			"^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

	private static final Pattern NAME_PATTERN = Pattern.compile(
			"^[A-Za-z\\s\\-]+$");

	private static final Pattern THREAD_NAME_PATTERN = Pattern.compile(
			"^[A-Za-z0-9\\s\\-]+$");

	private static final Pattern OTP_PATTERN = Pattern.compile("^\\d{6}$");

	private static final Pattern INVITATION_CODE_PATTERN = Pattern.compile("^[A-Za-z0-9\\-]{6}$");


	/*****
	 * <p> Method: sanitizeText(String input, int maxLength) </p>
	 *
	 * <p> Description: General-purpose sanitization that trims whitespace, removes null bytes,
	 * strips HTML-like tags, and truncates to the specified maximum length. This is the
	 * first line of defense applied to free-form text inputs. </p>
	 *
	 * @param input the raw user input
	 * @param maxLength the maximum allowed character count after trimming
	 * @return the sanitized string, or an empty string if input is null
	 */
	public static String sanitizeText(String input, int maxLength) {
		if (input == null) return "";
		String sanitized = input.replace("\0", "");
		sanitized = SCRIPT_PATTERN.matcher(sanitized).replaceAll("");
		sanitized = HTML_TAG_PATTERN.matcher(sanitized).replaceAll("");
		sanitized = sanitized.trim();
		if (sanitized.length() > maxLength) {
			sanitized = sanitized.substring(0, maxLength);
		}
		return sanitized;
	}


	/*****
	 * <p> Method: isValidName(String name, int maxLength) </p>
	 *
	 * <p> Description: Validates a person's name field (first, middle, last, preferred).
	 * Allows only letters, spaces, and hyphens. Rejects input containing SQL injection
	 * patterns or exceeding the length limit. </p>
	 *
	 * @param name the name string to validate
	 * @param maxLength the maximum allowed character count
	 * @return null if valid, or an error message describing the problem
	 */
	public static String isValidName(String name, int maxLength) {
		if (name == null || name.trim().isEmpty()) {
			return "Name cannot be empty.";
		}
		String trimmed = name.trim();
		if (trimmed.length() > maxLength) {
			return "Name cannot exceed " + maxLength + " characters.";
		}
		if (containsSQLInjectionPattern(trimmed)) {
			return "Name contains disallowed characters or patterns.";
		}
		if (!NAME_PATTERN.matcher(trimmed).matches()) {
			return "Name may only contain letters, spaces, and hyphens.";
		}
		return null;
	}


	/*****
	 * <p> Method: isValidEmail(String email) </p>
	 *
	 * <p> Description: Validates an email address against RFC-compliant format rules and
	 * a maximum length of 254 characters. Also checks for SQL injection patterns as a
	 * defense-in-depth measure. </p>
	 *
	 * @param email the email address to validate
	 * @return null if valid, or an error message describing the problem
	 */
	public static String isValidEmail(String email) {
		if (email == null || email.trim().isEmpty()) {
			return "Email address cannot be empty.";
		}
		String trimmed = email.trim();
		if (trimmed.length() > MAX_EMAIL_LENGTH) {
			return "Email address cannot exceed " + MAX_EMAIL_LENGTH + " characters.";
		}
		if (containsSQLInjectionPattern(trimmed)) {
			return "Email address contains disallowed characters or patterns.";
		}
		if (!EMAIL_PATTERN.matcher(trimmed).matches()) {
			return "Please enter a valid email address (example: user@gmail.com).";
		}
		return null;
	}


	/*****
	 * <p> Method: isValidPassword(String password) </p>
	 *
	 * <p> Description: Validates password complexity. Requires at least 8 characters, one
	 * uppercase letter, one lowercase letter, one digit, and one special character. Spaces
	 * are not allowed. This centralizes the rules previously only enforced during password
	 * update so they are also applied at account creation. </p>
	 *
	 * @param password the password to validate
	 * @return null if valid, or an error message describing the problem
	 */
	public static String isValidPassword(String password) {
		if (password == null || password.trim().isEmpty()) {
			return "Password cannot be empty.";
		}
		if (password.length() > MAX_PASSWORD_LENGTH) {
			return "Password cannot exceed " + MAX_PASSWORD_LENGTH + " characters.";
		}
		if (password.length() < 8) {
			return "Password must be at least 8 characters.";
		}
		if (password.contains(" ")) {
			return "Password cannot contain spaces.";
		}
		if (!password.matches(".*[A-Z].*")) {
			return "Password must include at least one uppercase letter.";
		}
		if (!password.matches(".*[a-z].*")) {
			return "Password must include at least one lowercase letter.";
		}
		if (!password.matches(".*\\d.*")) {
			return "Password must include at least one number.";
		}
		if (!password.matches(".*[^A-Za-z0-9].*")) {
			return "Password must include at least one special character.";
		}
		return null;
	}


	/*****
	 * <p> Method: isValidPostContent(String text, int maxLength) </p>
	 *
	 * <p> Description: Validates free-form post or reply content. Strips script and HTML tags
	 * for defense against XSS, enforces a maximum length, and checks for SQL injection
	 * patterns. Returns the sanitized text via {@link #sanitizeText} if valid. </p>
	 *
	 * @param text the raw content to validate
	 * @param maxLength the maximum allowed character count
	 * @return null if valid, or an error message describing the problem
	 */
	public static String isValidPostContent(String text, int maxLength) {
		if (text == null || text.trim().isEmpty()) {
			return "Content cannot be empty.";
		}
		String sanitized = sanitizeText(text, maxLength);
		if (sanitized.isEmpty()) {
			return "Content cannot be empty after removing disallowed markup.";
		}
		if (containsSQLInjectionPattern(sanitized)) {
			return "Content contains disallowed patterns.";
		}
		return null;
	}


	/*****
	 * <p> Method: sanitizePostContent(String text, int maxLength) </p>
	 *
	 * <p> Description: Convenience method that sanitizes post/reply content and returns
	 * the cleaned string. Callers should invoke {@link #isValidPostContent} first to check
	 * for errors. </p>
	 *
	 * @param text the raw content to sanitize
	 * @param maxLength the maximum allowed character count
	 * @return the sanitized text string
	 */
	public static String sanitizePostContent(String text, int maxLength) {
		return sanitizeText(text, maxLength);
	}


	/*****
	 * <p> Method: isValidThreadName(String thread, int maxLength) </p>
	 *
	 * <p> Description: Validates a discussion thread name. Allows alphanumeric characters,
	 * spaces, and hyphens only. Enforces a maximum length. </p>
	 *
	 * @param thread the thread name to validate
	 * @param maxLength the maximum allowed character count
	 * @return null if valid, or an error message describing the problem
	 */
	public static String isValidThreadName(String thread, int maxLength) {
		if (thread == null || thread.trim().isEmpty()) {
			return null; // empty thread defaults to "General" elsewhere
		}
		String trimmed = thread.trim();
		if (trimmed.length() > maxLength) {
			return "Thread name cannot exceed " + maxLength + " characters.";
		}
		if (containsSQLInjectionPattern(trimmed)) {
			return "Thread name contains disallowed patterns.";
		}
		if (!THREAD_NAME_PATTERN.matcher(trimmed).matches()) {
			return "Thread name may only contain letters, numbers, spaces, and hyphens.";
		}
		return null;
	}


	/*****
	 * <p> Method: isValidSearchKeyword(String keyword, int maxLength) </p>
	 *
	 * <p> Description: Validates a search keyword. Enforces a maximum length and rejects
	 * SQL injection patterns. Returns null for empty keywords (treated as "show all"). </p>
	 *
	 * @param keyword the search keyword to validate
	 * @param maxLength the maximum allowed character count
	 * @return null if valid (or empty), or an error message describing the problem
	 */
	public static String isValidSearchKeyword(String keyword, int maxLength) {
		if (keyword == null || keyword.trim().isEmpty()) {
			return null; // empty keyword is acceptable (shows all)
		}
		String trimmed = keyword.trim();
		if (trimmed.length() > maxLength) {
			return "Search keyword cannot exceed " + maxLength + " characters.";
		}
		if (containsSQLInjectionPattern(trimmed)) {
			return "Search keyword contains disallowed patterns.";
		}
		return null;
	}


	/*****
	 * <p> Method: isValidInvitationCode(String code) </p>
	 *
	 * <p> Description: Validates an invitation code format. Must be exactly 6 alphanumeric
	 * characters (or alphanumeric with hyphens, matching the UUID substring format used by
	 * {@link database.Database#generateInvitationCode}). </p>
	 *
	 * @param code the invitation code to validate
	 * @return null if valid, or an error message describing the problem
	 */
	public static String isValidInvitationCode(String code) {
		if (code == null || code.trim().isEmpty()) {
			return "Invitation code cannot be empty.";
		}
		String trimmed = code.trim();
		if (!INVITATION_CODE_PATTERN.matcher(trimmed).matches()) {
			return "Invitation code must be exactly 6 alphanumeric characters.";
		}
		return null;
	}


	/*****
	 * <p> Method: isValidOTP(String otp) </p>
	 *
	 * <p> Description: Validates a one-time password format. Must be exactly 6 digits. </p>
	 *
	 * @param otp the one-time password to validate
	 * @return null if valid, or an error message describing the problem
	 */
	public static String isValidOTP(String otp) {
		if (otp == null || otp.trim().isEmpty()) {
			return "One-time password cannot be empty.";
		}
		String trimmed = otp.trim();
		if (!OTP_PATTERN.matcher(trimmed).matches()) {
			return "One-time password must be exactly 6 digits.";
		}
		return null;
	}


	/*****
	 * <p> Method: containsSQLInjectionPattern(String input) </p>
	 *
	 * <p> Description: Defense-in-depth check that scans for common SQL injection patterns
	 * such as {@code ' OR}, {@code ; DROP}, {@code UNION SELECT}, {@code --}, and inline
	 * comments. The primary SQL injection defense is the exclusive use of
	 * {@code PreparedStatement} in {@link database.Database}; this method provides an
	 * additional layer of protection. </p>
	 *
	 * @param input the string to check
	 * @return true if a suspicious pattern is detected, false otherwise
	 */
	public static boolean containsSQLInjectionPattern(String input) {
		if (input == null || input.isEmpty()) {
			return false;
		}
		return SQL_INJECTION_PATTERN.matcher(input).find();
	}


	/*****
	 * <p> Method: isValidUsername(String username) </p>
	 *
	 * <p> Description: Validates a username by enforcing a length cap and checking for
	 * SQL injection patterns. The detailed FSM-based character validation is handled by
	 * {@link UserNameRecognizer#checkForValidUserName}, so this method only adds the
	 * injection-defense layer. </p>
	 *
	 * @param username the username to validate
	 * @return null if valid, or an error message describing the problem
	 */
	public static String isValidUsername(String username) {
		if (username == null || username.trim().isEmpty()) {
			return "Username cannot be empty.";
		}
		if (username.length() > MAX_USERNAME_LENGTH) {
			return "Username cannot exceed " + MAX_USERNAME_LENGTH + " characters.";
		}
		if (containsSQLInjectionPattern(username)) {
			return "Username contains disallowed characters or patterns.";
		}
		return null;
	}
}
