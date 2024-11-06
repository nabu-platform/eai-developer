/*
* Copyright (C) 2014 Alexander Verbruggen
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU Lesser General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public License
* along with this program. If not, see <https://www.gnu.org/licenses/>.
*/

package be.nabu.eai.developer.api;

import be.nabu.eai.repository.api.Entry;
import javafx.scene.Node;

/**
 * Collection managers are considered stateful
 */
public interface CollectionManager {
	public Entry getEntry();
	// whether this collection manager can open that entry (which is presumably a collection...) in a new tab
	// this allows you to get a closer look at the collection
	public default boolean hasDetailView() {
		return false;
	}
	// if so, open it
	public default Node getDetailView() {
		return null;
	}
	public default boolean hasThinDetailView() {
		return false;
	}
	public default Node getThinDetailView() {
		return null;
	}
	public default boolean hasIcon() {
		return false;
	}
	// get a small icon to show for example in the repository browser
	public default Node getIcon() {
		return null;
	}
	// there are "overviews" of collections, this is a more terse listing of the collection manager
	public default boolean hasSummaryView() {
		return false;
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
	// a large icon can be used to display it in other wizards. e.g. if you want to be able to select a database connection when creating a crud, it has to be listed
	public default boolean hasLargeIcon() {
		return false;
	}
	public default Node getLargeIcon() {
		return null;
	}
}
