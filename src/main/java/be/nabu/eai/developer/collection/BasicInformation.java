package be.nabu.eai.developer.collection;

import be.nabu.libs.types.api.annotation.ComplexTypeDescriptor;

@ComplexTypeDescriptor(propOrder = {"name", "version"})
public class BasicInformation {
	private String name, version;

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
}
