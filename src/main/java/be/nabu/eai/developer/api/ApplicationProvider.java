package be.nabu.eai.developer.api;

import be.nabu.eai.repository.api.Entry;
import javafx.scene.Node;

// application providers are _not_ stateful
public interface ApplicationProvider {

	// the subtype of application created by this one
	public String getSubType();
	
	public default String suggestName(Entry entry) {
		return null;
	}
	
	// get an icon to show when creating a new one
	public Node getLargeCreateIcon();
	
	// for in the project overview
	// if you return nothing, a default view will be used
	public default Node getSummaryView(Entry entry) {
		return null;
	}
	
	// initialize a new application in the target entry
	// by default nothing happens
	public default void initialize(Entry newApplication) {
		// do nothing
	}
	
	public default String getMediumIcon() {
		return null;
	}
	
	public default String getSmallIcon() {
		return null;
	}
	
	public default String getLargeIcon() {
		return null;
	}
	
}
