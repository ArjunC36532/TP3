package guiDiscussion;

import entityClasses.Reply;
import applicationMain.FoundationsMain;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

/*******
 * <p> Title: ControllerCreateReply Class </p>
 * 
 * <p> Description: Controller for the Create Reply dialog. Validates body, creates Reply,
 * calls replyStorage.addReply(), shows success/error/warning alerts. </p>
 * 
 * <p> Copyright: Arjun Chaudhary © 2026 </p>
 * 
 * @author Arjun Chaudhary
 * @version 1.00	2026-02-17 Initial implementation
 */
public class ControllerCreateReply {

	/**
	 * Create a new reply. Returns true if successful. Shows alerts for errors; warning for deleted post.
	 */
	public static boolean performCreate(String postID, String body, String author) {
		if (body == null || body.trim().isEmpty()) {
			showError("Reply body cannot be null, empty, or whitespace-only.");
			return false;
		}
		if (author == null || author.trim().isEmpty()) {
			showError("Author cannot be empty.");
			return false;
		}
		if (postID == null || postID.trim().isEmpty()) {
			showError("Post ID is required.");
			return false;
		}
		Reply reply = new Reply(body.trim(), author, postID);
		String result = FoundationsMain.replyStorage.addReply(reply);
		if (result != null && result.startsWith("Error:")) {
			showError(result);
			return false;
		}
		if (result != null && result.startsWith("Warning:")) {
			Alert warn = new Alert(AlertType.WARNING);
			warn.setTitle("Warning");
			warn.setHeaderText("Post Deleted");
			warn.setContentText(result);
			warn.showAndWait();
		}
		Alert success = new Alert(AlertType.INFORMATION);
		success.setTitle("Success");
		success.setHeaderText("Reply Created");
		success.setContentText("Your reply has been added successfully.");
		success.showAndWait();
		return true;
	}

	private static void showError(String message) {
		Alert alert = new Alert(AlertType.ERROR);
		alert.setTitle("Error");
		alert.setHeaderText("Cannot Create Reply");
		alert.setContentText(message);
		alert.showAndWait();
	}
}
