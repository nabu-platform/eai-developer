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
}
