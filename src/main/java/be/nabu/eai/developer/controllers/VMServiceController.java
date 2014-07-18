package be.nabu.eai.developer.controllers;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Control;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import be.nabu.eai.developer.api.Component;
import be.nabu.eai.developer.api.Controller;
import be.nabu.libs.validator.api.ValidationMessage;

/**
 * For scrollpanes: on the scrollpane you need to set "fit left" and "fit right" in the "layout" tab of the editor
 * 		on the anchorpane in the scrollpane, you _need_ to set "use computed sizes" from the right click menu, or it won't detect changes
 *
 */
public class VMServiceController implements Initializable, Controller {

	private Stage stage;
	
	@FXML
	private Pane panService, panLeft, panRight, panMiddle, panInput, panOutput, panMap;
	
	@FXML
	private HBox hbxButtons;
	
	@Override
	public <C extends Controller, T extends Control> Component<C, T> getComponent( String name) {
		return null;
	}

	@Override
	public void setStage(Stage stage) {
		this.stage = stage;
	}

	@Override
	public void notify(ValidationMessage... messages) {
		// do nothing
	}

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {

	}

	public Stage getStage() {
		return stage;
	}

	public HBox getHbxButtons() {
		return hbxButtons;
	}

	public Pane getPanService() {
		return panService;
	}

	public Pane getPanLeft() {
		return panLeft;
	}

	public Pane getPanRight() {
		return panRight;
	}

	public Pane getPanMiddle() {
		return panMiddle;
	}

	public Pane getPanInput() {
		return panInput;
	}

	public Pane getPanOutput() {
		return panOutput;
	}

	public Pane getPanMap() {
		return panMap;
	}
}
