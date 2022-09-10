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
