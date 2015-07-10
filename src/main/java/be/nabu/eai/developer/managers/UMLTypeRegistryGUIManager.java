package be.nabu.eai.developer.managers;

import be.nabu.eai.repository.managers.UMLRegistryManager;
import be.nabu.libs.types.uml.UMLRegistry;

public class UMLTypeRegistryGUIManager extends TypeRegistryGUIManager<UMLRegistry> {

	public UMLTypeRegistryGUIManager() {
		super(new UMLRegistryManager(), "UML Registry");
	}

}
