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
import be.nabu.eai.repository.events.RepositoryEvent;
import be.nabu.libs.events.api.EventHandler;
import be.nabu.libs.events.api.EventSubscription;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.control.TabPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;

// when you open a project, autoscan for collections and upgrade
// e.g. you can scan for jdbc connections and automatically create a collection around it?
// only if not in a collection yet!
public class ProjectManager implements CollectionManager {

	private Entry entry;
	private VBox content;

	public ProjectManager(Entry entry) {
		this.entry = entry;
	}

	@Override
	public boolean hasDetailView() {
		return true;
	}

	@Override
	public Node getDetailView() {
		TabPane tabs = new TabPane();
		tabs.setSide(Side.RIGHT);
		tabs.setTabClosingPolicy(TabClosingPolicy.ALL_TABS);
		
		Tab tab = new Tab("Project");
		// can't close the main tab!
		tab.setClosable(false);
		tabs.getTabs().add(tab);
		
		ScrollPane scroll = new ScrollPane();
		scroll.setFitToWidth(true);
		scroll.setHbarPolicy(ScrollBarPolicy.NEVER);
		content = new VBox();
		content.getStyleClass().add("project");
		scroll.setContent(content);
		
		drawAll(content, false);
		tab.setContent(scroll);
		return tabs;
	}

	private void drawAll(VBox content, boolean refresh) {
		content.getChildren().clear();
		
		if (refresh) {
			// we update the entry, it may have been refreshed/superceeded by another entry
			Entry refreshed = entry.getRepository().getEntry(entry.getId());
			if (refreshed != null && !refreshed.equals(entry)) {
				System.out.println("Updating to new entry");
				entry = refreshed;
			}
		}
		
		VBox section = new VBox();
		section.getStyleClass().addAll("collection-group", "project-actions");
//		HBox crumbs = new HBox();
//		crumbs.getStyleClass().add("crumbs");
//		// it is in the root of the project
//		crumbs.getChildren().add(getIcon());
//		Label crumbName = new Label(entry.getCollection().getName() == null ? entry.getName() : entry.getCollection().getName() + " Actions");
//		crumbName.getStyleClass().add("crumb-name");
//		crumbs.getChildren().add(crumbName);
		Label title = new Label("Project Actions");
		title.getStyleClass().add("h1");
		// first we add a section with the actions you can take
		HBox actions = new HBox();
		for (CollectionManagerFactory factory : MainController.getInstance().getCollectionManagerFactories()) {
			List<CollectionAction> actionsFor = factory.getActionsFor(entry);
			for (CollectionAction action : actionsFor) {
				Button button = new Button();
				button.setGraphic(action.getNode());
				button.addEventHandler(ActionEvent.ANY, action.getEventHandler());
				actions.getChildren().add(button);
			}
		}
		section.getChildren().addAll(title, actions);
		content.getChildren().add(section);
		
		
		Map<String, List<Entry>> collections = new HashMap<String, List<Entry>>();
		// first we scan the project for collections
		scan(entry, null, collections);
		
		System.out.println("found collections: " + collections);
		
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
			section = new VBox();
			VBox.setMargin(section, new Insets(20, 0, 0, 0));
			section.getStyleClass().add("collection-group");
			HBox crumbs = new HBox();
			crumbs.getStyleClass().add("crumbs");
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
						icon = collectionManager == null ? null : collectionManager.getIcon();
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
					crumbs.getChildren().add(icon);
					Label crumbName = new Label(current.getCollection() != null && current.getCollection().getName() != null ? current.getCollection().getName() : current.getName());
					crumbName.getStyleClass().add("crumb-name");
					crumbs.getChildren().add(crumbName);
				}
			}
			section.getChildren().addAll(crumbs);
			
			TilePane tiles = new TilePane();
			tiles.getStyleClass().add("collection-tiles");
			section.getChildren().add(tiles);
			tiles.setAlignment(Pos.CENTER);
			tiles.setTileAlignment(Pos.CENTER);
			for (Entry entry : collections.get(key)) {
				CollectionManager collectionManager = MainController.getInstance().newCollectionManager(entry);
				Node summaryView = collectionManager.getSummaryView();
				if (summaryView != null) {
					summaryView.getStyleClass().add("collection-tile");
					tiles.getChildren().add(summaryView);
					TilePane.setMargin(summaryView, new Insets(5));
				}
			}
			content.getChildren().add(section);
		}
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

	private List<EventSubscription<?, ?>> subscriptions = new ArrayList<EventSubscription<?, ?>>();
	@Override
	public void showDetail() {
		EventSubscription<?, ?> subscription = MainController.getInstance().getRepository().getEventDispatcher().subscribe(RepositoryEvent.class, new EventHandler<RepositoryEvent, Void>() {
			@Override
			public Void handle(RepositoryEvent event) {
				// we are interested in loading, reloading & unloading, anything might change...
				if (event.isDone()) {
					// make sure all other actions are done before we redraw
					// for example when you delete, we first do an unload cycle, then delete, but that means the unload event is done _before_ the delete, we can only see folders that are gone _after_ the delete though
					Platform.runLater(new Runnable() {
						@Override
						public void run() {
							drawAll(content, true);
						}
					});
				}
				return null;
			}
		});
		subscriptions.add(subscription);
	}

	@Override
	public void hideDetail() {
		for (EventSubscription<?, ?> subscription : subscriptions) {
			subscription.unsubscribe();
		}
		subscriptions.clear();
	}
	
}
