package guiDiscussion;

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
 * <p> Title: ViewCreatePost Class </p>
 * 
 * <p> Description: Dialog for creating a new post. Form fields: Title, Body, Thread (default General).
 * Create and Cancel buttons. </p>
 * 
 * <p> Copyright: Arjun Chaudhary © 2026 </p>
 * 
 * @author Arjun Chaudhary
 * @version 1.00	2026-02-17 Initial implementation
 */
public class ViewCreatePost {

	private static Stage dialogStage;
	private static TextField textTitle;
	private static TextArea textBody;
	private static TextField textThread;
	private static Label labelError;
	private static boolean created = false;

	/**
	 * Display the Create Post dialog. Returns true if user created a post, false if cancelled.
	 */
	public static boolean display(Stage owner, String author) {
		created = false;
		dialogStage = new Stage();
		dialogStage.initModality(Modality.APPLICATION_MODAL);
		dialogStage.initOwner(owner);
		dialogStage.setTitle("Create Post");

		GridPane grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(20));

		Label labelTitle = new Label("Title:");
		labelTitle.setFont(Font.font("Arial", 14));
		textTitle = new TextField();
		textTitle.setPromptText("Enter post title");
		textTitle.setMinWidth(300);
		grid.add(labelTitle, 0, 0);
		grid.add(textTitle, 1, 0);

		Label labelBody = new Label("Body:");
		labelBody.setFont(Font.font("Arial", 14));
		textBody = new TextArea();
		textBody.setPromptText("Enter post content");
		textBody.setMinWidth(300);
		textBody.setMinHeight(120);
		textBody.setWrapText(true);
		grid.add(labelBody, 0, 1);
		grid.add(textBody, 1, 1);

		Label labelThread = new Label("Thread:");
		labelThread.setFont(Font.font("Arial", 14));
		textThread = new TextField("General");
		textThread.setPromptText("General if left empty");
		textThread.setMinWidth(300);
		grid.add(labelThread, 0, 2);
		grid.add(textThread, 1, 2);

		labelError = new Label();
		labelError.setStyle("-fx-text-fill: red;");
		grid.add(labelError, 1, 3);

		Button btnCreate = new Button("Create");
		Button btnCancel = new Button("Cancel");
		btnCreate.setOnAction(_ -> {
			labelError.setText("");
			if (ControllerCreatePost.performCreate(textTitle.getText(), textBody.getText(), textThread.getText(), author)) {
				created = true;
				dialogStage.close();
			}
		});
		btnCancel.setOnAction(_ -> dialogStage.close());
		HBox buttons = new HBox(10);
		buttons.getChildren().addAll(btnCreate, btnCancel);
		buttons.setAlignment(Pos.CENTER_RIGHT);
		grid.add(buttons, 1, 4);

		Scene scene = new Scene(grid);
		dialogStage.setScene(scene);
		dialogStage.showAndWait();
		return created;
	}
}
