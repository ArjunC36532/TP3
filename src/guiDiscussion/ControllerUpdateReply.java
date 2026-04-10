package guiDiscussion;

import applicationMain.FoundationsMain;
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
		if (newBody == null || newBody.trim().isEmpty()) {
			showError("Updated reply body cannot be null, empty, or whitespace-only.");
			return false;
		}
		String err = FoundationsMain.replyStorage.updateReply(replyID, newBody.trim(), author);
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
