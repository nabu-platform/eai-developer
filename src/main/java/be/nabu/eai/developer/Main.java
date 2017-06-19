package be.nabu.eai.developer;

import java.io.InputStream;
import java.security.Principal;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Properties;

import be.nabu.eai.developer.managers.util.SimpleProperty;
import be.nabu.eai.developer.managers.util.SimplePropertyUpdater;
import be.nabu.eai.developer.util.EAIDeveloperUtils;
import be.nabu.eai.server.ServerConnection;
import be.nabu.libs.authentication.api.principals.BasicPrincipal;
import be.nabu.libs.property.api.Property;
import be.nabu.libs.types.base.ValueImpl;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class Main extends Application {

	private MainController controller;

	public static void main(String...args) {
		launch(args);
	}

	@Override
	public void start(Stage stage) throws Exception {
		setUserAgentStylesheet(STYLESHEET_MODENA);
		
		FXMLLoader loader = new FXMLLoader();
		loader.setLocation(Thread.currentThread().getContextClassLoader().getResource("developer.fxml"));
		loader.load();
		
		controller = loader.getController();
		controller.setStage(stage);
		Parent root = loader.getRoot();
		Scene scene = new Scene(root);
		
		scene.addEventHandler(KeyEvent.KEY_PRESSED,
			new EventHandler<KeyEvent>() {
				@Override
				public void handle(KeyEvent event) {
					if (event.isAltDown()) {
						event.consume();
					}
				}
			}
		);
	
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

	// TODO: refactor to first check if authentication is required, only pop up username/password window if it is
	public static void draw(MainController controller) {
		SimpleProperty<String> serverProperty = new SimpleProperty<String>("Server", String.class, true);
		SimpleProperty<Integer> portProperty = new SimpleProperty<Integer>("Port", Integer.class, true);
		SimpleProperty<String> usernameProperty = new SimpleProperty<String>("Username", String.class, false);
		SimpleProperty<String> passwordProperty = new SimpleProperty<String>("Password", String.class, false);
		passwordProperty.setPassword(true);
		Properties properties = MainController.getProperties();
		final SimplePropertyUpdater updater = new SimplePropertyUpdater(true, new LinkedHashSet<Property<?>>(Arrays.asList(serverProperty, portProperty, usernameProperty, passwordProperty)), 
			new ValueImpl<String>(serverProperty, properties.getProperty("last.server", "localhost")),
			new ValueImpl<Integer>(portProperty, Integer.parseInt(properties.getProperty("last.port", "5555"))),
			new ValueImpl<String>(usernameProperty, properties.getProperty("last.username"))
		);
		EAIDeveloperUtils.buildPopup(controller, updater, "Connect", new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
				String host = updater.getValue("Server");
				Integer port = updater.getValue("Port");
				String username = updater.getValue("Username");
				String password = updater.getValue("Password");
				properties.setProperty("last.server", host);
				properties.setProperty("last.port", port == null ? "5555" : port.toString());
				if (username == null || username.isEmpty()) {
					properties.remove("last.username");
				}
				else {
					properties.setProperty("last.username", username);
				}
				MainController.saveProperties();
				Principal principal = username == null ? null : new BasicPrincipal() {
					private static final long serialVersionUID = 1L;
					@Override
					public String getName() {
						return username;
					}
					@Override
					public String getPassword() {
						return password;
					}
				};
				controller.connect(new ServerConnection(null, principal, host, port));
			}
		});
	}
}
