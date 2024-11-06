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

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Node;
import be.nabu.eai.developer.MainController;
import be.nabu.eai.developer.api.ResourceManager;
import be.nabu.eai.developer.api.ResourceManagerInstance;
import be.nabu.jfx.control.ace.AceEditor;
import be.nabu.libs.resources.api.ReadableResource;
import be.nabu.libs.resources.api.Resource;
import be.nabu.libs.resources.api.WritableResource;
import be.nabu.utils.io.IOUtils;
import be.nabu.utils.io.api.ByteBuffer;
import be.nabu.utils.io.api.ReadableContainer;
import be.nabu.utils.io.api.WritableContainer;

public class TextResourceManager implements ResourceManager {

	@Override
	public ResourceManagerInstance manage(Resource resource) {
		if (resource.getContentType().startsWith("text/") || resource.getContentType().equals("application/xml")
			 || resource.getContentType().equals("application/json")
			 || resource.getContentType().equals("application/javascript")) {
			return new TextResourceManagerInstance(resource);
		}
		return null;
	}
	
	public static class TextResourceManagerInstance implements ResourceManagerInstance {

		private Resource resource;
		private AceEditor editor;

		public TextResourceManagerInstance(Resource resource) {
			this.resource = resource;
		}

		@Override
		public void save() {
			if (editor != null) {
				String content = editor.getContent();
				try {
					WritableContainer<ByteBuffer> writable = ((WritableResource) resource).getWritable();
					try {
						writable.write(IOUtils.wrap(content.getBytes("UTF-8"), true));	
					}
					finally {
						writable.close();
					}
				}
				catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		}

		@Override
		public Node getView() {
			if (editor == null) {
				String content;
				
				try {
					ReadableContainer<ByteBuffer> readable = ((ReadableResource) resource).getReadable();
					try {
						content = new String(IOUtils.toBytes(readable), "UTF-8");
					}
					finally {
						readable.close();
					}
				}
				catch (Exception e) {
					throw new RuntimeException(e);
				}
				editor = new AceEditor();
				editor.setContent(resource.getContentType(), content);
				editor.subscribe(AceEditor.CHANGE, new EventHandler<Event>() {
					@Override
					public void handle(Event arg0) {
						MainController.getInstance().setChanged();
					}
				});
				editor.subscribe(AceEditor.SAVE, new EventHandler<Event>() {
					@Override
					public void handle(Event arg0) {
						try {
							MainController.getInstance().save();
						}
						catch (IOException e) {
							throw new RuntimeException(e);
						}
					}
				});
			}
			return editor.getWebView();
		}
		
	}

}
