package guiDiscussion;

import entityClasses.Post;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;

/*******
 * <p> Title: ViewCreateReply Class </p>
 * 
 * <p> Description: Dialog for creating a reply. TextArea for body, post context shown.
 * Warning label if post is deleted. </p>
 * 
 * <p> Copyright: Arjun Chaudhary © 2026 </p>
 * 
 * @author Arjun Chaudhary
 * @version 1.00	2026-02-17 Initial implementation
 */
public class ViewCreateReply {

	private static Stage dialogStage;
	private static TextArea textBody;
	private static boolean created = false;
	private static String postID;

	/**
	 * Display the Create Reply dialog. Returns true if user created a reply.
	 */
	public static boolean display(Stage owner, Post post, String author) {
		if (post == null) return false;
		created = false;
		postID = post.getPostID();
		dialogStage = new Stage();
		dialogStage.initModality(Modality.APPLICATION_MODAL);
		dialogStage.initOwner(owner);
		dialogStage.setTitle("Create Reply");

		GridPane grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(20));

		Label labelContext = new Label("Replying to: " + post.getTitle() + " (ID: " + post.getPostID() + ")");
		labelContext.setFont(Font.font("Arial", 12));
		labelContext.setWrapText(true);
		grid.add(labelContext, 0, 0, 2, 1);

		if (post.isDeleted()) {
			Label labelWarning = new Label("Warning: The original post has been deleted.");
			labelWarning.setStyle("-fx-text-fill: orange;");
			grid.add(labelWarning, 0, 1, 2, 1);
		}

		Label labelBody = new Label("Reply:");
		labelBody.setFont(Font.font("Arial", 14));
		textBody = new TextArea();
		textBody.setPromptText("Enter your reply");
		textBody.setMinWidth(350);
		textBody.setMinHeight(120);
		textBody.setWrapText(true);
		grid.add(labelBody, 0, 2);
		grid.add(textBody, 1, 2);

		Button btnCreate = new Button("Create");
		Button btnCancel = new Button("Cancel");
		btnCreate.setOnAction(_ -> {
			if (ControllerCreateReply.performCreate(postID, textBody.getText(), author)) {
				created = true;
				dialogStage.close();
			}
		});
		btnCancel.setOnAction(_ -> dialogStage.close());
		HBox buttons = new HBox(10);
		buttons.getChildren().addAll(btnCreate, btnCancel);
		buttons.setAlignment(Pos.CENTER_RIGHT);
		grid.add(buttons, 1, 3);

		Scene scene = new Scene(grid);
		dialogStage.setScene(scene);
		dialogStage.showAndWait();
		return created;
	}
}
