package guiAdminHome;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ViewManageInvitations {

    private static TableView<InvitationData> table;

    public static void display(Stage ownerStage) {
        Stage stage = new Stage();
        stage.initOwner(ownerStage);
        stage.setTitle("Outstanding Invitations");

        table = new TableView<>();
        refreshTable();

        // Columns
        TableColumn<InvitationData, String> codeCol =
                new TableColumn<>("Code");
        codeCol.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getCode()));

        TableColumn<InvitationData, String> emailCol =
                new TableColumn<>("Email");
        emailCol.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getEmail()));

        TableColumn<InvitationData, String> roleCol =
                new TableColumn<>("Role");
        roleCol.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getRole()));

        // Delete button column
        TableColumn<InvitationData, Void> deleteCol =
                new TableColumn<>("Action");

        deleteCol.setCellFactory(col -> new TableCell<>() {
            private final Button deleteBtn = new Button("Delete");

            {
                deleteBtn.setOnAction(e -> {
                	InvitationData row = getTableView().getItems().get(getIndex());
                    applicationMain.FoundationsMain.database
                            .deleteInvitation(row.getCode());
                    refreshTable();
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : deleteBtn);
            }
        });

        table.getColumns().addAll(codeCol, emailCol, roleCol, deleteCol);

        VBox root = new VBox(10, table);
        root.setPrefSize(600, 350);

        stage.setScene(new Scene(root));
        stage.show();
    }

    private static void refreshTable() {
        ObservableList<InvitationData> data =
                FXCollections.observableArrayList(
                        applicationMain.FoundationsMain.database.getInvitationData()
                );
        table.setItems(data);
    }
}
