package guiDiscussion;

import applicationMain.FoundationsMain;
import guiTools.InputValidator;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

/*******
 * <p> Title: ControllerUpdateReply Class </p>
 * 
 * <p> Description: Controller for the Update Reply dialog. Validates body, calls
 * replyStorage.updateReply(), handles authorization errors. </p>
 * 
 * <p> Copyright: Arjun Chaudhary © 2026 </p>
 * 
 * @author Arjun Chaudhary
 * @version 1.00	2026-02-17 Initial implementation
 */
public class ControllerUpdateReply {

	/**
	 * Update a reply. Returns true if successful. Shows alerts for errors.
	 */
	public static boolean performUpdate(String replyID, String newBody, String author) {
		String bodyError = InputValidator.isValidPostContent(newBody, InputValidator.MAX_POST_BODY_LENGTH);
		if (bodyError != null) {
			showError("Body: " + bodyError);
			return false;
		}
		String sanitizedBody = InputValidator.sanitizePostContent(newBody, InputValidator.MAX_POST_BODY_LENGTH);
		String err = FoundationsMain.replyStorage.updateReply(replyID, sanitizedBody, author);
		if (err != null) {
			showError(err);
			return false;
		}
		Alert success = new Alert(AlertType.INFORMATION);
		success.setTitle("Success");
		success.setHeaderText("Reply Updated");
		success.setContentText("The reply has been updated successfully.");
		success.showAndWait();
		return true;
	}

	private static void showError(String message) {
		Alert alert = new Alert(AlertType.ERROR);
		alert.setTitle("Error");
		alert.setHeaderText("Cannot Update Reply");
		alert.setContentText(message);
		alert.showAndWait();
	}
}
