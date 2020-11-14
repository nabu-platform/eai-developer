package be.nabu.eai.developer.api;

import javafx.scene.Node;

/**
 * Collection managers are considered stateful
 */
public interface CollectionManager {
	// whether this collection manager can open that entry (which is presumably a collection...) in a new tab
	// this allows you to get a closer look at the collection
	public default boolean hasDetailView() {
		return false;
	}
	// there are "overviews" of collections, this is a more terse listing of the collection manager
	public default boolean hasSummaryView() {
		return false;
	}
	public default boolean hasIcon() {
		return false;
	}
	// if so, open it
	public default Node getDetailView() {
		return null;
	}
	// get the icon
	public default Node getIcon() {
		return null;
	}
	// create list view
	public default Node getSummaryView() {
		return null;
	}
	// a hook for when we are showing the detail (you can subscribe to changes etc)
	public default void showDetail() {
		// do nothing
	}
	// a hook for when we are hiding the detail (you can unsubscribe to changes etc)
	public default void hideDetail() {
		// do nothing
	}
}
