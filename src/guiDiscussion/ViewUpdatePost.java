package guiDiscussion;

import entityClasses.Post;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;

/*******
 * <p> Title: ViewUpdatePost Class </p>
 * 
 * <p> Description: Dialog for updating an existing post. Pre-populated with post data.
 * Update and Cancel buttons. </p>
 * 
 * <p> Copyright: Arjun Chaudhary © 2026 </p>
 * 
 * @author Arjun Chaudhary
 * @version 1.00	2026-02-17 Initial implementation
 */
public class ViewUpdatePost {

	private static Stage dialogStage;
	private static TextField textTitle;
	private static TextArea textBody;
	private static Label labelError;
	private static boolean updated = false;
	private static String postID;

	/**
	 * Display the Update Post dialog. Returns true if user updated the post, false if cancelled.
	 */
	public static boolean display(Stage owner, Post post, String author) {
		if (post == null) return false;
		updated = false;
		postID = post.getPostID();
		dialogStage = new Stage();
		dialogStage.initModality(Modality.APPLICATION_MODAL);
		dialogStage.initOwner(owner);
		dialogStage.setTitle("Update Post");

		GridPane grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(20));

		Label labelTitle = new Label("Title:");
		labelTitle.setFont(Font.font("Arial", 14));
		textTitle = new TextField(post.getTitle());
		textTitle.setMinWidth(300);
		grid.add(labelTitle, 0, 0);
		grid.add(textTitle, 1, 0);

		Label labelBody = new Label("Body:");
		labelBody.setFont(Font.font("Arial", 14));
		textBody = new TextArea(post.getBody());
		textBody.setMinWidth(300);
		textBody.setMinHeight(120);
		textBody.setWrapText(true);
		grid.add(labelBody, 0, 1);
		grid.add(textBody, 1, 1);

		labelError = new Label();
		labelError.setStyle("-fx-text-fill: red;");
		grid.add(labelError, 1, 2);

		Button btnUpdate = new Button("Update");
		Button btnCancel = new Button("Cancel");
		btnUpdate.setOnAction(_ -> {
			labelError.setText("");
			if (ControllerUpdatePost.performUpdate(postID, textTitle.getText(), textBody.getText(), author)) {
				updated = true;
				dialogStage.close();
			}
		});
		btnCancel.setOnAction(_ -> dialogStage.close());
		HBox buttons = new HBox(10);
		buttons.getChildren().addAll(btnUpdate, btnCancel);
		buttons.setAlignment(Pos.CENTER_RIGHT);
		grid.add(buttons, 1, 3);

		Scene scene = new Scene(grid);
		dialogStage.setScene(scene);
		dialogStage.showAndWait();
		return updated;
	}
}
