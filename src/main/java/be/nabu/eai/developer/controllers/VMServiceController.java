package be.nabu.eai.developer.controllers;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Control;
import javafx.scene.control.Tab;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import be.nabu.eai.developer.api.Component;
import be.nabu.eai.developer.api.Controller;
import be.nabu.libs.validator.api.ValidationMessage;

public class VMServiceController implements Initializable, Controller {

	private Stage stage;
	
	@FXML
	private VBox vbxService;
	
	@FXML
	private HBox hbxButtons;
	
	@FXML
	private Tab tabInterface, tabMap;
	
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
		// TODO Auto-generated method stub
		
	}

	public Stage getStage() {
		return stage;
	}
	
	public Tab getTabInterface() {
		return tabInterface;
	}

	public Tab getTabMap() {
		return tabMap;
	}

	public VBox getVbxService() {
		return vbxService;
	}

	public HBox getHbxButtons() {
		return hbxButtons;
	}
}
