package be.nabu.eai.developer.managers.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import be.nabu.eai.developer.MainController.PropertyUpdater;
import be.nabu.libs.property.api.Property;
import be.nabu.libs.property.api.Value;
import be.nabu.libs.types.base.ValueImpl;
import be.nabu.libs.validator.api.ValidationMessage;

public class SimplePropertyUpdater implements PropertyUpdater {

	private Set<Property<?>> supported;
	private ObservableList<Value<?>> values;
	private boolean updatable;

	public SimplePropertyUpdater(boolean updatable, Set<Property<?>> supported, Value<?>...values) {
		this.updatable = updatable;
		this.supported = supported;
		this.values = FXCollections.observableArrayList(values);
	}
	@Override
	public Set<Property<?>> getSupportedProperties() {
		return supported;
	}

	@Override
	public Value<?>[] getValues() {
		return values.toArray(new Value[0]);
	}

	@Override
	public boolean canUpdate(Property<?> property) {
		return updatable;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public List<ValidationMessage> updateProperty(Property<?> property, Object value) {
		Iterator<Value<?>> iterator = values.iterator();
		while (iterator.hasNext()) {
			if (iterator.next().getProperty().equals(property)) {
				iterator.remove();
			}
		}
		values.add(new ValueImpl(property, value));
		return new ArrayList<ValidationMessage>();
	}
	
	public ObservableList<Value<?>> valuesProperty() {
		return values;
	}
	
	@SuppressWarnings("unchecked")
	public <T> T getValue(String name) {
		for (Value<?> value : values) {
			if (value.getProperty().getName().equals(name)) {
				return (T) value.getValue();
			}
		}
		return null;
	}
}