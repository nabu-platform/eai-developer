package be.nabu.eai.developer.managers.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import be.nabu.eai.developer.MainController.PropertyUpdaterWithSource;
import be.nabu.libs.property.api.Property;
import be.nabu.libs.property.api.Value;
import be.nabu.libs.types.base.ValueImpl;
import be.nabu.libs.validator.api.ValidationMessage;

public class SimplePropertyUpdater implements PropertyUpdaterWithSource {

	private Set<Property<?>> supported;
	private ObservableList<Value<?>> values;
	private boolean updatable;
	private Map<Property<?>, List<Property<?>>> propertyIndexes = new HashMap<Property<?>, List<Property<?>>>();
	private String sourceId;

	public SimplePropertyUpdater(boolean updatable, Set<Property<?>> supported, Value<?>...values) {
		this.updatable = updatable;
		this.supported = supported;
		this.values = FXCollections.observableArrayList(values);
	}
	@Override
	public Set<Property<?>> getSupportedProperties() {
		return explodeProperties();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Set<Property<?>> explodeProperties() {
		Set<Property<?>> properties = new LinkedHashSet<Property<?>>();
		for (Property<?> property : supported) {
			if (property instanceof SimpleProperty && ((SimpleProperty) property).isList()) {
				if (!propertyIndexes.containsKey(property)) {
					propertyIndexes.put(property, new ArrayList());
				}
				List list = getValue(property.getName());
				int amountOfProperties = list == null ? 1 : list.size() + 1;
				for (int i = ((List) propertyIndexes.get(property)).size(); i < amountOfProperties; i++) {
					SimpleProperty newProperty = new SimpleProperty(property.getName() + "[" + i + "]", property.getValueClass(), false);
					newProperty.setFilter(((SimpleProperty) property).getFilter());
					propertyIndexes.get(property).add(newProperty);
				}
				properties.addAll(propertyIndexes.get(property));
			}
			else {
				properties.add(property);
			}
		}
		return properties;
	}
	@Override
	public Value<?>[] getValues() {
		return explodeValues().toArray(new Value[0]);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Set<Value<?>> explodeValues() {
		Set<Value<?>> values = new LinkedHashSet<Value<?>>();
		for (Value<?> value : this.values) {
			if (propertyIndexes.containsKey(value.getProperty())) {
				List list = (List) value.getValue();
				if (list != null) {
					for (int i = 0; i < list.size(); i++) {
						values.add(new ValueImpl(propertyIndexes.get(value.getProperty()).get(i), list.get(i)));
					}
				}
			}
			else {
				values.add(value);
			}
		}
		return values;
	}
	@Override
	public boolean canUpdate(Property<?> property) {
		return updatable;
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List<ValidationMessage> updateProperty(Property<?> property, Object value) {
		System.out.println("setting " + property + " = " + value);
		if (supported.contains(property)) {
			Iterator<Value<?>> iterator = values.iterator();
			while (iterator.hasNext()) {
				if (iterator.next().getProperty().equals(property)) {
					iterator.remove();
				}
			}
			values.add(new ValueImpl(property, value));
		}
		// this will merge for list properties
		else {
			for (Property<?> listProperty : propertyIndexes.keySet()) {
				int index = propertyIndexes.get(listProperty).indexOf(property);
				if (index >= 0) {
					Value<?> currentValue = null;
					Iterator<Value<?>> iterator = values.iterator();
					while(iterator.hasNext()) {
						Value<?> next = iterator.next();
						if (next.getProperty().equals(listProperty)) {
							currentValue = next;
							break;
						}
					}
					List list = currentValue == null ? new ArrayList() : (List) currentValue.getValue();
					if (value == null) {
						list.remove(index);
					}
					else if (list.size() <= index) {
						list.add(value);
					}
					else {
						list.add(index, value);
					}
					Iterator listIterator = list.iterator();
					while (listIterator.hasNext()) {
						if (listIterator.next() == null) {
							listIterator.remove();
						}
					}
					return updateProperty(listProperty, list);
				}
			}
		}
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
	@Override
	public boolean isMandatory(Property<?> property) {
		return property instanceof SimpleProperty && ((SimpleProperty<?>) property).isMandatory();
	}
	
	@Override
	public String getSourceId() {
		return sourceId;
	}
	public void setSourceId(String sourceId) {
		this.sourceId = sourceId;
	}
	
}