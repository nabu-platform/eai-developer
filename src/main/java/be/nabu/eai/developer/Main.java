package be.nabu.eai.developer;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

//TODO: when adding an artifact repository > scan!
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
		Scene scene = new Scene(root);
		scene.getStylesheets().add(Thread.currentThread().getContextClassLoader().getResource("style.css").toExternalForm());
		stage.setScene(scene);
		stage.setTitle("Nabu Developer");
		stage.show();
		stage.setMaximized(true);
		
		ServerConnection.draw(controller);
	}

}
