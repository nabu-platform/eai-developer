/*
* Copyright (C) 2014 Alexander Verbruggen
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU Lesser General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public License
* along with this program. If not, see <https://www.gnu.org/licenses/>.
*/

package be.nabu.eai.developer.util;

import be.nabu.eai.developer.api.InterfaceLister.InterfaceDescription;

public class InterfaceDescriptionImpl implements InterfaceDescription {
	private String category, name, iface;

	public InterfaceDescriptionImpl() {
		// autoconstruct
	}
	
	public InterfaceDescriptionImpl(String category, String name, String iface) {
		this.category = category;
		this.name = name;
		this.iface = iface;
	}

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
