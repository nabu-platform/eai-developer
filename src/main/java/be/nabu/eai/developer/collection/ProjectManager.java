package be.nabu.eai.developer.collection;

import be.nabu.eai.developer.MainController;
import be.nabu.eai.developer.api.CollectionManager;
import be.nabu.eai.repository.api.Entry;
import javafx.scene.Node;
import javafx.scene.control.Tab;

public class ProjectManager implements CollectionManager {

	private Entry entry;

	public ProjectManager(Entry entry) {
		this.entry = entry;
	}

	@Override
	public boolean isOpenable() {
		return true;
	}

	@Override
	public void open() {
		Tab newTab = MainController.getInstance().newTab("Project: " + entry.getCollection().getName());
	}

	@Override
	public boolean hasIcon() {
		return true;
	}

	@Override
	public Node getIcon() {
		return MainController.loadFixedSizeGraphic("folder-project.png", 16, 25);
	}
	
}
