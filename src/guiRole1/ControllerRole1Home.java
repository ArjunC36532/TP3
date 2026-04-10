package guiRole1;

import entityClasses.Post;
import guiDiscussion.ViewCreatePost;
import guiDiscussion.ViewPostDetail;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

/*******
 * <p> Title: ControllerRole1Home Class. </p>
 * 
 * <p> Description: The Java/FX-based Role 1 Home Page.  This class provides the controller
 * actions for the Student Discussion System and user account. </p>
 * 
 * <p> Copyright: Lynn Robert Carter © 2025 </p>
 * 
 * @author Lynn Robert Carter
 * @version 1.00		2025-08-17 Initial version
 * @version 2.00		2026-02-17 Discussion system CRUD actions
 */

public class ControllerRole1Home {

	/**
	 * Default constructor is not used.
	 */
	public ControllerRole1Home() {
	}

	protected static void performUpdate () {
		guiUserUpdate.ViewUserUpdate.displayUserUpdate(ViewRole1Home.theStage, ViewRole1Home.theUser);
	}	

	protected static void performLogout() {
		guiUserLogin.ViewUserLogin.displayUserLogin(ViewRole1Home.theStage);
	}
	
	protected static void performQuit() {
		System.exit(0);
	}

	protected static void performCreatePost() {
		String author = ViewRole1Home.theUser != null ? ViewRole1Home.theUser.getUserName() : "";
		if (ViewCreatePost.display(ViewRole1Home.theStage, author)) {
			ModelRole1Home.refreshPostList(null, null);
		}
	}

	protected static void performViewPost() {
		Post selected = ViewRole1Home.tableView_Posts.getSelectionModel().getSelectedItem();
		if (selected == null) {
			Alert alert = new Alert(AlertType.WARNING);
			alert.setTitle("No Selection");
			alert.setHeaderText("Select a post");
			alert.setContentText("Please select a post from the list to view.");
			alert.showAndWait();
			return;
		}
		String username = ViewRole1Home.theUser != null ? ViewRole1Home.theUser.getUserName() : "";
		ViewPostDetail.display(ViewRole1Home.theStage, selected, username, false, () -> ModelRole1Home.refreshPostList(null, null));
	}

	protected static void performSearchPosts() {
		String keyword = ViewRole1Home.text_SearchKeyword.getText();
		String thread = ViewRole1Home.combobox_ThreadFilter.getSelectionModel().getSelectedItem();
		if (keyword != null && keyword.trim().isEmpty()) keyword = null;
		ModelRole1Home.refreshPostList(keyword, thread);
	}

	protected static void performRefresh() {
		ModelRole1Home.refreshPostList(null, null);
	}
}
