package guiDiscussion;

import applicationMain.FoundationsMain;
import guiTools.InputValidator;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

/*******
 * <p> Title: ControllerUpdatePost Class </p>
 * 
 * <p> Description: Controller for the Update Post dialog. Validates content, calls
 * postStorage.updatePost(), handles authorization errors. </p>
 * 
 * <p> Copyright: Arjun Chaudhary © 2026 </p>
 * 
 * @author Arjun Chaudhary
 * @version 1.00	2026-02-17 Initial implementation
 */
public class ControllerUpdatePost {

	/**
	 * Update a post. Returns true if successful. Shows alerts for errors.
	 */
	public static boolean performUpdate(String postID, String newTitle, String newBody, String author) {
		String titleError = InputValidator.isValidPostContent(newTitle, InputValidator.MAX_POST_TITLE_LENGTH);
		if (titleError != null) {
			showError("Title: " + titleError);
			return false;
		}
		String bodyError = InputValidator.isValidPostContent(newBody, InputValidator.MAX_POST_BODY_LENGTH);
		if (bodyError != null) {
			showError("Body: " + bodyError);
			return false;
		}
		String sanitizedTitle = InputValidator.sanitizePostContent(newTitle, InputValidator.MAX_POST_TITLE_LENGTH);
		String sanitizedBody = InputValidator.sanitizePostContent(newBody, InputValidator.MAX_POST_BODY_LENGTH);
		String err = FoundationsMain.postStorage.updatePost(postID, sanitizedTitle, sanitizedBody, author);
		if (err != null) {
			showError(err);
			return false;
		}
		Alert success = new Alert(AlertType.INFORMATION);
		success.setTitle("Success");
		success.setHeaderText("Post Updated");
		success.setContentText("The post has been updated successfully.");
		success.showAndWait();
		return true;
	}

	private static void showError(String message) {
		Alert alert = new Alert(AlertType.ERROR);
		alert.setTitle("Error");
		alert.setHeaderText("Cannot Update Post");
		alert.setContentText(message);
		alert.showAndWait();
	}
}
