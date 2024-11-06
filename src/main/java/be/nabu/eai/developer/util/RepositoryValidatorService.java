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

package be.nabu.eai.developer.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import be.nabu.eai.developer.MainController;
import be.nabu.eai.developer.components.RepositoryBrowser;
import be.nabu.eai.repository.api.Entry;
import be.nabu.eai.repository.api.Repository;
import be.nabu.eai.repository.util.RepositoryValidator;
import be.nabu.libs.validator.api.Validation;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableView;
import javafx.scene.layout.AnchorPane;

public class RepositoryValidatorService implements Runnable {

	private boolean stopped;
	private RepositoryValidator validator;
	private MenuBar bar;
	private Menu menu;
	private Map<Entry, List<? extends Validation<?>>> current = new HashMap<Entry, List<? extends Validation<?>>>();
	
	public RepositoryValidatorService(Repository repository, MenuBar bar) {
		this.bar = bar;
		this.validator = new RepositoryValidator(repository);
		this.menu = new Menu("Validations");
		this.bar.getMenus().add(menu);
	}
	
	public void clear(String id) {
		synchronized(current) {
			Iterator<Entry> iterator = current.keySet().iterator();
			while (iterator.hasNext()) {
				if (iterator.next().getId().equals(id)) {
					iterator.remove();
				}
			}
		}
		refreshMenu();
	}
	
	private void refreshMenu() {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				synchronized(current) {
					menu.setText("Validations (" + current.size() + ")");
					Map<String, MenuItem> items = new HashMap<String, MenuItem>();
					ObservableList<MenuItem> menuItems = menu.getItems();
					Iterator<MenuItem> iterator = menuItems.iterator();
					while (iterator.hasNext()) {
						MenuItem item = iterator.next();
						String id = item.getText().replaceAll("[\\s]+.*", "");
						Entry found = null;
						for (Entry entry : current.keySet()) {
							if (entry.getId().equals(id)) {
								found = entry;
								break;
							}
						}
						if (found == null) {
							iterator.remove();
						}
						else {
							item.setText(id + " (" + current.get(found).size() + ")");
							items.put(id, item);
						}
					}
					for (Entry entry : current.keySet()) {
						if (!items.containsKey(entry.getId())) {
							MenuItem item = new MenuItem(entry.getId() + " (" + current.get(entry).size() + ")");
							item.addEventHandler(ActionEvent.ANY, new EventHandler<ActionEvent>() {
								@Override
								public void handle(ActionEvent arg0) {
									if (!MainController.getInstance().activate(entry.getId())) {
//										RepositoryBrowser.open(MainController.getInstance(), entry);
										// open the entry
										MainController.getInstance().open(entry.getId());
										// and show the validations
										// not all validations are visible inside the artifact, some (like deprecated references) are calculated afterwards
										List<? extends Validation<?>> validations = current.get(entry);
										TableView<Validation<?>> validationTable = new TableView<Validation<?>>();
										EAIDeveloperUtils.validationView(validationTable);
										validationTable.getItems().addAll(validations);
										MainController.getInstance().getAncPipeline().getChildren().clear();
										MainController.getInstance().getAncPipeline().getChildren().add(validationTable);
										AnchorPane.setBottomAnchor(validationTable, 0d);
										AnchorPane.setLeftAnchor(validationTable, 0d);
										AnchorPane.setTopAnchor(validationTable, 0d);
										AnchorPane.setRightAnchor(validationTable, 0d);
									}
								}
							});
							menu.getItems().add(item);
						}
					}
					List<MenuItem> list = new ArrayList<MenuItem>(menu.getItems());
					Collections.sort(list, new Comparator<MenuItem>() {
						@Override
						public int compare(MenuItem o1, MenuItem o2) {
							return o1.getText().compareTo(o2.getText());
						}
					});
					menu.getItems().clear();
					menu.getItems().addAll(list);
				}
			}
		});
	}
	
	@Override
	public void run() {
		while (!stopped) {
			Map<Entry, List<? extends Validation<?>>> validations = validator.validate();
			synchronized(current) {
				current.putAll(validations);
			}
			refreshMenu();
			try {
				Thread.sleep(60000);
			}
			catch (InterruptedException e) {
				// continue
			}
		}
	}

	
	public void start() {
		if (stopped) {
			throw new IllegalStateException("Can only start once");
		}
		Thread thread = new Thread(this);
		thread.setPriority(Thread.MIN_PRIORITY);
		thread.setDaemon(true);
		thread.start();
	}
	
	public void stop() {
		stopped = true;
	}
}
