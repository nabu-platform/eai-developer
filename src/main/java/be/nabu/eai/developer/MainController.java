package be.nabu.eai.developer;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import be.nabu.eai.developer.api.Component;
import be.nabu.eai.developer.api.Controller;
import be.nabu.eai.repository.EAIRepository;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Control;
import javafx.scene.layout.AnchorPane;

public class MainController implements Initializable, Controller {

	@FXML
	private AnchorPane ancLeft, ancMiddle, ancRight;
	
	private Map<String, Component<MainController, ?>> components = new HashMap<String, Component<MainController, ?>>();
	
	private EAIRepository repository;
	
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		// TODO Auto-generated method stub
		
	}

	@SuppressWarnings("unchecked")
	@Override
	public <C extends Controller, T extends Control> Component<C, T> getComponent(String id) {
		return (Component<C, T>) components.get(id);
	}

	@Override
	public void addError(String... errors) {
		for (String error : errors) {
			System.err.println(error);
		}
	}
	
}
