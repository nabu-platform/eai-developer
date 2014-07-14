package be.nabu.eai.developer.api;

import javafx.scene.control.Control;
import javafx.stage.Stage;

public interface Controller {
	public <C extends Controller, T extends Control> Component<C, T> getComponent(String name);
	public void setStage(Stage stage);
}
