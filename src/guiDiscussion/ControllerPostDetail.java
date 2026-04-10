package guiDiscussion;

import java.util.List;
import entityClasses.Post;
import entityClasses.Reply;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

/*******
 * <p> Title: ControllerPostDetail Class </p>
 * 
 * <p> Description: Controller for the Post Detail view. Handles reply creation, post/reply
 * updates and deletions, confirmation dialogs. </p>
 * 
 * <p> Copyright: Arjun Chaudhary © 2026 </p>
 * 
 * @author Arjun Chaudhary
 * @version 1.00	2026-02-17 Initial implementation
 */
public class ControllerPostDetail {

	private static Stage theStage;
	private static Post thePost;
	private static String currentUsername;
	private static boolean isAdmin;
	private static Runnable onCloseRefresh;

	/**
	 * Initialize controller state when opening post detail.
	 */
	public static void init(Stage stage, Post post, String username, boolean admin, Runnable refreshCallback) {
		theStage = stage;
		thePost = post;
		currentUsername = username;
		isAdmin = admin;
		onCloseRefresh = refreshCallback;
	}

	public static Post getPost() { return thePost; }
	public static String getCurrentUsername() { return currentUsername; }
	public static boolean isAdmin() { return isAdmin; }
	public static Stage getStage() { return theStage; }

	/**
	 * Check if current user can edit/delete this post (author or admin).
	 */
	public static boolean canEditPost() {
		if (thePost == null) return false;
		return isAdmin || currentUsername != null && currentUsername.equals(thePost.getAuthor());
	}

	/**
	 * Check if current user can edit/delete this reply (author or admin).
	 */
	public static boolean canEditReply(Reply reply) {
		if (reply == null) return false;
		return isAdmin || (currentUsername != null && currentUsername.equals(reply.getAuthor()));
	}

	/**
	 * Delete post with confirmation. Returns true if deleted. For admin, bypass author check in storage by using post author.
	 */
	public static boolean performDeletePost() {
		if (thePost == null) return false;
		Alert confirm = new Alert(AlertType.CONFIRMATION);
		confirm.setTitle("Confirm Delete");
		confirm.setHeaderText("Delete Post");
		confirm.setContentText("Are you sure you want to delete this post?");
		if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return false;
		String authorToUse = canEditPost() ? thePost.getAuthor() : currentUsername;
		String err = ModelPostDetail.deletePost(thePost.getPostID(), authorToUse);
		if (err != null) {
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Error");
			alert.setHeaderText("Cannot Delete Post");
			alert.setContentText(err);
			alert.showAndWait();
			return false;
		}
		Alert success = new Alert(AlertType.INFORMATION);
		success.setTitle("Success");
		success.setContentText("Post has been deleted.");
		success.showAndWait();
		if (onCloseRefresh != null) onCloseRefresh.run();
		theStage.close();
		return true;
	}

	/**
	 * Delete reply with confirmation. For admin we need to allow delete - but storage checks author. So we must pass reply.getAuthor() when admin.
	 */
	public static boolean performDeleteReply(Reply reply) {
		if (reply == null) return false;
		Alert confirm = new Alert(AlertType.CONFIRMATION);
		confirm.setTitle("Confirm Delete");
		confirm.setHeaderText("Delete Reply");
		confirm.setContentText("Are you sure you want to delete this reply?");
		if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return false;
		String authorToUse = canEditReply(reply) ? reply.getAuthor() : currentUsername;
		String err = ModelPostDetail.deleteReply(reply.getReplyID(), authorToUse);
		if (err != null) {
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Error");
			alert.setHeaderText("Cannot Delete Reply");
			alert.setContentText(err);
			alert.showAndWait();
			return false;
		}
		Alert success = new Alert(AlertType.INFORMATION);
		success.setTitle("Success");
		success.setContentText("Reply has been deleted.");
		success.showAndWait();
		if (onCloseRefresh != null) onCloseRefresh.run();
		return true;
	}

	public static void notifyRefresh() {
		if (onCloseRefresh != null) onCloseRefresh.run();
	}
}
