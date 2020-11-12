package be.nabu.eai.developer.api;

import java.util.ArrayList;
import java.util.List;

import be.nabu.eai.repository.api.Entry;
import javafx.scene.Node;

public interface CollectionManagerFactory {
	// manage an instance of a collection
	public CollectionManager getCollectionManager(Entry entry);
	// manage the available actions you have on another entry, for example a create jdbc on a project
	public default List<CollectionAction> getActionsFor(Entry entry) {
		return new ArrayList<CollectionAction>();
	}
}
