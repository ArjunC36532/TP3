package guiDiscussion;

import java.util.List;
import entityClasses.Post;
import entityClasses.Reply;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

/*******
 * <p> Title: ViewPostDetail Class </p>
 * 
 * <p> Description: Window displaying post details and all replies. Action buttons for
 * Create Reply, Update/Delete Post (if author), Update/Delete Reply (if reply author). </p>
 * 
 * <p> Copyright: Arjun Chaudhary © 2026 </p>
 * 
 * @author Arjun Chaudhary
 * @version 1.00	2026-02-17 Initial implementation
 */
public class ViewPostDetail {

	private static Stage stage;
	private static VBox repliesBox;
	private static Post currentPost;
	private static String currentUsername;
	private static boolean isAdmin;

	/**
	 * Display the post detail window for the given post.
	 * owner: parent stage; post: the post to show; username: current user; admin: whether current user is admin.
	 * onCloseRefresh: called when window closes or post is deleted, to refresh parent post list.
	 */
	public static void display(Stage owner, Post post, String username, boolean admin, Runnable onCloseRefresh) {
		if (post == null) return;
		currentPost = post;
		currentUsername = username;
		isAdmin = admin;
		stage = new Stage();
		stage.initOwner(owner);
		stage.setTitle("Post: " + post.getTitle());
		ControllerPostDetail.init(stage, post, username, admin, onCloseRefresh);

		VBox root = new VBox(15);
		root.setPadding(new Insets(20));

		if (post.isDeleted()) {
			Label warn = new Label("This post has been deleted.");
			warn.setStyle("-fx-text-fill: orange; -fx-font-weight: bold;");
			root.getChildren().add(warn);
		}

		Label labelTitle = new Label(post.getTitle());
		labelTitle.setFont(Font.font("Arial", FontWeight.BOLD, 18));
		root.getChildren().add(labelTitle);

		Label labelMeta = new Label("By " + post.getAuthor() + " | Thread: " + post.getThread() + " | ID: " + post.getPostID());
		labelMeta.setFont(Font.font("Arial", 12));
		root.getChildren().add(labelMeta);

		Label labelBody = new Label(post.getBody());
		labelBody.setWrapText(true);
		labelBody.setMaxWidth(600);
		root.getChildren().add(labelBody);

		// Post action buttons
		javafx.scene.layout.HBox postButtons = new javafx.scene.layout.HBox(10);
		Button btnCreateReply = new Button("Create Reply");
		btnCreateReply.setOnAction(_ -> {
			if (ViewCreateReply.display(stage, currentPost, currentUsername)) {
				refreshReplies();
				if (onCloseRefresh != null) onCloseRefresh.run();
			}
		});
		postButtons.getChildren().add(btnCreateReply);
		if (ControllerPostDetail.canEditPost()) {
			Button btnUpdatePost = new Button("Update Post");
			btnUpdatePost.setOnAction(_ -> {
				if (ViewUpdatePost.display(stage, currentPost, currentUsername)) {
					currentPost = ModelPostDetail.getPost(currentPost.getPostID());
					if (currentPost != null) {
						labelTitle.setText(currentPost.getTitle());
						labelBody.setText(currentPost.getBody());
					}
					if (onCloseRefresh != null) onCloseRefresh.run();
				}
			});
			Button btnDeletePost = new Button("Delete Post");
			btnDeletePost.setOnAction(_ -> ControllerPostDetail.performDeletePost());
			postButtons.getChildren().addAll(btnUpdatePost, btnDeletePost);
		}
		root.getChildren().add(postButtons);

		root.getChildren().add(new Separator());

		Label labelReplies = new Label("Replies");
		labelReplies.setFont(Font.font("Arial", FontWeight.BOLD, 14));
		root.getChildren().add(labelReplies);

		repliesBox = new VBox(10);
		refreshReplies();
		ScrollPane scroll = new ScrollPane(repliesBox);
		scroll.setFitToWidth(true);
		scroll.setPrefHeight(250);
		root.getChildren().add(scroll);

		Button btnClose = new Button("Close");
		btnClose.setOnAction(_ -> {
			stage.close();
			if (onCloseRefresh != null) onCloseRefresh.run();
		});
		root.getChildren().add(btnClose);

		javafx.scene.Scene scene = new javafx.scene.Scene(root, 640, 500);
		stage.setScene(scene);
		stage.setOnHidden(_ -> {
			if (onCloseRefresh != null) onCloseRefresh.run();
		});
		stage.show();
	}

	private static void refreshReplies() {
		repliesBox.getChildren().clear();
		if (currentPost == null) return;
		List<Reply> replies = ModelPostDetail.getReplies(currentPost.getPostID());
		if (replies.isEmpty()) {
			Label empty = new Label("No replies yet. Be the first to reply!");
			empty.setStyle("-fx-text-fill: gray;");
			repliesBox.getChildren().add(empty);
			return;
		}
		for (Reply r : replies) {
			VBox card = new VBox(5);
			card.setStyle("-fx-border-color: lightgray; -fx-border-width: 1; -fx-padding: 8;");
			Label authorLabel = new Label("By " + r.getAuthor());
			authorLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
			Label bodyLabel = new Label(r.getBody());
			bodyLabel.setWrapText(true);
			card.getChildren().add(authorLabel);
			card.getChildren().add(bodyLabel);
			if (ControllerPostDetail.canEditReply(r)) {
				javafx.scene.layout.HBox rButtons = new javafx.scene.layout.HBox(5);
				Button btnUpdate = new Button("Update");
				btnUpdate.setOnAction(_ -> {
					if (ViewUpdateReply.display(stage, r, currentUsername)) {
						refreshReplies();
						ControllerPostDetail.notifyRefresh();
					}
				});
				Button btnDelete = new Button("Delete");
				btnDelete.setOnAction(_ -> {
					if (ControllerPostDetail.performDeleteReply(r)) refreshReplies();
				});
				rButtons.getChildren().addAll(btnUpdate, btnDelete);
				card.getChildren().add(rButtons);
			}
			repliesBox.getChildren().add(card);
		}
	}
}
