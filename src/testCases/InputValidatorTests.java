package testCases;

import guiTools.InputValidator;

/*******
 * <p> Title: InputValidatorTests Class </p>
 *
 * <p> Description: Comprehensive test suite for the {@link InputValidator} utility class.
 * Tests cover SQL injection pattern detection, XSS/HTML injection stripping, boundary
 * length enforcement, format validation (OTP, invitation code, email), password complexity,
 * name validation, post content sanitization, and search keyword validation. </p>
 *
 * <p> Each test method returns {@code null} on success or a descriptive error message on failure.
 * Run {@link #main(String[])} to execute all tests and print PASS/FAIL results. </p>
 *
 * <p> Copyright: Arjun Chaudhary &copy; 2026 </p>
 *
 * @author Arjun Chaudhary
 *
 * @version 1.00	2026-04-11 Initial implementation for TP3 injection defense testing
 */
public class InputValidatorTests {

	private static int passed = 0;
	private static int failed = 0;

	/**
	 * Runs all InputValidator tests and prints results.
	 */
	public static void main(String[] args) {
		System.out.println("=== InputValidator Test Suite ===\n");

		runSQLInjectionTests();
		runXSSTests();
		runSanitizeTextTests();
		runPasswordTests();
		runEmailTests();
		runNameTests();
		runPostContentTests();
		runThreadNameTests();
		runSearchKeywordTests();
		runOTPTests();
		runInvitationCodeTests();
		runUsernameTests();

		System.out.println("\n=== Results: " + passed + " passed, " + failed + " failed ===");
	}

	// =========================================================================
	// SQL Injection Detection Tests
	// =========================================================================

	private static void runSQLInjectionTests() {
		System.out.println("--- SQL Injection Pattern Detection ---");
		check("SQLi-1: OR injection",
			sqlInjectionTest("' OR '1'='1", true));
		check("SQLi-2: DROP TABLE",
			sqlInjectionTest("'; DROP TABLE userDB;--", true));
		check("SQLi-3: UNION SELECT",
			sqlInjectionTest("admin UNION SELECT * FROM userDB", true));
		check("SQLi-4: Comment attack",
			sqlInjectionTest("admin'-- ", true));
		check("SQLi-5: EXEC command",
			sqlInjectionTest("'; EXEC xp_cmdshell('dir');", true));
		check("SQLi-6: Normal text safe",
			sqlInjectionTest("Hello World", false));
		check("SQLi-7: Normal name safe",
			sqlInjectionTest("John Smith", false));
		check("SQLi-8: AND injection",
			sqlInjectionTest("' AND 1=1 ", true));
		check("SQLi-9: INSERT injection",
			sqlInjectionTest("; INSERT INTO userDB VALUES", true));
		check("SQLi-10: Block comment",
			sqlInjectionTest("admin /* comment */", true));
	}

	private static String sqlInjectionTest(String input, boolean expectDetected) {
		boolean result = InputValidator.containsSQLInjectionPattern(input);
		if (result != expectDetected) {
			return "Expected " + (expectDetected ? "detected" : "safe") +
				" but got " + (result ? "detected" : "safe") + " for: " + input;
		}
		return null;
	}

	// =========================================================================
	// XSS / HTML Injection Tests
	// =========================================================================

	private static void runXSSTests() {
		System.out.println("\n--- XSS / HTML Injection ---");
		check("XSS-1: Script tag stripped",
			xssStripTest("<script>alert('xss')</script>Normal text", "Normal text"));
		check("XSS-2: Iframe stripped",
			xssStripTest("<iframe src=\"evil\">content</iframe>Safe", "Safe"));
		check("XSS-3: Img onerror stripped",
			xssStripTest("<img onerror=\"alert(1)\">Text", "Text"));
		check("XSS-4: SVG onload stripped",
			xssStripTest("<svg onload=\"alert(1)\">Data", "Data"));
		check("XSS-5: Mixed HTML tags stripped",
			xssStripTest("<b>Bold</b> <i>Italic</i>", "Bold Italic"));
		check("XSS-6: Normal text unchanged",
			xssStripTest("Hello World", "Hello World"));
	}

