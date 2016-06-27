package be.nabu.eai.developer;

import java.io.InputStream;
import java.util.Arrays;
import java.util.LinkedHashSet;

import be.nabu.eai.developer.managers.util.SimpleProperty;
import be.nabu.eai.developer.managers.util.SimplePropertyUpdater;
import be.nabu.eai.developer.util.EAIDeveloperUtils;
import be.nabu.eai.server.ServerConnection;
import be.nabu.libs.property.api.Property;
import be.nabu.libs.types.base.ValueImpl;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class Main extends Application {

	private MainController controller;

	public static void main(String...args) {
		launch(args);
	}

	@Override
	public void start(Stage stage) throws Exception {
		FXMLLoader loader = new FXMLLoader();
		loader.setLocation(Thread.currentThread().getContextClassLoader().getResource("developer.fxml"));
		loader.load();
		
		controller = loader.getController();
		controller.setStage(stage);
		Parent root = loader.getRoot();
		Scene scene = new Scene(root);
		stage.initStyle(StageStyle.DECORATED);
		scene.getStylesheets().add(Thread.currentThread().getContextClassLoader().getResource("style.css").toExternalForm());
		stage.setScene(scene);
		stage.setTitle("Nabu Developer");
		stage.setMaximized(true);
		stage.setResizable(true);
		InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream("icon.png");
		try {
			stage.getIcons().add(new Image(stream));
		}
		finally {
			stream.close();
		}
		stage.show();
		
		draw(controller);
	}
	
	@Override
	public void stop() throws Exception {
		controller.close();
		super.stop();
	}

	public static void draw(MainController controller) {
		SimpleProperty<String> serverProperty = new SimpleProperty<String>("server", String.class, true);
		SimpleProperty<Integer> portProperty = new SimpleProperty<Integer>("port", Integer.class, true);
		final SimplePropertyUpdater updater = new SimplePropertyUpdater(true, new LinkedHashSet<Property<?>>(Arrays.asList(serverProperty, portProperty)), 
			new ValueImpl<String>(serverProperty, "localhost"),
			new ValueImpl<Integer>(portProperty, 5555)
		);
		EAIDeveloperUtils.buildPopup(controller, updater, "Connect", new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
				String host = updater.getValue("server");
				Integer port = updater.getValue("port");
				controller.connect(new ServerConnection(null, null, host, port));
			}
		});
	}
}
