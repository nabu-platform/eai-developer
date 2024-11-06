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

import java.io.IOException;
import java.util.List;

import be.nabu.libs.artifacts.api.Artifact;
import be.nabu.libs.validator.api.Validation;

public interface ArtifactGUIInstance {
	public String getId();
	public List<Validation<?>> save() throws IOException;
	public boolean hasChanged();
	public void setChanged(boolean changed);
	
	/**
	 * The GUI instances are sometimes returned synchronously but they are actually created asynchronously
	 * This should be set to true once the instance is ready
	 */
	public boolean isReady();
	public boolean isEditable();
	
	// whether or not we can detach this artifact gui instance
	public default boolean isDetachable() {
		return true;
	}
	
	public default boolean requiresPropertiesPane() {
		return false;
	}
	
	public Artifact getArtifact();
}
