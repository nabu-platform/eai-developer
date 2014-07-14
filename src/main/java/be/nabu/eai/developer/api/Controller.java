package be.nabu.eai.developer.api;

import javafx.scene.control.Control;

public interface Controller {
	public <C extends Controller, T extends Control> Component<C, T> getComponent(String name);
	public void addError(String...errors);
}
