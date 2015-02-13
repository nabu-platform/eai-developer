package be.nabu.eai.developer.managers.util;

import be.nabu.jfx.control.tree.MarshallableWithDescription;
import be.nabu.libs.types.api.Attribute;
import be.nabu.libs.types.api.ComplexType;
import be.nabu.libs.types.api.DefinedType;
import be.nabu.libs.types.api.Element;

public class ElementMarshallable implements MarshallableWithDescription<Element<?>> {
	@Override
	public String marshal(Element<?> element) {
		return (element instanceof Attribute ? "@" : "") + element.getName();
	}

	@Override
	public String getDescription(Element<?> element) {
		if (element.getType() instanceof DefinedType && element.getType() instanceof ComplexType) {
			return ((DefinedType) element.getType()).getId();
		}
		return null;
	}
}