	private static String xssStripTest(String input, String expected) {
		String result = InputValidator.sanitizeText(input, 1000);
		if (!result.equals(expected)) {
			return "Expected '" + expected + "' but got '" + result + "'";
		}
		return null;
	}

	// =========================================================================
	// sanitizeText Tests
	// =========================================================================

	private static void runSanitizeTextTests() {
		System.out.println("\n--- sanitizeText ---");
		check("Sanitize-1: Null returns empty",
			sanitizeTest(null, 100, ""));
		check("Sanitize-2: Truncation at maxLength",
			sanitizeLengthTest("abcdefghij", 5, 5));
		check("Sanitize-3: Null bytes removed",
			sanitizeContainsTest("abc\0def", 100, "\0", false));
		check("Sanitize-4: Whitespace trimmed",
			sanitizeTest("  hello  ", 100, "hello"));
		check("Sanitize-5: Empty after strip",
			sanitizeTest("<script>alert(1)</script>", 100, ""));
	}

	private static String sanitizeTest(String input, int max, String expected) {
		String result = InputValidator.sanitizeText(input, max);
		if (!result.equals(expected)) {
			return "Expected '" + expected + "' but got '" + result + "'";
		}
		return null;
	}

	private static String sanitizeLengthTest(String input, int max, int expectedLen) {
		String result = InputValidator.sanitizeText(input, max);
		if (result.length() != expectedLen) {
			return "Expected length " + expectedLen + " but got " + result.length();
		}
		return null;
	}

	private static String sanitizeContainsTest(String input, int max, String substr, boolean shouldContain) {
		String result = InputValidator.sanitizeText(input, max);
		boolean contains = result.contains(substr);
		if (contains != shouldContain) {
			return "Expected " + (shouldContain ? "contains" : "does not contain") +
				" '" + substr + "' but got opposite in: " + result;
		}
		return null;
	}

	// =========================================================================
	// Password Validation Tests
	// =========================================================================

	private static void runPasswordTests() {
		System.out.println("\n--- Password Validation ---");
		check("PW-1: Valid password",
			expectNull(InputValidator.isValidPassword("Str0ng!Pw")));
		check("PW-2: Too short",
			expectNotNull(InputValidator.isValidPassword("Ab1!")));
		check("PW-3: No uppercase",
			expectNotNull(InputValidator.isValidPassword("abcdefg1!")));
		check("PW-4: No lowercase",
			expectNotNull(InputValidator.isValidPassword("ABCDEFG1!")));
		check("PW-5: No digit",
			expectNotNull(InputValidator.isValidPassword("Abcdefgh!")));
		check("PW-6: No special char",
			expectNotNull(InputValidator.isValidPassword("Abcdefg1")));
		check("PW-7: Contains space",
			expectNotNull(InputValidator.isValidPassword("Abc def1!")));
		check("PW-8: Null password",
			expectNotNull(InputValidator.isValidPassword(null)));
		check("PW-9: Empty password",
			expectNotNull(InputValidator.isValidPassword("")));
		check("PW-10: Exceeds max length",
			expectNotNull(InputValidator.isValidPassword("A".repeat(129) + "a1!")));
	}

	// =========================================================================
	// Email Validation Tests
	// =========================================================================

	private static void runEmailTests() {
		System.out.println("\n--- Email Validation ---");
		check("Email-1: Valid email",
			expectNull(InputValidator.isValidEmail("user@gmail.com")));
		check("Email-2: Valid complex email",
			expectNull(InputValidator.isValidEmail("first.last+tag@sub.domain.org")));
		check("Email-3: Missing @",
			expectNotNull(InputValidator.isValidEmail("usergmail.com")));
		check("Email-4: Missing domain",
			expectNotNull(InputValidator.isValidEmail("user@")));
		check("Email-5: Missing TLD",
			expectNotNull(InputValidator.isValidEmail("user@domain")));
		check("Email-6: Null email",
			expectNotNull(InputValidator.isValidEmail(null)));
		check("Email-7: Empty email",
			expectNotNull(InputValidator.isValidEmail("")));
		check("Email-8: SQL injection in email",
			expectNotNull(InputValidator.isValidEmail("'; DROP TABLE userDB;--@evil.com")));
		check("Email-9: Too long",
			expectNotNull(InputValidator.isValidEmail("a".repeat(250) + "@b.com")));
	}

