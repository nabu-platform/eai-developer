package be.nabu.eai.developer.collection;

import java.util.ArrayList;
import java.util.List;

import be.nabu.eai.developer.api.CollectionAction;
import be.nabu.eai.developer.api.CollectionManager;
import be.nabu.eai.developer.api.CollectionManagerFactory;
import be.nabu.eai.repository.api.Collection;
import be.nabu.eai.repository.api.Entry;

public class ProjectManagerFactory implements CollectionManagerFactory {

	@Override
	public CollectionManager getCollectionManager(Entry entry) {
		Collection collection = entry.getCollection();
		if (collection != null && "project".equals(collection.getType()) && "standard".equals(collection.getSubType())) {
			return new ProjectManager(entry);
		}
		else if (collection != null && "application".equals(collection.getType()) && "standard".equals(collection.getSubType())) {
			return new ApplicationManager(entry);
		}
		return null;
	}

	@Override
	public List<CollectionAction> getActionsFor(Entry entry) {
		List<CollectionAction> actions = new ArrayList<CollectionAction>();
		Collection collection = entry.getCollection();
		// for projects, you can add applications
		if (collection != null && collection.getType().equals("project")) {
			
		}
		return actions;
	}

}
