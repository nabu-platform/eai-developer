package be.nabu.eai.developer.managers;

import be.nabu.eai.repository.managers.XMLSchemaRegistryManager;
import be.nabu.libs.types.xml.XMLSchema;

public class XMLSchemaTypeRegistryGUIManager extends TypeRegistryGUIManager<XMLSchema> {

	public XMLSchemaTypeRegistryGUIManager() {
		super(new XMLSchemaRegistryManager(), "XML Schema");
	}
	
	@Override
	public String getCategory() {
		return "Types";
	}
}
