package be.nabu.eai.developer.managers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import be.nabu.eai.developer.MainController;
import be.nabu.eai.developer.managers.base.BaseJAXBGUIManager;
import be.nabu.eai.developer.managers.util.ElementSelectionListener;
import be.nabu.eai.repository.artifacts.simpleType.SimpleTypeArtifact;
import be.nabu.eai.repository.artifacts.simpleType.SimpleTypeConfiguration;
import be.nabu.eai.repository.artifacts.simpleType.SimpleTypeManager;
import be.nabu.eai.repository.resources.RepositoryEntry;
import be.nabu.libs.property.api.Property;
import be.nabu.libs.property.api.Value;
import be.nabu.libs.types.api.DefinedSimpleType;
import be.nabu.libs.types.api.SimpleType;

public class SimpleTypeGUIManager extends BaseJAXBGUIManager<SimpleTypeConfiguration, SimpleTypeArtifact<Object>> {

	public SimpleTypeGUIManager() {
		super("Simple Type", new SimpleTypeManager().getArtifactClass(), new SimpleTypeManager(), SimpleTypeConfiguration.class);
	}

	@Override
	protected List<Property<?>> getCreateProperties() {
		List<Property<?>> properties = new ArrayList<Property<?>>();
		ElementSelectionListener.TypeProperty property = new ElementSelectionListener.TypeProperty();
		properties.add(property);
		return properties;
	}

	@Override
	protected SimpleTypeArtifact<Object> newInstance(MainController controller, RepositoryEntry entry, Value<?>... values) throws IOException {
		SimpleTypeArtifact<Object> instance = new SimpleTypeArtifact<Object>(entry.getId(), entry, entry.getRepository());
		for (Value<?> value : values) {
			if (value.getProperty() instanceof ElementSelectionListener.TypeProperty) {
				SimpleType<?> type = (SimpleType<?>) value.getValue();
				instance.getConfiguration().setParent(type instanceof DefinedSimpleType ? ((DefinedSimpleType<?>) type).getId() : type.getInstanceClass().getName());
			}
		}
		return instance;
	}

	@Override
	public <V> V getValue(SimpleTypeArtifact<Object> instance, Property<V> property) {
		if (property.getName().equals("properties")) {
			for (Property<?> supported : instance.getParent().getSupportedProperties()) {
				try {
					if (!instance.getConfiguration().getProperties().containsKey(supported.getName())) {
						instance.getConfiguration().getProperties().put(supported.getName(), null);
					}
				}
				catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		}
		return super.getValue(instance, property);
	}

	@Override
	public String getCategory() {
		return "Types";
	}
}
