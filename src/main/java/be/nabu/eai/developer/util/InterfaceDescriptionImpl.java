package be.nabu.eai.developer.util;

import be.nabu.eai.developer.api.InterfaceLister.InterfaceDescription;

public class InterfaceDescriptionImpl implements InterfaceDescription {
	private String category, name, iface;

	@Override
	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}

	@Override
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getInterface() {
		return iface;
	}
	public void setInterface(String iface) {
		this.iface = iface;
	}
}
