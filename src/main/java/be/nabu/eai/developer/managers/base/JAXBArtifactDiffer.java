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

package be.nabu.eai.developer.managers.base;

import java.util.Collection;

import javafx.geometry.HPos;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import be.nabu.eai.developer.api.ArtifactDiffer;
import be.nabu.libs.artifacts.api.Artifact;
import be.nabu.libs.converter.ConverterFactory;
import be.nabu.libs.converter.api.Converter;
import be.nabu.libs.property.api.Property;

public class JAXBArtifactDiffer<T extends Artifact> implements ArtifactDiffer<T> {

	private BasePropertyOnlyGUIManager<T, ?> manager;
	private Converter converter = ConverterFactory.getInstance().getConverter();

	public JAXBArtifactDiffer(BasePropertyOnlyGUIManager<T, ?> manager) {
		this.manager = manager;
	}
	
	@Override
	public boolean diff(T original, T other, AnchorPane target) {
		Collection<Property<?>> properties = manager.getModifiableProperties(original);
		GridPane grid = new GridPane();
		
		int row = 0;
		for (Property<?> property : properties) {
			Object originalValue = manager.getValue(original, property);
			Object otherValue = manager.getValue(other, property);
			if (originalValue instanceof Artifact) {
				originalValue = ((Artifact) originalValue).getId();
			}
			if (otherValue instanceof Artifact) {
				otherValue = ((Artifact) otherValue).getId();
			}
			// if they are different, add an entry in the grid
			if ((originalValue == null && otherValue != null)
					|| (originalValue != null && otherValue == null)
					|| (originalValue != null && !originalValue.equals(otherValue))) {
				grid.setVgap(5);
				grid.setHgap(10);
				String originalStringValue = originalValue == null || converter.canConvert(originalValue.getClass(), String.class) ? converter.convert(originalValue, String.class) : originalValue.toString();
				String otherStringValue = otherValue == null || converter.canConvert(otherValue.getClass(), String.class) ? converter.convert(otherValue, String.class) : otherValue.toString();
				Label name = new Label(property.getName());
				grid.add(name, 0, row);
				GridPane.setHalignment(name, HPos.RIGHT);
				
				TextInputControl originalTextField = originalStringValue != null && originalStringValue.contains("\n") ? new TextArea(originalStringValue) : new TextField(originalStringValue);
				TextInputControl otherTextField = otherStringValue != null && otherStringValue.contains("\n") ? new TextArea(otherStringValue) : new TextField(otherStringValue);
				originalTextField.setDisable(true);
				otherTextField.setDisable(true);
				grid.add(originalTextField, 1, row);
				grid.add(otherTextField, 2, row);
				row++;
			}
		}
		
		AnchorPane.setLeftAnchor(grid, 0d);
		AnchorPane.setRightAnchor(grid, 0d);
		target.getChildren().add(grid);
		return row > 0;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Class<T> getArtifactClass() {
		return (Class<T>) Artifact.class;
	}

}
