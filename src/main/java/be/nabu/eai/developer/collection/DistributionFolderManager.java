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

package be.nabu.eai.developer.collection;

import be.nabu.eai.developer.MainController;
import be.nabu.eai.developer.api.CollectionManager;
import be.nabu.eai.repository.api.Entry;
import javafx.scene.Node;

public class DistributionFolderManager implements CollectionManager {

	private Entry entry;

	public DistributionFolderManager(Entry entry) {
		this.entry = entry;
	}
	
	@Override
	public Entry getEntry() {
		return entry;
	}

	@Override
	public Node getIcon() {
		String iconName = "folder-utility.png";
		if (entry.getName().equals("nabu")) {
			iconName = "icon-small.png";
		}
		return MainController.loadFixedSizeGraphic(iconName, 16, 25);
	}

	@Override
	public boolean hasIcon() {
		return true;
	}
	
}
