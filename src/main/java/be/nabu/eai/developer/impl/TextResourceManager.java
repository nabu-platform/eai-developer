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
		if (resource.getContentType().startsWith("text/")) {
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
