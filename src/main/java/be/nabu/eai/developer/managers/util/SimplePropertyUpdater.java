package be.nabu.eai.developer.managers.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import be.nabu.eai.developer.MainController.PropertyUpdaterWithSource;
import be.nabu.eai.repository.api.Repository;
import be.nabu.libs.property.api.Enumerated;
import be.nabu.libs.property.api.Property;
import be.nabu.libs.property.api.Value;
import be.nabu.libs.types.CollectionHandlerFactory;
import be.nabu.libs.types.api.CollectionHandlerProvider;
import be.nabu.libs.types.base.ValueImpl;
import be.nabu.libs.validator.api.ValidationMessage;

public class SimplePropertyUpdater implements PropertyUpdaterWithSource {

	private Set<Property<?>> supported;
	private ObservableList<Value<?>> values;
	private boolean updatable;
	private Map<Property<?>, List<Property<?>>> propertyIndexes = new HashMap<Property<?>, List<Property<?>>>();
	private String sourceId;
	private Repository repository;

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
				Object value = getValue(property.getName());
				int counter = 0;
				if (!propertyIndexes.containsKey(property)) {
					propertyIndexes.put(property, new ArrayList());
				}
				else {
					propertyIndexes.get(property).clear();
				}
				if (value != null) {
					CollectionHandlerProvider handler = CollectionHandlerFactory.getInstance().getHandler().getHandler(value.getClass());
					for (Object index : handler.getIndexes(value)) {
						SimpleProperty newProperty;
						if (property instanceof Enumerated) {
							newProperty = new EnumeratedSimpleProperty(property.getName() + "[" + index + "]", property.getValueClass(), false);
							((EnumeratedSimpleProperty) newProperty).addEnumeration(((Enumerated) property).getEnumerations());
						}
						else {
							newProperty = new SimpleProperty(property.getName() + "[" + index + "]", property.getValueClass(), false);
						}
						newProperty.setEnvironmentSpecific(((SimpleProperty) property).isEnvironmentSpecific());
						newProperty.setFilter(((SimpleProperty) property).getFilter());
						newProperty.setAdvanced(((SimpleProperty) property).isAdvanced());
						newProperty.setDescription(((SimpleProperty) property).getDescription());
						newProperty.setTitle(((SimpleProperty) property).getTitle());
						newProperty.setInput(((SimpleProperty) property).isList());
						newProperty.setShow(((SimpleProperty) property).getShow());
						newProperty.setHide(((SimpleProperty) property).getHide());
						newProperty.setGroup(((SimpleProperty) property).getGroup());
						newProperty.setDefaultValue(((SimpleProperty) property).getDefaultValue());
						newProperty.setHiddenCalculator(((SimpleProperty) property).getHiddenCalculator());
						propertyIndexes.get(property).add(newProperty);
						counter++;
					}
				}
				// if not a fixed list, add a trailing field so it can be populated
				// note that it has to be numeric
				if (!((SimpleProperty) property).isFixedList() && !(value instanceof Map)) {
					SimpleProperty newProperty;
					if (property instanceof Enumerated) {
						newProperty = new EnumeratedSimpleProperty(property.getName() + "[" + counter + "]", property.getValueClass(), false);
						((EnumeratedSimpleProperty) newProperty).addEnumeration(((Enumerated) property).getEnumerations());
					}
					else {
						newProperty = new SimpleProperty(property.getName() + "[" + counter + "]", property.getValueClass(), false);
					}
					newProperty.setAdvanced(((SimpleProperty) property).isAdvanced());
					newProperty.setDescription(((SimpleProperty) property).getDescription());
					newProperty.setTitle(((SimpleProperty) property).getTitle());
					newProperty.setFilter(((SimpleProperty) property).getFilter());
					newProperty.setInput(((SimpleProperty) property).isList());
					newProperty.setShow(((SimpleProperty) property).getShow());
					newProperty.setHide(((SimpleProperty) property).getHide());
					newProperty.setGroup(((SimpleProperty) property).getGroup());
					newProperty.setDefaultValue(((SimpleProperty) property).getDefaultValue());
					newProperty.setHiddenCalculator(((SimpleProperty) property).getHiddenCalculator());
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
				if (value.getValue() != null) {
					CollectionHandlerProvider handler = CollectionHandlerFactory.getInstance().getHandler().getHandler(value.getValue().getClass());
					int i = 0;
					for (Object index : handler.getIndexes(value.getValue())) {
						values.add(new ValueImpl(propertyIndexes.get(value.getProperty()).get(i++), handler.get(value.getValue(), index)));
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
			String index = property.getName().replaceAll(".*\\[([^\\]]+)\\].*", "$1");
			for (Property<?> listProperty : propertyIndexes.keySet()) {
				if (propertyIndexes.get(listProperty).contains(property)) {
					Value<?> currentValue = null;
					Iterator<Value<?>> iterator = values.iterator();
					while(iterator.hasNext()) {
						Value<?> next = iterator.next();
						if (next.getProperty().equals(listProperty)) {
							currentValue = next;
							break;
						}
					}
					Object collection = currentValue == null || currentValue.getValue() == null ? new ArrayList() : currentValue.getValue();
					CollectionHandlerProvider handler = CollectionHandlerFactory.getInstance().getHandler().getHandler(collection.getClass());
					
					handler.set(collection, handler.unmarshalIndex(index), value);
					
					if (collection instanceof Collection) {
						Iterator listIterator = ((Collection) collection).iterator();
						while (listIterator.hasNext()) {
							if (listIterator.next() == null) {
								listIterator.remove();
							}
						}
					}
					return updateProperty(listProperty, collection);
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
	@Override
	public Repository getRepository() {
		return repository;
	}
	public void setRepository(Repository repository) {
		this.repository = repository;
	}
}