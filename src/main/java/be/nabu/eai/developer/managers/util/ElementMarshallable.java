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
