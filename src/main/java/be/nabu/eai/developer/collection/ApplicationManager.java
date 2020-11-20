package be.nabu.eai.developer.collection;

import be.nabu.eai.developer.api.CollectionManager;
import be.nabu.eai.repository.api.Entry;
import javafx.scene.Node;

// the application manager gets the same view as the project manager!
public class ApplicationManager implements CollectionManager {

	private Entry entry;
	private ProjectManager projectManager;

	public ApplicationManager(Entry entry) {
		this.entry = entry;
	}

	@Override
	public boolean hasDetailView() {
		return true;
	}

	@Override
	public Node getDetailView() {
		if (projectManager == null) {
			projectManager = new ProjectManager(entry, false);
		}
		return projectManager.getDetailView();
	}

	@Override
	public void showDetail() {
		projectManager.showDetail();
	}

	@Override
	public void hideDetail() {
		projectManager.hideDetail();
	}

	@Override
	public Entry getEntry() {
		return entry;
	}
	
}
