package be.nabu.eai.developer.collection;

import be.nabu.eai.developer.api.CollectionManager;
import be.nabu.eai.repository.api.Entry;

public class ApplicationManager implements CollectionManager {

	private Entry entry;

	public ApplicationManager(Entry entry) {
		this.entry = entry;
	}

}
