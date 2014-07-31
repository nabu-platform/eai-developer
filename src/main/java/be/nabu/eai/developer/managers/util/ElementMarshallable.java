package be.nabu.eai.developer.managers.util;

import be.nabu.jfx.control.tree.Marshallable;
import be.nabu.libs.types.api.Attribute;
import be.nabu.libs.types.api.Element;

public class ElementMarshallable implements Marshallable<Element<?>> {
	@Override
	public String marshal(Element<?> element) {
		return (element instanceof Attribute ? "@" : "") + element.getName();
	}
}
