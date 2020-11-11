package be.nabu.eai.developer.api;

import javafx.scene.Node;

/**
 * Collection managers are considered stateful
 */
public interface CollectionManager {
	// whether this collection manager can open that entry (which is presumably a collection...) in a new tab
	// this allows you to get a closer look at the collection
	public default boolean isOpenable() {
		return false;
	}
	// there are "overviews" of collections, this is a more terse listing of the collection manager
	public default boolean isListable() {
		return false;
	}
	public default boolean hasIcon() {
		return false;
	}
	// if so, open it
	public default void open() {
		// do nothing
	}
	// get the icon
	public default Node getIcon() {
		return null;
	}
	// create list view
	public default Node list() {
		return null;
	}
	// can add other combinations later like a create
}