	// =========================================================================
	// Name Validation Tests
	// =========================================================================

	private static void runNameTests() {
		System.out.println("\n--- Name Validation ---");
		check("Name-1: Valid simple name",
			expectNull(InputValidator.isValidName("John", 100)));
		check("Name-2: Valid hyphenated name",
			expectNull(InputValidator.isValidName("Smith-Jones", 100)));
		check("Name-3: Valid name with space",
			expectNull(InputValidator.isValidName("Mary Jane", 100)));
		check("Name-4: Rejects semicolon",
			expectNotNull(InputValidator.isValidName("John;", 100)));
		check("Name-5: Rejects numbers",
			expectNotNull(InputValidator.isValidName("John123", 100)));
		check("Name-6: Null name",
			expectNotNull(InputValidator.isValidName(null, 100)));
		check("Name-7: Empty name",
			expectNotNull(InputValidator.isValidName("", 100)));
		check("Name-8: Exceeds max length",
			expectNotNull(InputValidator.isValidName("A".repeat(101), 100)));
		check("Name-9: SQL injection in name",
			expectNotNull(InputValidator.isValidName("'; DROP TABLE userDB;--", 100)));
	}

	// =========================================================================
	// Post Content Validation Tests
	// =========================================================================

	private static void runPostContentTests() {
		System.out.println("\n--- Post Content Validation ---");
		check("Post-1: Valid content",
			expectNull(InputValidator.isValidPostContent("This is a valid post body.", 5000)));
		check("Post-2: Null content",
			expectNotNull(InputValidator.isValidPostContent(null, 5000)));
		check("Post-3: Empty content",
			expectNotNull(InputValidator.isValidPostContent("", 5000)));
		check("Post-4: Whitespace only",
			expectNotNull(InputValidator.isValidPostContent("   ", 5000)));
		check("Post-5: Script-only content rejected",
			expectNotNull(InputValidator.isValidPostContent("<script>alert(1)</script>", 5000)));
		check("Post-6: Content at max length",
			expectNull(InputValidator.isValidPostContent("A".repeat(5000), 5000)));
		check("Post-7: SQL pattern in content",
			expectNotNull(InputValidator.isValidPostContent("'; DROP TABLE userDB;-- rest of text", 5000)));
		check("Post-8: Sanitize preserves text",
			postSanitizeTest("Hello <b>World</b>!", 5000, "Hello World!"));
	}

	private static String postSanitizeTest(String input, int max, String expected) {
		String result = InputValidator.sanitizePostContent(input, max);
		if (!result.equals(expected)) {
			return "Expected '" + expected + "' but got '" + result + "'";
		}
		return null;
	}

	// =========================================================================
	// Thread Name Validation Tests
	// =========================================================================

	private static void runThreadNameTests() {
		System.out.println("\n--- Thread Name Validation ---");
		check("Thread-1: Valid name",
			expectNull(InputValidator.isValidThreadName("General", 50)));
		check("Thread-2: Valid with numbers",
			expectNull(InputValidator.isValidThreadName("Week 3 Discussion", 50)));
		check("Thread-3: Valid with hyphen",
			expectNull(InputValidator.isValidThreadName("Topic-One", 50)));
		check("Thread-4: Null is OK (defaults elsewhere)",
			expectNull(InputValidator.isValidThreadName(null, 50)));
		check("Thread-5: Empty is OK (defaults elsewhere)",
			expectNull(InputValidator.isValidThreadName("", 50)));
		check("Thread-6: Rejects special chars",
			expectNotNull(InputValidator.isValidThreadName("Topic; DROP", 50)));
		check("Thread-7: Exceeds max length",
			expectNotNull(InputValidator.isValidThreadName("A".repeat(51), 50)));
	}

	// =========================================================================
	// Search Keyword Validation Tests
	// =========================================================================

