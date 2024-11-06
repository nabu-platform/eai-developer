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

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
// add a "categorizer" or something? want to divide actions into categories? or the actions providers just need to make sure it is clear where the actions belong?
// can add sections (like connectors) that don't quite exist yet?
public interface CollectionAction {
	
	public Node getNode();
	public EventHandler<ActionEvent> getEventHandler();
	public default EntryAcceptor getEntryAcceptor() {
		return null;
	}
}
