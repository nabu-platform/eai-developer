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

package be.nabu.eai.developer.collection;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import be.nabu.eai.developer.MainController;
import be.nabu.eai.developer.api.ApplicationProvider;
import be.nabu.eai.developer.api.CollectionManager;
import be.nabu.eai.developer.impl.CustomTooltip;
import be.nabu.eai.developer.util.Confirm;
import be.nabu.eai.developer.util.Confirm.ConfirmType;
import be.nabu.eai.developer.util.EAIDeveloperUtils;
import be.nabu.eai.repository.EAIRepositoryUtils;
import be.nabu.eai.repository.api.Collection;
import be.nabu.eai.repository.api.Entry;
import be.nabu.eai.repository.api.ExtensibleEntry;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Button;

// the application manager gets the same view as the project manager!
public class ApplicationManager implements CollectionManager {

	private Entry entry;
	private ProjectManager projectManager;

	private static List<ApplicationProvider> applicationProviders;
	
	public static List<ApplicationProvider> getApplicationProviders() {
		if (applicationProviders == null) {
			synchronized(ApplicationManager.class) {
				if (applicationProviders == null) {
					List<ApplicationProvider> applicationProviders = new ArrayList<ApplicationProvider>();
					for (Class<ApplicationProvider> manager : EAIRepositoryUtils.getImplementationsFor(ApplicationProvider.class)) {
						try {
							applicationProviders.add(manager.newInstance());
						}
						catch (Exception e) {
							e.printStackTrace();
						}
					}
					ApplicationManager.applicationProviders = applicationProviders;
				}
			}
		}
		return applicationProviders;
	}
	
	public ApplicationManager(Entry entry) {
		this.entry = entry;
	}

	@Override
	public boolean hasThinDetailView() {
		return true;
	}

	@Override
	public Node getThinDetailView() {
		if (projectManager == null) {
			projectManager = new ProjectManager(entry, false);
		}
		return projectManager.getThinDetailView();
	}

	@Override
	public boolean hasDetailView() {
		return true;
	}

	@Override
	public Node getDetailView() {
		if (projectManager == null) {
			projectManager = new ProjectManager(entry, false);
		}
		return projectManager.getDetailView();
	}

	@Override
	public void showDetail() {
		projectManager.showDetail();
	}

	@Override
	public void hideDetail() {
		projectManager.hideDetail();
	}

	@Override
	public Entry getEntry() {
		return entry;
	}
	
	
	@Override
	public boolean hasSummaryView() {
		return true;
	}

	@Override
	public Node getSummaryView() {
		Collection collection = entry.getCollection();
		String subType = collection == null ? null : collection.getSubType();
		if (subType != null) {
			for (ApplicationProvider provider : ApplicationManager.getApplicationProviders()) {
				if (subType.equals(provider.getSubType())) {
					Node summaryView = provider.getSummaryView(entry);
					if (summaryView != null) {
						return summaryView;
					}
				}
			}
		}
		return buildSummaryView(entry, "application/application-big.png");
	}

	public static Node buildSummaryView(Entry entry, String icon, Button...summaryButtons) {
		List<Button> allButtons = new ArrayList<Button>();

		// a button to open the application collections
		Button view = new Button();
		view.setGraphic(MainController.loadFixedSizeGraphic("icons/search.png", 12));
		new CustomTooltip("Open the application").install(view);
		allButtons.add(view);
		view.addEventHandler(ActionEvent.ANY, new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				MainController.getInstance().openCollection(entry, event);
			}
		});
		
		// the summary buttons
		if (summaryButtons != null && summaryButtons.length > 0) {
			allButtons.addAll(Arrays.asList(summaryButtons));
		}
		
		Button remove = new Button();
		remove.setGraphic(MainController.loadFixedSizeGraphic("icons/delete.png", 12));
		new CustomTooltip("Remove the application").install(remove);
		allButtons.add(remove);
		remove.addEventHandler(ActionEvent.ANY, new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
				Confirm.confirm(ConfirmType.WARNING, "Delete " + entry.getName(), "Are you sure you want to delete this application? This action can not be undone.", new EventHandler<ActionEvent>() {
					@Override
					public void handle(ActionEvent arg0) {
						EAIDeveloperUtils.delete(entry.getId());
					}
				});
			}
		});
		// a button to delete it
		return EAICollectionUtils.newSummaryTile(entry, icon, allButtons.toArray(new Button[allButtons.size()]));
	}

	public static Node newNode(String icon, String name, String title) {
		return EAICollectionUtils.newActionTile(icon, name, title);
	}

}
