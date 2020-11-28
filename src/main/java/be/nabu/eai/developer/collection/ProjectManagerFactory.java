package be.nabu.eai.developer.collection;

import java.util.ArrayList;
import java.util.List;

import be.nabu.eai.api.NamingConvention;
import be.nabu.eai.developer.CollectionActionImpl;
import be.nabu.eai.developer.MainController;
import be.nabu.eai.developer.api.ApplicationProvider;
import be.nabu.eai.developer.api.CollectionAction;
import be.nabu.eai.developer.api.CollectionManager;
import be.nabu.eai.developer.api.CollectionManagerFactory;
import be.nabu.eai.developer.api.EntryAcceptor;
import be.nabu.eai.developer.managers.util.SimplePropertyUpdater;
import be.nabu.eai.developer.util.EAIDeveloperUtils;
import be.nabu.eai.repository.CollectionImpl;
import be.nabu.eai.repository.api.Collection;
import be.nabu.eai.repository.api.Entry;
import be.nabu.eai.repository.resources.RepositoryEntry;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class ProjectManagerFactory implements CollectionManagerFactory {

	@Override
	public CollectionManager getCollectionManager(Entry entry) {
		Collection collection = entry.getCollection();
		if (EAICollectionUtils.isProject(entry)) {
			return new ProjectManager(entry);
		}
		else if (collection != null && "application".equals(collection.getType())) {
			return new ApplicationManager(entry);
		}
		return null;
	}

	@Override
	public List<CollectionAction> getActionsFor(Entry entry) {
		List<CollectionAction> actions = new ArrayList<CollectionAction>();
		// for projects, you can add applications
		if (EAICollectionUtils.isProject(entry)) {
			actions.add(new CollectionActionImpl(EAICollectionUtils.newActionTile("application/application-big.png", "Add Application", "Interact with data or visualize insights."), new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent arg0) {
					TilePane tiles = new TilePane();
					tiles.getStyleClass().add("collection-tiles");
					VBox root = new VBox();
					Stage stage = EAIDeveloperUtils.buildPopup("Add Application", root, MainController.getInstance().getStage(), StageStyle.DECORATED, false);

					for (ApplicationProvider provider : ApplicationManager.getApplicationProviders()) {
						Node largeIcon = provider.getLargeCreateIcon();
						if (largeIcon != null) {
							Button button = new Button();
							button.setGraphic(largeIcon);
							button.setPrefWidth(232);
							button.setPrefHeight(172);
							tiles.getChildren().add(button);
							button.addEventHandler(ActionEvent.ANY, new EventHandler<ActionEvent>() {
								@Override
								public void handle(ActionEvent arg0) {
									stage.close();
									create(entry, provider);
								}
							});
						}
					}
					Node newNode = ApplicationManager.newNode(null, "Empty Application", "Start from scratch");
					Button button = new Button();
					button.getStyleClass().addAll("collection-tile", "tile-medium");
					button.setGraphic(newNode);
					button.addEventHandler(ActionEvent.ANY, new EventHandler<ActionEvent>() {
						@Override
						public void handle(ActionEvent arg0) {
							stage.close();
							create(entry, null);
						}
					});
					tiles.getChildren().add(button);
					
					root.getStyleClass().addAll("project");
					// if we don't explicitly set it, it will default to 5, claiming empty space if you only have like 4...
					tiles.setPrefColumns(Math.min(4, tiles.getChildren().size()));
					Label title = new Label("Choose the application type");
					title.getStyleClass().add("h2");
					root.getChildren().addAll(title, tiles);
					
					
					HBox buttons = new HBox();
					buttons.getStyleClass().add("buttons");
					Button close = new Button("Close");
					close.addEventHandler(ActionEvent.ANY, new EventHandler<ActionEvent>() {
						@Override
						public void handle(ActionEvent event) {
							stage.close();
						}
					});
					buttons.getChildren().add(close);
					root.getChildren().add(buttons);
					stage.sizeToScene();
					stage.show();
				}
			}, new EntryAcceptor() {
				@Override
				public boolean accept(Entry entry) {
					return EAICollectionUtils.isProject(entry);
				}
			}));
		}
		return actions;
	}

	private void create(Entry entry, ApplicationProvider provider) {
		VBox root = new VBox();
		root.getStyleClass().add("collection-form");
		BasicInformation basicInformation = new BasicInformation();
		basicInformation.setName(provider.suggestName(entry));
		SimplePropertyUpdater updater = EAIDeveloperUtils.createUpdater(basicInformation, null);
		VBox container = new VBox();
		root.getChildren().add(container);
		MainController.getInstance().showProperties(updater, container, true);
		
		HBox buttons = new HBox();
		buttons.getStyleClass().add("buttons");
		root.getChildren().add(buttons);
		
		Button create = new Button("Create");
		create.getStyleClass().add("primary");
		Button cancel = new Button("Cancel");
		buttons.getChildren().addAll(create, cancel);
		
		Stage stage = EAIDeveloperUtils.buildPopup("Add Application", root, MainController.getInstance().getStage(), StageStyle.DECORATED, false);
		
		cancel.addEventHandler(ActionEvent.ANY, new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				stage.close();
			}
		});
		create.addEventHandler(ActionEvent.ANY, new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				stage.close();
				if (basicInformation.getName() != null && !basicInformation.getName().trim().isEmpty()) {
					try {
						// we create a new entry
						// then use the provider to initialize it
						String normalized = EAICollectionUtils.normalize(basicInformation.getName());
						if (basicInformation.getVersion() != null && !basicInformation.getVersion().trim().isEmpty()) {
							if (basicInformation.getVersion().startsWith("v")) {
								basicInformation.setVersion(basicInformation.getVersion().substring(1));
							}
							if (!basicInformation.getVersion().trim().isEmpty()) {
								normalized += "v" + NamingConvention.UNDERSCORE.apply(basicInformation.getVersion().trim());
							}
						}
						RepositoryEntry applicationDirectory = EAIDeveloperUtils.mkdir((RepositoryEntry) entry, normalized);
						CollectionImpl collection = applicationDirectory.getCollection();
						if (collection == null) {
							collection = new CollectionImpl();
							applicationDirectory.setCollection(collection);
							if (provider != null) {
								collection.setSmallIcon(provider.getSmallIcon());
								collection.setMediumIcon(provider.getMediumIcon());
								collection.setLargeIcon(provider.getLargeIcon());
							}
						}
						collection.setType("application");
						collection.setSubType(provider.getSubType());
						// if we have a version, we definitely want a pretty name
						if (basicInformation.getVersion() != null && !basicInformation.getVersion().trim().isEmpty()) {
							collection.setName(basicInformation.getName().trim() + " v" + basicInformation.getVersion());
						}
						else if (!normalized.equals(basicInformation.getName().trim())) {
							collection.setName(basicInformation.getName().trim());
						}
						applicationDirectory.saveCollection();
						EAIDeveloperUtils.updated(applicationDirectory.getId());
						provider.initialize(applicationDirectory, basicInformation.getVersion() != null && !basicInformation.getVersion().trim().isEmpty() ? basicInformation.getVersion().trim() : null);
					}
					catch (Exception e) {
						MainController.getInstance().notify(e);
					}
				}
			}
		});
		
		stage.sizeToScene();
		stage.show();
	}
}
