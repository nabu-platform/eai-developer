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

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import be.nabu.libs.property.api.Enumerated;

public class EnumeratedSimpleProperty<T> extends SimpleProperty<T> implements Enumerated<T> {

	private Set<T> enumerations = new LinkedHashSet<T>();
	
	public EnumeratedSimpleProperty(String name, Class<T> clazz, boolean isMandatory) {
		super(name, clazz, isMandatory);
	}

	@Override
	public Set<T> getEnumerations() {
		return enumerations;
	}

	public void addEnumeration(Collection<T> values) {
		enumerations.addAll(values);
	}

	@SuppressWarnings("unchecked")
	public void addAll(T...values) {
		addEnumeration(Arrays.asList(values));
	}
	
	@Override
	public EnumeratedSimpleProperty<T> clone() {
		EnumeratedSimpleProperty<T> property = new EnumeratedSimpleProperty<T>(getName(), getValueClass(), isMandatory());
		property.setInput(isInput());
		property.setPassword(isPassword());
		property.setLarge(isLarge());
		property.setEvaluatable(isEvaluatable());
		property.setFixedList(isFixedList());
		property.setEnvironmentSpecific(isEnvironmentSpecific());
		property.setFilter(getFilter());
		property.addEnumeration(enumerations);
		property.setShow(getShow());
		property.setHide(getHide());
		property.setHiddenCalculator(getHiddenCalculator());
		return property;
	}
}
