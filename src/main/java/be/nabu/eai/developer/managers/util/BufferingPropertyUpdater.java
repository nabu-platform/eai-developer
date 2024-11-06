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

package be.nabu.eai.developer.managers.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import be.nabu.eai.developer.MainController.PropertyUpdater;
import be.nabu.libs.property.api.Property;
import be.nabu.libs.property.api.Value;
import be.nabu.libs.validator.api.ValidationMessage;

public class BufferingPropertyUpdater implements PropertyUpdater {

	private PropertyUpdater original;
	private List<Value<?>> values;

	public BufferingPropertyUpdater(PropertyUpdater original) {
		this.original = original;
	}
	
	@Override
	public Set<Property<?>> getSupportedProperties() {
		return original.getSupportedProperties();
	}

	@Override
	public Value<?>[] getValues() {
		if (values == null) {
			values = new ArrayList<Value<?>>(Arrays.asList(original.getValues()));
		}
		return values.toArray(new Value[values.size()]);
	}

	@Override
	public boolean canUpdate(Property<?> property) {
		return original.canUpdate(property);
	}

	@Override
	public List<ValidationMessage> updateProperty(Property<?> property, Object value) {
		// TODO Auto-generated method stub
		return null;
	}

	public void commit() {
		// TODO
	}

	@Override
	public boolean isMandatory(Property<?> property) {
		return original.isMandatory(property);
	}
}
