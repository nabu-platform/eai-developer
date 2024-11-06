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
	public default void initialize(Entry newApplication, String version) {
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
