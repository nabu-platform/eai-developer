package be.nabu.eai.developer.collection;

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
		return null;
	}

}
