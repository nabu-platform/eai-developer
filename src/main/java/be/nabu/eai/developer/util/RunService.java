package be.nabu.eai.developer.util;

import java.util.LinkedHashMap;
import java.util.Map;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import be.nabu.eai.developer.MainController;
import be.nabu.libs.services.api.Service;
import be.nabu.libs.services.api.ServiceException;
import be.nabu.libs.services.api.ServiceInstance;
import be.nabu.libs.types.api.ComplexContent;
import be.nabu.libs.types.api.ComplexType;
import be.nabu.libs.types.api.Element;
import be.nabu.libs.types.api.Unmarshallable;

public class RunService {
	
	private Map<String, TextField> fields = new LinkedHashMap<String, TextField>();
	private Service service;
	
	public RunService(Service service) {
		this.service = service;
	}
	public void build(final MainController controller) {
		final Stage stage = new Stage();
		AnchorPane pane = new AnchorPane();
		VBox vbox = new VBox();
		pane.getChildren().add(vbox);
		buildInput(controller, null, service.getServiceInterface().getInputDefinition(), vbox);
		
		HBox buttons = new HBox();
		Button run = new Button("Run");
		run.addEventHandler(ActionEvent.ACTION, new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
				ServiceInstance instance = service.newInstance();
				try {
					ComplexContent result = instance.execute(controller.getRepository().newRuntime(null), buildInput());
					stage.hide();
					controller.showContent(result);
				}
				catch (ServiceException e) {
					// TODO: better handling
					throw new RuntimeException(e);
				}
			}
		});
		buttons.getChildren().add(run);
		vbox.getChildren().add(buttons);
		
		stage.initOwner(controller.getStage());
		stage.initModality(Modality.WINDOW_MODAL);
		Scene scene = new Scene(pane);
		stage.setWidth(500);
		stage.setHeight(500);
		stage.setScene(scene);
		stage.show();
	}
	
	private ComplexContent buildInput() {
		ComplexContent input = service.getServiceInterface().getInputDefinition().newInstance();
		for (String path : fields.keySet()) {
			input.set(path, fields.get(path).getText().isEmpty() ? null : fields.get(path).getText());
		}
		return input;
	}
	
	private void buildInput(MainController controller, String path, ComplexType type, Pane parent) {
		for (Element<?> child : type) {
			String childPath = path == null ? child.getName() : path + "/" + child.getName();
			HBox hbox = new HBox();
			hbox.getChildren().add(new Label(child.getName()));
			// list are not supported yet
			if (child.getType().isList(child.getProperties())) {
				// currently always allow you to fill in the first element
				// will need to provide a button to add second etc
				childPath += "[0]";
			}
			if (child.getType() instanceof ComplexType) {
				VBox vbox = new VBox();
				buildInput(controller, childPath, (ComplexType) child.getType(), vbox);
				hbox.getChildren().add(vbox);
				// TODO: complex simple types
			}
			// simple types
			else if (child.getType() instanceof Unmarshallable) {
				TextField textField = new TextField();
				fields.put(childPath, textField);
				hbox.getChildren().add(textField);
			}
			else {
				hbox.getChildren().add(new Label("Can not be unmarshalled"));
			}
			parent.getChildren().add(hbox);
		}
	}
}
