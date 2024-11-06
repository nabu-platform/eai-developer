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
import be.nabu.eai.repository.api.Repository;
import be.nabu.libs.artifacts.api.Artifact;

/**
 * This interface allows us to "merge" artifacts
 * For example suppose you want to deploy a jdbc connection.
 * The first time the source is the connection on dev and the target is empty (no corresponding artifact on target server)
 * The pane should visualize the fields the user needs to change in order for it to be deployable
 * Note that the source is already a _copy_ of the original source so it can be modified as needed.
 * 
 * On the second deployment, the target exists so the merger should retrieve the necessary fields from the target and merge it into the source
 * 
 * The boolean return value determines whether or not user interaction is required (in other words whether the pane is shown)
 * It may not be required if it can be automerged or if nothing has changed or if there is a generic artifact (e.g. jaxbartifact) that doesn't know at compile time whether it should be merged
 * 
 * If they are automerged, you should return false
 * So basically if return == true: show pane
 */
public interface ArtifactMerger<T extends Artifact> {
	public boolean merge(T source, T target, AnchorPane pane, Repository targetRepository);
	public Class<T> getArtifactClass();
}
