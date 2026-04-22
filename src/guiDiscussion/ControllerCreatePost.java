package guiDiscussion;

import entityClasses.Post;
import applicationMain.FoundationsMain;
import guiTools.InputValidator;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

/*******
 * <p> Title: ControllerCreatePost Class </p>
 * 
 * <p> Description: Controller for the Create Post dialog. Validates input, creates Post,
 * calls postStorage.addPost(), and shows success/error alerts. </p>
 * 
 * <p> Copyright: Arjun Chaudhary © 2026 </p>
 * 
 * @author Arjun Chaudhary
 * @version 1.00	2026-02-17 Initial implementation
 */
public class ControllerCreatePost {

	/**
	 * Create a new post with the given title, body, thread, and author.
	 * Returns true if successful, false otherwise. Shows alerts for errors.
	 */
	public static boolean performCreate(String title, String body, String thread, String author) {
		String titleError = InputValidator.isValidPostContent(title, InputValidator.MAX_POST_TITLE_LENGTH);
		if (titleError != null) {
			showError("Title: " + titleError);
			return false;
		}
		String bodyError = InputValidator.isValidPostContent(body, InputValidator.MAX_POST_BODY_LENGTH);
		if (bodyError != null) {
			showError("Body: " + bodyError);
			return false;
		}
		if (author == null || author.trim().isEmpty()) {
			showError("Author cannot be empty.");
			return false;
		}
		String threadError = InputValidator.isValidThreadName(thread, InputValidator.MAX_THREAD_NAME_LENGTH);
		if (threadError != null) {
			showError("Thread: " + threadError);
			return false;
		}
		String sanitizedTitle = InputValidator.sanitizePostContent(title, InputValidator.MAX_POST_TITLE_LENGTH);
		String sanitizedBody = InputValidator.sanitizePostContent(body, InputValidator.MAX_POST_BODY_LENGTH);
		String threadVal = (thread == null || thread.trim().isEmpty()) ? "General" : thread.trim();
		Post post = new Post(sanitizedTitle, sanitizedBody, threadVal, author);
		String err = FoundationsMain.postStorage.addPost(post);
		if (err != null) {
			showError(err);
			return false;
		}
		Alert success = new Alert(AlertType.INFORMATION);
		success.setTitle("Success");
		success.setHeaderText("Post Created");
		success.setContentText("Your post has been created successfully.");
		success.showAndWait();
		return true;
	}

	private static void showError(String message) {
		Alert alert = new Alert(AlertType.ERROR);
		alert.setTitle("Error");
		alert.setHeaderText("Cannot Create Post");
		alert.setContentText(message);
		alert.showAndWait();
	}
}
