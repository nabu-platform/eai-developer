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

package be.nabu.eai.developer.api;

import be.nabu.libs.validator.api.ValidationMessage;
import javafx.scene.control.Control;
import javafx.stage.Stage;

public interface Controller {
	public <C extends Controller, T extends Control> Component<C, T> getComponent(String name);
	public void setStage(Stage stage);
	public void notify(ValidationMessage...messages);
}
