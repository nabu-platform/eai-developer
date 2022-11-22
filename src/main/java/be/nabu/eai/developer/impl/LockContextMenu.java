package be.nabu.eai.developer.impl;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;

import be.nabu.eai.developer.MainController;
import be.nabu.eai.developer.api.EntryContextMenuProvider;
import be.nabu.eai.developer.managers.util.SimpleProperty;
import be.nabu.eai.developer.managers.util.SimplePropertyUpdater;
import be.nabu.eai.developer.util.EAIDeveloperUtils;
import be.nabu.eai.repository.EAIRepositoryUtils;
import be.nabu.eai.repository.EAIRepositoryUtils.EntryFilter;
import be.nabu.eai.repository.api.Entry;
import be.nabu.eai.repository.api.ResourceEntry;
import be.nabu.libs.property.api.Property;
import be.nabu.libs.resources.api.ReadableResource;
import be.nabu.libs.types.base.ValueImpl;
import be.nabu.libs.validator.api.ValidationMessage;
import be.nabu.libs.validator.api.ValidationMessage.Severity;
import be.nabu.utils.io.IOUtils;
import be.nabu.utils.io.api.ByteBuffer;
import be.nabu.utils.io.api.ReadableContainer;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.MenuItem;

public class LockContextMenu implements EntryContextMenuProvider {

	@Override
	public MenuItem getContext(Entry entry) {
		if (EAIRepositoryUtils.isProject(entry)) {
			MenuItem download = new MenuItem("Download locked");
			download.addEventHandler(ActionEvent.ANY, new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent arg0) {
					SimpleProperty<File> fileProperty = new SimpleProperty<File>("File", File.class, true);
					SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmssSSS");
					File file = new File(entry.getName() + "-v" + formatter.format(new Date()) + ".zip");
					Set properties = new LinkedHashSet(Arrays.asList(new Property [] { fileProperty }));
					final SimplePropertyUpdater updater = new SimplePropertyUpdater(true, properties, 
						new ValueImpl<File>(fileProperty, file));
					EAIDeveloperUtils.buildPopup(MainController.getInstance(), updater, "Download locked " + entry.getName(), new EventHandler<ActionEvent>() {
						@Override
						public void handle(ActionEvent arg0) {
							try {
								File file = updater.getValue("File");
								OutputStream output = new BufferedOutputStream(new FileOutputStream(file));
								try {
									EAIRepositoryUtils.zipInto(output, entry, new EntryFilter() {
										@Override
										public boolean accept(ResourceEntry entry) {
											return entry.isNode() && entry.getNode().isLocked();
										}
										@Override
										public boolean recurse(ResourceEntry entry) {
											return !entry.isLeaf();
										}
									});
								}
								finally {
									output.close();
								}
							}
							catch (Exception e) {
								e.printStackTrace();
								MainController.getInstance().notify(new ValidationMessage(Severity.ERROR, "Cannot download resource '" + entry.getName() + "': " + e.getMessage()));
							}
						}
					});
				}
			});
			return download;
		}
		return null;
	}

}
