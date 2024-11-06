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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javafx.scene.layout.AnchorPane;
import be.nabu.eai.developer.MainController;
import be.nabu.eai.developer.api.ArtifactMerger;
import be.nabu.eai.developer.api.EnvironmentAwareProperty;
import be.nabu.eai.developer.managers.util.SimpleProperty;
import be.nabu.eai.developer.managers.util.SimplePropertyUpdater;
import be.nabu.eai.repository.api.Repository;
import be.nabu.libs.artifacts.api.Artifact;
import be.nabu.libs.property.api.Property;
import be.nabu.libs.property.api.Value;
import be.nabu.libs.types.base.ValueImpl;
import be.nabu.libs.validator.api.ValidationMessage;

public class JAXBArtifactMerger<T extends Artifact> implements ArtifactMerger<T> {

	private BasePropertyOnlyGUIManager<T, ?> manager;

	public JAXBArtifactMerger(BasePropertyOnlyGUIManager<T, ?> manager) {
		this.manager = manager;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public boolean merge(T source, T target, AnchorPane pane, Repository targetRepository) {
		Set<Property<?>> properties = new HashSet<Property<?>>();
		List<Value<?>> values = new ArrayList<Value<?>>();
		boolean refresh = false;
		for (Property property : manager.getModifiableProperties(source)) {
			if (property instanceof EnvironmentAwareProperty && ((EnvironmentAwareProperty<?>) property).isEnvironmentSpecific()) {
				properties.add(property);
				Object value = target == null ? manager.getValue(source, property) : manager.getValue(target, property);
				values.add(new ValueImpl(property, value));
				if (property instanceof SimpleProperty && ((SimpleProperty<?>) property).isList()) {
					refresh = true;
				}
				// already initially set the target value in the source!
				manager.setValue(source, property, value);
			}
		}
		if (!properties.isEmpty()) {
			MainController.getInstance().showProperties(new SimplePropertyUpdater(true, properties, values.toArray(new Value[0])) {
					@Override
					public List<ValidationMessage> updateProperty(Property property, Object value) {
						manager.setValue(source, property, value);
						return super.updateProperty(property, value);
					}
				}, 
				pane, 
				refresh, 
				targetRepository,
				true
			);
		}
		return !properties.isEmpty();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Class<T> getArtifactClass() {
		return (Class<T>) Artifact.class;
	}

}
