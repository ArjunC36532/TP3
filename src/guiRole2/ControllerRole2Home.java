package guiRole2;

import entityClasses.Post;
import guiDiscussion.ViewCreatePost;
import guiDiscussion.ViewPostDetail;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

/*******
 * <p> Title: ControllerRole2Home Class. </p>
 * 
 * <p> Description: The Java/FX-based Role 2 Home Page. Controller actions for the Student Discussion System. </p>
 * 
 * <p> Copyright: Lynn Robert Carter © 2025 </p>
 * @version 2.00		2026-02-17 Discussion system CRUD actions
 */

public class ControllerRole2Home {

	public ControllerRole2Home() {
	}

	protected static void performUpdate () {
		guiUserUpdate.ViewUserUpdate.displayUserUpdate(ViewRole2Home.theStage, ViewRole2Home.theUser);
	}	

	protected static void performLogout() {
		guiUserLogin.ViewUserLogin.displayUserLogin(ViewRole2Home.theStage);
	}
	
	protected static void performQuit() {
		System.exit(0);
	}

	protected static void performCreatePost() {
		String author = ViewRole2Home.theUser != null ? ViewRole2Home.theUser.getUserName() : "";
		if (ViewCreatePost.display(ViewRole2Home.theStage, author)) {
			ModelRole2Home.refreshPostList(null, null);
		}
	}

	protected static void performViewPost() {
		Post selected = ViewRole2Home.tableView_Posts.getSelectionModel().getSelectedItem();
		if (selected == null) {
			Alert alert = new Alert(AlertType.WARNING);
			alert.setTitle("No Selection");
			alert.setHeaderText("Select a post");
			alert.setContentText("Please select a post from the list to view.");
			alert.showAndWait();
			return;
		}
		String username = ViewRole2Home.theUser != null ? ViewRole2Home.theUser.getUserName() : "";
		ViewPostDetail.display(ViewRole2Home.theStage, selected, username, false, () -> ModelRole2Home.refreshPostList(null, null));
	}

	protected static void performSearchPosts() {
		String keyword = ViewRole2Home.text_SearchKeyword.getText();
		String thread = ViewRole2Home.combobox_ThreadFilter.getSelectionModel().getSelectedItem();
		if (keyword != null && keyword.trim().isEmpty()) keyword = null;
		ModelRole2Home.refreshPostList(keyword, thread);
	}

	protected static void performRefresh() {
		ModelRole2Home.refreshPostList(null, null);
	}
}
