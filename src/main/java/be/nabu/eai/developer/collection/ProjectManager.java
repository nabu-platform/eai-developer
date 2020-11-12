package be.nabu.eai.developer.collection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import be.nabu.eai.developer.MainController;
import be.nabu.eai.developer.api.CollectionAction;
import be.nabu.eai.developer.api.CollectionManager;
import be.nabu.eai.developer.api.CollectionManagerFactory;
import be.nabu.eai.repository.api.Entry;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.layout.HBox;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;

public class ProjectManager implements CollectionManager {

	private Entry entry;

	public ProjectManager(Entry entry) {
		this.entry = entry;
	}

	@Override
	public boolean hasDetailView() {
		return true;
	}

	@Override
	public Node getDetailView() {
		ScrollPane scroll = new ScrollPane();
		scroll.setFitToWidth(true);
		scroll.setHbarPolicy(ScrollBarPolicy.NEVER);
		VBox content = new VBox();
		content.getStyleClass().add("project");
		scroll.setContent(content);
		
		// first we add a section with the actions you can take
		HBox actions = new HBox();
		actions.getStyleClass().add("project-actions");
		content.getChildren().add(actions);
		for (CollectionManagerFactory factory : MainController.getInstance().getCollectionManagerFactories()) {
			List<CollectionAction> actionsFor = factory.getActionsFor(entry);
			for (CollectionAction action : actionsFor) {
				Button button = new Button();
				button.setGraphic(action.getNode());
				button.addEventHandler(ActionEvent.ANY, action.getEventHandler());
				actions.getChildren().add(button);
			}
		}
		
		Map<String, List<Entry>> collections = new HashMap<String, List<Entry>>();
		// first we scan the project for collections
		scan(entry, null, collections);
		
		ArrayList<String> keys = new ArrayList<String>(collections.keySet());
		Collections.sort(keys, new Comparator<String>() {
			@Override
			public int compare(String arg0, String arg1) {
				if (arg0 == null && arg1 != null) {
					return -1;
				}
				else if (arg0 != null && arg1 == null) {
					return 1;
				}
				else if (arg0 == null && arg1 == null) {
					return 0;
				}
				String[] split1 = arg0.split("\\.");
				String[] split2 = arg1.split("\\.");
				if (split1.length < split2.length) {
					return -1;
				}
				else if (split2.length < split1.length) {
					return 1;
				}
				else {
					return arg0.compareToIgnoreCase(arg1);
				}
			}
		});
		for (String key : keys) {
			VBox section = new VBox();
			section.getStyleClass().add("collection-group");
			HBox crumbs = new HBox();
			crumbs.setAlignment(Pos.CENTER_LEFT);
			// it is in the root of the project
			if (key == null) {
				crumbs.getChildren().add(getIcon());
				Label crumbName = new Label(entry.getCollection().getName() == null ? entry.getName() : entry.getCollection().getName());
				crumbName.getStyleClass().add("crumb-name");
				crumbs.getChildren().add(crumbName);
			}
			else {
				Entry current = this.entry;
				boolean first = true;
				for (String part : key.split("\\.")) {
					current = current.getChild(part);
					Node icon = null;
					if (current.isCollection()) {
						CollectionManager collectionManager = MainController.getInstance().newCollectionManager(current);
						icon = collectionManager.getIcon();
						if (icon == null && current.getCollection().getIcon() != null) {
							icon = MainController.loadFixedSizeGraphic(current.getCollection().getIcon(), 16, 25);
						}
					}
					if (icon == null) {
						icon = MainController.loadFixedSizeGraphic("folder.png", 16, 25);
					}
					if (first) {
						first = false;
					}
					else {
						Label separator = new Label();
						separator.setGraphic(MainController.loadFixedSizeGraphic("right-chevron.png", 12, 25));
						separator.getStyleClass().add("crumb-separator");
						crumbs.getChildren().add(separator);
					}
					crumbs.getChildren().add(getIcon());
					Label crumbName = new Label(entry.getCollection() != null && entry.getCollection().getName() != null ? entry.getCollection().getName() : entry.getName());
					crumbName.getStyleClass().add("crumb-name");
					crumbs.getChildren().add(crumbName);
				}
			}
			section.getChildren().addAll(crumbs);
			
			TilePane tiles = new TilePane();
			tiles.getStyleClass().add("collection-tiles");
			section.getChildren().add(tiles);
			for (Entry entry : collections.get(key)) {
				CollectionManager collectionManager = MainController.getInstance().newCollectionManager(entry);
				Node summaryView = collectionManager.getSummaryView();
				summaryView.getStyleClass().add("collection-tile");
				tiles.getChildren().add(summaryView);
			}
			content.getChildren().add(section);
		}
		return scroll;
	}
	
	private void scan(Entry entry, String path, Map<String, List<Entry>> collections) {
		for (Entry child : entry) {
			// we have a collection worth mentioning!
			CollectionManager collectionManager = MainController.getInstance().newCollectionManager(child);
			if (collectionManager != null && collectionManager.hasSummaryView()) {
				if (!collections.containsKey(path)) {
					collections.put(path, new ArrayList<Entry>());
				}
				collections.get(path).add(child);
			}
			// we don't recurse _inside_ collections (for now)
			// but we do recurse folders
			else if (!child.isNode()) {
				String childPath = path == null ? child.getName() : path + "." + child.getName();
				scan(child, childPath, collections);
			}
		}
	}

	@Override
	public boolean hasIcon() {
		return true;
	}

	@Override
	public Node getIcon() {
		return MainController.loadFixedSizeGraphic("folder-project.png", 16, 25);
	}
	
}
