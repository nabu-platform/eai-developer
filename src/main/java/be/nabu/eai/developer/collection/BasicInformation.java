package be.nabu.eai.developer.collection;

import be.nabu.libs.types.api.annotation.ComplexTypeDescriptor;

@ComplexTypeDescriptor(propOrder = {"name"})
public class BasicInformation {
	private String name;

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
}
