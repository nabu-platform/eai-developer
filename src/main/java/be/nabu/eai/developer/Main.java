package be.nabu.eai.developer;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

	public static void main(String...args) {
		launch(args);
	}

	@Override
	public void start(Stage stage) throws Exception {
		FXMLLoader loader = new FXMLLoader();
		loader.setLocation(Thread.currentThread().getContextClassLoader().getResource("main.fxml"));
		loader.load();
			
		Parent root = loader.getRoot();
		stage.setScene(new Scene(root));
		stage.setTitle("Flexinator");
		stage.show();
	}

}
