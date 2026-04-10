package guiDiscussion;

import entityClasses.Reply;
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
 * <p> Title: ViewUpdateReply Class </p>
 * 
 * <p> Description: Dialog for updating an existing reply. Pre-populated with reply body. </p>
 * 
 * <p> Copyright: Arjun Chaudhary © 2026 </p>
 * 
 * @author Arjun Chaudhary
 * @version 1.00	2026-02-17 Initial implementation
 */
public class ViewUpdateReply {

	private static Stage dialogStage;
	private static TextArea textBody;
	private static boolean updated = false;
	private static String replyID;

	/**
	 * Display the Update Reply dialog. Returns true if user updated the reply.
	 */
	public static boolean display(Stage owner, Reply reply, String author) {
		if (reply == null) return false;
		updated = false;
		replyID = reply.getReplyID();
		dialogStage = new Stage();
		dialogStage.initModality(Modality.APPLICATION_MODAL);
		dialogStage.initOwner(owner);
		dialogStage.setTitle("Update Reply");

		GridPane grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(20));

		Label labelBody = new Label("Reply:");
		labelBody.setFont(Font.font("Arial", 14));
		textBody = new TextArea(reply.getBody());
		textBody.setMinWidth(350);
		textBody.setMinHeight(120);
		textBody.setWrapText(true);
		grid.add(labelBody, 0, 0);
		grid.add(textBody, 1, 0);

		Button btnUpdate = new Button("Update");
		Button btnCancel = new Button("Cancel");
		btnUpdate.setOnAction(_ -> {
			if (ControllerUpdateReply.performUpdate(replyID, textBody.getText(), author)) {
				updated = true;
				dialogStage.close();
			}
		});
		btnCancel.setOnAction(_ -> dialogStage.close());
		HBox buttons = new HBox(10);
		buttons.getChildren().addAll(btnUpdate, btnCancel);
		buttons.setAlignment(Pos.CENTER_RIGHT);
		grid.add(buttons, 1, 1);

		Scene scene = new Scene(grid);
		dialogStage.setScene(scene);
		dialogStage.showAndWait();
		return updated;
	}
}
