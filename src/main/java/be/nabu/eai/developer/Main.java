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
		loader.setLocation(Thread.currentThread().getContextClassLoader().getResource("developer.fxml"));
		loader.load();
		
		MainController controller = loader.getController();
		controller.setStage(stage);
		Parent root = loader.getRoot();
		stage.setScene(new Scene(root));
		stage.setTitle("Nabu Developer");
		stage.show();
	}

}
