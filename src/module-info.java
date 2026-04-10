module FoundationsF25 {
	requires javafx.controls;
	requires java.sql;
	
	opens applicationMain to javafx.graphics, javafx.fxml;
	opens guiDiscussion to javafx.graphics, javafx.fxml;
	opens database;
	opens entityClasses;
	opens guiUserLogin to javafx.graphics, javafx.fxml;
	opens guiAdminHome to javafx.graphics, javafx.fxml;
	opens testCases;
}
