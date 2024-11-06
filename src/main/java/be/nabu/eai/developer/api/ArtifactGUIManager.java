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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;

import javafx.scene.image.ImageView;
import be.nabu.eai.developer.MainController;
import be.nabu.eai.repository.api.ArtifactManager;
import be.nabu.eai.repository.api.Entry;
import be.nabu.jfx.control.tree.TreeItem;
import be.nabu.libs.artifacts.api.Artifact;

// TODO: update create/view to work with Entry only instead of TreeItem<Entry>
// this can decouple it slightly further from however we get the entry
public interface ArtifactGUIManager<T extends Artifact> {
	public ArtifactManager<T> getArtifactManager();
	public String getArtifactName();
	public ImageView getGraphic();
	public Class<T> getArtifactClass();
	public ArtifactGUIInstance create(MainController controller, TreeItem<Entry> target) throws IOException;
	public ArtifactGUIInstance view(MainController controller, TreeItem<Entry> target) throws IOException, ParseException;
	default String getCategory() { return null; }
	
	public default ArtifactGUIInstance view(MainController controller, Entry target) throws IOException, ParseException {
		TreeItem<Entry> resolve = controller.getTree().resolve(target.getId().replace(".", "/"));
		if (resolve == null) {
			throw new FileNotFoundException("Can not find: " + target.getId());
		}
		else {
			return view(controller, resolve);
		}
	}
}
