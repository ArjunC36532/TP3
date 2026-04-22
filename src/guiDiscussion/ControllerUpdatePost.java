package guiDiscussion;

import applicationMain.FoundationsMain;
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
 * @version 1.10	2026-04-21 Updated for TP3 access control
 */
public class ControllerUpdatePost {

	/**
	 * Update a post. Returns true if successful. Shows alerts for errors.
	 */
	public static boolean performUpdate(String postID, String newTitle, String newBody, String author) {
		if (newTitle == null || newTitle.trim().isEmpty()) {
			showError("Updated title cannot be null, empty, or whitespace-only.");
			return false;
		}
		if (newBody == null || newBody.trim().isEmpty()) {
			showError("Updated body cannot be null, empty, or whitespace-only.");
			return false;
		}

		String currentUsername = ControllerPostDetail.getCurrentUsername();
		boolean isAdmin = ControllerPostDetail.isAdmin();

		if (currentUsername == null || currentUsername.trim().isEmpty()) {
			currentUsername = author;
		}

		String err = FoundationsMain.postStorage.updatePost(
				postID,
				newTitle.trim(),
				newBody.trim(),
				currentUsername,
				isAdmin);

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