	private static void runSearchKeywordTests() {
		System.out.println("\n--- Search Keyword Validation ---");
		check("Search-1: Valid keyword",
			expectNull(InputValidator.isValidSearchKeyword("homework", 100)));
		check("Search-2: Null keyword is OK",
			expectNull(InputValidator.isValidSearchKeyword(null, 100)));
		check("Search-3: Empty keyword is OK",
			expectNull(InputValidator.isValidSearchKeyword("", 100)));
		check("Search-4: Exceeds max length",
			expectNotNull(InputValidator.isValidSearchKeyword("A".repeat(101), 100)));
		check("Search-5: SQL injection in keyword",
			expectNotNull(InputValidator.isValidSearchKeyword("' OR '1'='1", 100)));
	}

	// =========================================================================
	// OTP Validation Tests
	// =========================================================================

	private static void runOTPTests() {
		System.out.println("\n--- OTP Validation ---");
		check("OTP-1: Valid 6 digits",
			expectNull(InputValidator.isValidOTP("123456")));
		check("OTP-2: Too short",
			expectNotNull(InputValidator.isValidOTP("12345")));
		check("OTP-3: Too long",
			expectNotNull(InputValidator.isValidOTP("1234567")));
		check("OTP-4: Contains letters",
			expectNotNull(InputValidator.isValidOTP("12345a")));
		check("OTP-5: Null OTP",
			expectNotNull(InputValidator.isValidOTP(null)));
		check("OTP-6: Empty OTP",
			expectNotNull(InputValidator.isValidOTP("")));
		check("OTP-7: Spaces in OTP",
			expectNotNull(InputValidator.isValidOTP("12 345")));
	}

	// =========================================================================
	// Invitation Code Validation Tests
	// =========================================================================

	private static void runInvitationCodeTests() {
		System.out.println("\n--- Invitation Code Validation ---");
		check("InvCode-1: Valid 6 alphanumeric",
			expectNull(InputValidator.isValidInvitationCode("a1b2c3")));
		check("InvCode-2: Valid with hyphen (UUID substr)",
			expectNull(InputValidator.isValidInvitationCode("ab-cd3")));
		check("InvCode-3: Too short",
			expectNotNull(InputValidator.isValidInvitationCode("abc")));
		check("InvCode-4: Too long",
			expectNotNull(InputValidator.isValidInvitationCode("abcdefg")));
		check("InvCode-5: Contains special chars",
			expectNotNull(InputValidator.isValidInvitationCode("ab;cd3")));
		check("InvCode-6: Null code",
			expectNotNull(InputValidator.isValidInvitationCode(null)));
		check("InvCode-7: Empty code",
			expectNotNull(InputValidator.isValidInvitationCode("")));
	}

	// =========================================================================
	// Username Validation Tests
	// =========================================================================

	private static void runUsernameTests() {
		System.out.println("\n--- Username Validation ---");
		check("User-1: Valid username",
			expectNull(InputValidator.isValidUsername("john")));
		check("User-2: Null username",
			expectNotNull(InputValidator.isValidUsername(null)));
		check("User-3: Empty username",
			expectNotNull(InputValidator.isValidUsername("")));
		check("User-4: Too long (>16)",
			expectNotNull(InputValidator.isValidUsername("a".repeat(17))));
		check("User-5: SQL injection in username",
			expectNotNull(InputValidator.isValidUsername("admin'-- ")));
		check("User-6: At max length (16)",
			expectNull(InputValidator.isValidUsername("a".repeat(16))));
	}

	// =========================================================================
	// Helper methods
	// =========================================================================

	private static String expectNull(String result) {
		if (result != null) {
			return "Expected null (valid) but got: " + result;
		}
		return null;
	}

	private static String expectNotNull(String result) {
		if (result == null) {
			return "Expected error message but got null (valid)";
		}
		return null;
	}

	private static void check(String testName, String result) {
		if (result == null) {
			System.out.println("  " + testName + ": PASS");
			passed++;
		} else {
			System.out.println("  " + testName + ": FAIL - " + result);
			failed++;
		}
	}
}
