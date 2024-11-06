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

import be.nabu.jfx.control.tree.MarshallableWithDescription;
import be.nabu.libs.property.api.Value;
import be.nabu.libs.types.api.Attribute;
import be.nabu.libs.types.api.Element;
import be.nabu.libs.types.properties.CommentProperty;

public class ElementMarshallable implements MarshallableWithDescription<Element<?>> {
	@Override
	public String marshal(Element<?> element) {
		return (element instanceof Attribute ? "@" : "") + element.getName();
	}

	@Override
	public String getDescription(Element<?> element) {
		Value<String> property = element.getProperty(CommentProperty.getInstance());
		if (property != null && property.getValue() != null && !property.getValue().trim().isEmpty()) {
			return property.getValue();
		}
		// we want only info that can help you build business logic, types are not it
//		if (element.getType() instanceof DefinedType && element.getType() instanceof ComplexType) {
//			return ((DefinedType) element.getType()).getId();
//		}
		return null;
	}
}
