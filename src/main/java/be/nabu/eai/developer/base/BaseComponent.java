/*
* Copyright (C) 2014 Alexander Verbruggen
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU Lesser General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public License
* along with this program. If not, see <https://www.gnu.org/licenses/>.
*/

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
