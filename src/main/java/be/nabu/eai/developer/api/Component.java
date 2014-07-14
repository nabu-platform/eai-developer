package be.nabu.eai.developer.api;

import javafx.scene.control.Control;

public interface Component<C extends Controller, T extends Control> {
	public void initialize(C controller, T control);
	public T getControl();
}