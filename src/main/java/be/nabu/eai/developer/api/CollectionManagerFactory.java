package be.nabu.eai.developer.api;

import be.nabu.eai.repository.api.Entry;

public interface CollectionManagerFactory {
	public CollectionManager getCollectionManager(Entry entry);
}
