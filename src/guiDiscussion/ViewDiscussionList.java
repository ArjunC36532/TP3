package guiDiscussion;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import entityClasses.Post;
import applicationMain.FoundationsMain;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;
import javafx.stage.Stage;

/**
 * Shared discussion list window: post table, search, create, view, refresh.
 * Used by Admin to open the discussion system in a separate window.
 */
public class ViewDiscussionList {

	private static Stage stage;
	private static TableView<Post> tableView_Posts;
	private static TextField text_SearchKeyword;
	private static ComboBox<String> combobox_ThreadFilter;
	private static Label label_EmptyMessage;
	private static ObservableList<Post> postsList;
	private static String currentUsername;
	private static boolean isAdmin;

	public static void display(Stage owner, String username, boolean admin) {
		currentUsername = username != null ? username : "";
		isAdmin = admin;
		stage = new Stage();
		stage.initOwner(owner);
		stage.setTitle("Student Discussion System");
		double w = 780;
		double h = 520;
		Pane root = new Pane();
		postsList = FXCollections.observableArrayList();
		label_EmptyMessage = new Label("No posts available. Create the first post!");
		label_EmptyMessage.setStyle("-fx-text-fill: gray;");

		Label label_Search = new Label("Search:");
		label_Search.setFont(Font.font("Arial", 14));
		label_Search.setLayoutX(20);
		label_Search.setLayoutY(15);
		text_SearchKeyword = new TextField();
		text_SearchKeyword.setPromptText("Keyword");
		text_SearchKeyword.setLayoutX(85);
		text_SearchKeyword.setLayoutY(12);
		text_SearchKeyword.setPrefWidth(150);
		combobox_ThreadFilter = new ComboBox<>();
		combobox_ThreadFilter.getItems().add("<All Threads>");
		combobox_ThreadFilter.getSelectionModel().select(0);
		combobox_ThreadFilter.setLayoutX(245);
		combobox_ThreadFilter.setLayoutY(12);
		combobox_ThreadFilter.setPrefWidth(120);
		Button button_Search = new Button("Search");
		button_Search.setLayoutX(375);
		button_Search.setLayoutY(10);
		button_Search.setOnAction(_ -> doSearch());
		Button button_CreatePost = new Button("Create Post");
		button_CreatePost.setLayoutX(20);
		button_CreatePost.setLayoutY(48);
		button_CreatePost.setOnAction(_ -> {
			if (ViewCreatePost.display(stage, currentUsername)) refreshList(null, null);
		});
		Button button_ViewPost = new Button("View Post");
		button_ViewPost.setLayoutX(140);
		button_ViewPost.setLayoutY(48);
		button_ViewPost.setOnAction(_ -> viewPost());
		Button button_Refresh = new Button("Refresh");
		button_Refresh.setLayoutX(250);
		button_Refresh.setLayoutY(48);
		button_Refresh.setOnAction(_ -> refreshList(null, null));

		tableView_Posts = new TableView<>();
		tableView_Posts.setLayoutX(20);
		tableView_Posts.setLayoutY(90);
		tableView_Posts.setPrefSize(740, 380);
		tableView_Posts.setPlaceholder(label_EmptyMessage);
		TableColumn<Post, String> colId = new TableColumn<>("Post ID");
		colId.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getPostID()));
		colId.setPrefWidth(120);
		TableColumn<Post, String> colTitle = new TableColumn<>("Title");
		colTitle.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getTitle()));
		colTitle.setPrefWidth(200);
		TableColumn<Post, String> colAuthor = new TableColumn<>("Author");
		colAuthor.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getAuthor()));
		colAuthor.setPrefWidth(100);
		TableColumn<Post, String> colThread = new TableColumn<>("Thread");
		colThread.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getThread()));
		colThread.setPrefWidth(100);
		TableColumn<Post, String> colReplies = new TableColumn<>("Replies");
		colReplies.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(String.valueOf(FoundationsMain.replyStorage.getReplyCount(c.getValue().getPostID()))));
		colReplies.setPrefWidth(80);
		tableView_Posts.getColumns().addAll(colId, colTitle, colAuthor, colThread, colReplies);
		tableView_Posts.setItems(postsList);

		Button button_Close = new Button("Close");
		button_Close.setLayoutX(20);
		button_Close.setLayoutY(480);
		button_Close.setOnAction(_ -> stage.close());

		root.getChildren().addAll(
			label_Search, text_SearchKeyword, combobox_ThreadFilter, button_Search,
			button_CreatePost, button_ViewPost, button_Refresh,
			tableView_Posts, button_Close
		);
		Scene scene = new Scene(root, w, h);
		stage.setScene(scene);
		refreshList(null, null);
		stage.show();
	}

	private static void doSearch() {
		String keyword = text_SearchKeyword.getText();
		String thread = combobox_ThreadFilter.getSelectionModel().getSelectedItem();
		if (keyword != null && keyword.trim().isEmpty()) keyword = null;
		refreshList(keyword, thread);
	}

	private static void viewPost() {
		Post selected = tableView_Posts.getSelectionModel().getSelectedItem();
		if (selected == null) {
			javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.WARNING);
			alert.setTitle("No Selection");
			alert.setHeaderText("Select a post");
			alert.setContentText("Please select a post from the list to view.");
			alert.showAndWait();
			return;
		}
		ViewPostDetail.display(stage, selected, currentUsername, isAdmin, ViewDiscussionList::refreshFromCallback);
	}

	private static void refreshFromCallback() {
		refreshList(null, null);
	}

	private static void refreshList(String keyword, String thread) {
		List<Post> posts;
		if (keyword != null && !keyword.trim().isEmpty()) {
			String threadFilter = (thread == null || thread.trim().isEmpty() || "<All Threads>".equals(thread)) ? null : thread.trim();
			posts = FoundationsMain.postStorage.searchPosts(keyword.trim(), threadFilter);
		} else {
			posts = FoundationsMain.postStorage.getAllPosts();
			Set<String> threads = new LinkedHashSet<>();
			threads.add("General");
			for (Post p : posts) threads.add(p.getThread());
			List<String> threadList = new ArrayList<>(threads);
			threadList.sort(String.CASE_INSENSITIVE_ORDER);
			List<String> comboItems = new ArrayList<>();
			comboItems.add("<All Threads>");
			comboItems.addAll(threadList);
			combobox_ThreadFilter.getItems().setAll(comboItems);
			combobox_ThreadFilter.getSelectionModel().select(0);
		}
		postsList.clear();
		postsList.addAll(posts);
		if (posts.isEmpty()) {
			label_EmptyMessage.setText(
				(keyword != null && !keyword.trim().isEmpty())
					? "No posts match your search criteria."
					: "No posts available. Create the first post!");
		}
	}
}
