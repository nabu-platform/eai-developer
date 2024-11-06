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

package be.nabu.eai.developer.impl;

import java.io.IOException;

import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import be.nabu.eai.developer.api.ResourceManager;
import be.nabu.eai.developer.api.ResourceManagerInstance;
import be.nabu.libs.resources.api.Resource;
import be.nabu.libs.resources.api.ReadableResource;
import be.nabu.utils.io.IOUtils;
import be.nabu.utils.io.api.ByteBuffer;
import be.nabu.utils.io.api.ReadableContainer;

public class GraphicResourceManager implements ResourceManager {

	@Override
	public ResourceManagerInstance manage(Resource resource) {
		if (resource.getContentType().startsWith("image/")) {
			return new GraphicResourceManagerInstance(resource);
		}
		return null;
	}

	public static class GraphicResourceManagerInstance implements ResourceManagerInstance {

		private Resource resource;

		public GraphicResourceManagerInstance(Resource resource) {
			this.resource = resource;
		}

		@Override
		public void save() {
			// don't save
		}

		@Override
		public Node getView() {
			try {
				ScrollPane pane = new ScrollPane();
				ReadableContainer<ByteBuffer> readable = ((ReadableResource) resource).getReadable();
				try {
					pane.setContent(new ImageView(new Image(IOUtils.toInputStream(readable))));
				}
				finally {
					readable.close();
				}
				return pane;
			}
			catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		
	}
}
