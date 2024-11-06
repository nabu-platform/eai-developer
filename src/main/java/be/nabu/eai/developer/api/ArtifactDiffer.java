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

import javafx.scene.layout.AnchorPane;
import be.nabu.libs.artifacts.api.Artifact;

/**
 * This visualizes the difference between two artifacts in the target pane
 * The boolean return indicates whether there is even a diff to be shown or not.
 * If the items are in sync (however that is defined), you should return "false" and the pane will not be shown
 * 
 * So basically if return == true: show pane
 */
public interface ArtifactDiffer<T extends Artifact> {
	public boolean diff(T original, T other, AnchorPane target);
	public Class<T> getArtifactClass();
}
