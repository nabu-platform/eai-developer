package be.nabu.eai.developer.base;

import javafx.scene.control.Control;
import be.nabu.eai.developer.api.Component;
import be.nabu.eai.developer.api.Controller;

abstract public class BaseComponent<C extends Controller, T extends Control> implements Component<C, T> {

	private C controller;
	private T control;
	
	@Override
	public Component<C, T> initialize(C controller, T control) {
		this.controller = controller;
		this.control = control;
		initialize(control);
		return this;
	}

	@Override
	public T getControl() {
		return control;
	}
	
	protected C getController() {
		return controller;
	}
	
	abstract protected void initialize(T control);
}
