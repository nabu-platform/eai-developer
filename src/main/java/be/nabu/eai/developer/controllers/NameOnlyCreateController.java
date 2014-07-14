package be.nabu.eai.developer.controllers;

import java.net.URL;
import java.util.ResourceBundle;

import be.nabu.eai.developer.api.Component;
import be.nabu.eai.developer.api.Controller;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

public class NameOnlyCreateController implements Initializable, Controller {

	@FXML
	private Button btnCreate, btnCancel;
	
	@FXML
	private TextField txtName;
	
	private Stage stage;
	
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		// do nothing
		btnCancel.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent arg0) {
				stage.close();
			}
		});
	}
	public Button getBtnCreate() {
		return btnCreate;
	}
	public Button getBtnCancel() {
		return btnCancel;
	}
	public TextField getTxtName() {
		return txtName;
	}
	@Override
	public <C extends Controller, T extends Control> Component<C, T> getComponent(String name) {
		return null;
	}
	@Override
	public void setStage(Stage stage) {
		this.stage = stage;
	}
	public Stage getStage() {
		return stage;
	}
}
