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

package be.nabu.eai.developer.managers.base;

import java.io.IOException;
import java.text.ParseException;

import javafx.scene.layout.AnchorPane;
import be.nabu.eai.developer.MainController;
import be.nabu.eai.developer.api.ArtifactGUIInstance;
import be.nabu.eai.developer.api.PortableArtifactGUIManager;
import be.nabu.eai.repository.api.ArtifactManager;
import be.nabu.eai.repository.api.Entry;
import be.nabu.libs.artifacts.api.Artifact;

abstract public class BasePortableGUIManager<T extends Artifact, I extends ArtifactGUIInstance> extends BaseGUIManager<T, I> implements PortableArtifactGUIManager<T> {

	public BasePortableGUIManager(String name, Class<T> artifactClass, ArtifactManager<T> artifactManager) {
		super(name, artifactClass, artifactManager);
	}

	@SuppressWarnings("unchecked")
	final protected T display(MainController controller, AnchorPane pane, Entry entry) throws IOException, ParseException {
		T artifact = (T) entry.getNode().getArtifact();
		display(controller, pane, artifact);
		return artifact;
	}
}
