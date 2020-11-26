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
import javafx.collections.ListChangeListener;
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
import javafx.scene.layout.Priority;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;

// when you open a project, autoscan for collections and upgrade
// e.g. you can scan for jdbc connections and automatically create a collection around it?
// only if not in a collection yet!
public class ProjectManager implements CollectionManager {

	private Entry entry;
	private VBox content;
	private boolean useTabs;
	private String lastThinSelected;

	public ProjectManager(Entry entry) {
		this(entry, true);
	}
	
	public ProjectManager(Entry entry, boolean useTabs) {
		this.entry = entry;
		this.useTabs = useTabs;
	}

	@Override
	public boolean hasThinDetailView() {
		return true;
	}

	@Override
	public Node getThinDetailView() {
		ScrollPane scroll = new ScrollPane();
		scroll.setFitToWidth(true);
		scroll.setFitToHeight(true);
		scroll.setHbarPolicy(ScrollBarPolicy.NEVER);
		thinContent = new VBox();
		scroll.setContent(thinContent);
		drawAllThin(thinContent);
		return scroll;
	}
	
	private void drawAllThin(VBox content) {
		content.getChildren().clear();
		HBox halves = new HBox();
		VBox.setVgrow(halves, Priority.ALWAYS);
		VBox topics = new VBox();
		topics.getStyleClass().add("collection-topics");
		VBox contents = new VBox();
		contents.getStyleClass().add("collection-topic-contents");
		halves.getChildren().addAll(topics, contents);
		content.getChildren().add(halves);
		
		Map<String, List<Entry>> collections = new HashMap<String, List<Entry>>();
		// first we scan the project for collections
		scan(entry, null, collections);
		
		VBox actionTopic = new VBox();
		actionTopic.getStyleClass().add("collection-topic");
		actionTopic.getChildren().add(MainController.loadFixedSizeGraphic("icons/menu-medium.png", 32));
		Label actionTopicName = new Label("Actions");
		actionTopicName.getStyleClass().add("collection-topic-name");
		actionTopic.getChildren().add(actionTopicName);
		
		TilePane actions = new TilePane();
		VBox.setMargin(actions, new Insets(5, 0, 0, 0));
		actions.getStyleClass().add("collection-tiles");
		actions.setVgap(5);
		actions.setHgap(5);
		for (CollectionManagerFactory factory : MainController.getInstance().getCollectionManagerFactories()) {
			List<CollectionAction> actionsFor = factory.getActionsFor(entry);
			for (CollectionAction action : actionsFor) {
				Button button = new Button();
				button.getStyleClass().add("collection-action-button");
				button.setGraphic(action.getNode());
				button.addEventHandler(ActionEvent.ANY, action.getEventHandler());
				actions.getChildren().add(button);
			}
		}
		Button actionButton = new Button();
		topics.getChildren().addAll(actionButton);
		actionButton.setGraphic(actionTopic);
		actionButton.getStyleClass().addAll("collection-topic-button", "collection-tile");
		VBox.setMargin(actionButton, new Insets(3, 0, 0, 0));
		actionButton.addEventHandler(ActionEvent.ANY, new javafx.event.EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
				lastThinSelected = "$actions";
				activateTopic(topics, contents, actionButton, actions);
			}
		});
		
		boolean activated = false;
		ArrayList<String> keys = getSortedKeys(collections);
		for (String key : keys) {
			VBox topic = new VBox();
			topic.getStyleClass().add("collection-topic");

			// it is in the root of the project
			if (key == null) {
				// if it's at the root, it "should" be an application, this is not guaranteed but very likely
//				topic.getChildren().add(MainController.loadFixedSizeGraphic("project-medium.png", 32));
//				Label topicName = new Label("Project");
//				topicName.getStyleClass().add("collection-topic-name");
//				topic.getChildren().add(topicName);
				topic.getChildren().add(MainController.loadFixedSizeGraphic("application/application-medium.png", 32));
				Label topicName = new Label("Applications");
				topicName.getStyleClass().add("collection-topic-name");
				topic.getChildren().add(topicName);
			}
			else {
				Entry current = this.entry;
				boolean first = true;
				for (String part : key.split("\\.")) {
					current = current.getChild(part);
				}
				Node icon = null;
				if (current.isCollection() && current.getCollection().getMediumIcon() != null) {
					icon = MainController.loadFixedSizeGraphic(current.getCollection().getMediumIcon(), 32);
				}
				if (icon == null) {
					icon = MainController.loadFixedSizeGraphic("project-medium.png", 32);
				}
				if (first) {
					first = false;
				}
				topic.getChildren().add(icon);
				Label topicName = new Label(current.getCollection() != null && current.getCollection().getName() != null ? current.getCollection().getName() : current.getName());
				topicName.getStyleClass().add("collection-topic-name");
				topic.getChildren().add(topicName);
			}
			Button button = new Button();
			topics.getChildren().addAll(button);
			button.setGraphic(topic);
			button.getStyleClass().add("collection-topic-button");
			VBox.setMargin(button, new Insets(3, 0, 0, 0));
			
			TilePane tiles = new TilePane();
			VBox.setMargin(tiles, new Insets(5, 0, 0, 0));
			tiles.setVgap(5);
			tiles.setHgap(5);
			tiles.getStyleClass().add("collection-tiles");
			tiles.setAlignment(Pos.CENTER_LEFT);
			tiles.setTileAlignment(Pos.CENTER);
			for (Entry entry : collections.get(key)) {
				CollectionManager collectionManager = MainController.getInstance().newCollectionManager(entry);
				Node summaryView = collectionManager.getSummaryView();
				if (summaryView != null) {
					summaryView.getStyleClass().add("collection-tile");
					tiles.getChildren().add(summaryView);
				}
			}
			button.addEventHandler(ActionEvent.ANY, new javafx.event.EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent arg0) {
					lastThinSelected = key;
					activateTopic(topics, contents, button, tiles);
				}
			});
			if (lastThinSelected != null && lastThinSelected.equals(key)) {
				contents.getChildren().add(tiles);
				button.getStyleClass().add("collection-topic-button-selected");
				activated = true;
			}
		}
		// default activate the actions
		if (!activated) {
			activateTopic(topics, contents, actionButton, actions);
		}
	}

	private void activateTopic(VBox topics, VBox contents, Button button, TilePane tiles) {
		contents.getChildren().clear();
		contents.getChildren().add(tiles);
		Node lookup = topics.lookup(".collection-topic-button-selected");
		if (lookup != null) {
			lookup.getStyleClass().remove("collection-topic-button-selected");
		}
		button.getStyleClass().add("collection-topic-button-selected");
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
		content = new VBox();
		content.getStyleClass().add("project");
		scroll.setContent(content);
		
		drawAll(content, false);
		if (useTabs) {
			tabs = new TabPane();
			tabs.setSide(Side.RIGHT);
			tabs.setTabClosingPolicy(TabClosingPolicy.ALL_TABS);
			
			Tab tab = new Tab("Project");
			// can't close the main tab!
			tab.setClosable(false);
			tabs.getTabs().add(tab);
			tab.setContent(scroll);
			
			tabs.getTabs().addListener(new ListChangeListener<Tab>() {
				@Override
				public void onChanged(javafx.collections.ListChangeListener.Change<? extends Tab> change) {
					while (change.next()) {
						if (change.wasRemoved()) {
							for (Tab removed : change.getRemoved()) {
								if (removed.getUserData() instanceof CollectionManager) {
									((CollectionManager) removed.getUserData()).hideDetail();
								}
							}
						}
					}
				}
			});
			
			return tabs;
		}
		else {
			return scroll;
		}
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
		Label title = new Label("Actions");
		title.getStyleClass().add("h1");
		// first we add a section with the actions you can take
		TilePane actions = new TilePane();
		actions.getStyleClass().add("collection-tiles");
		actions.setVgap(5);
		actions.setHgap(5);
		for (CollectionManagerFactory factory : MainController.getInstance().getCollectionManagerFactories()) {
			List<CollectionAction> actionsFor = factory.getActionsFor(entry);
			for (CollectionAction action : actionsFor) {
				Button button = new Button();
				button.getStyleClass().add("collection-action-button");
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
		
		ArrayList<String> keys = getSortedKeys(collections);
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
						if (icon == null && current.getCollection().getSmallIcon() != null) {
							icon = MainController.loadFixedSizeGraphic(current.getCollection().getSmallIcon(), 16, 25);
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
			VBox.setMargin(tiles, new Insets(5, 0, 0, 0));
			tiles.setVgap(5);
			tiles.setHgap(5);
			tiles.getStyleClass().add("collection-tiles");
			section.getChildren().add(tiles);
			tiles.setAlignment(Pos.CENTER_LEFT);
			tiles.setTileAlignment(Pos.CENTER);
			for (Entry entry : collections.get(key)) {
				CollectionManager collectionManager = MainController.getInstance().newCollectionManager(entry);
				Node summaryView = collectionManager.getSummaryView();
				if (summaryView != null) {
					summaryView.getStyleClass().add("collection-tile");
					tiles.getChildren().add(summaryView);
//					TilePane.setMargin(summaryView, new Insets(5));
				}
			}
			content.getChildren().add(section);
		}
	}

	private ArrayList<String> getSortedKeys(Map<String, List<Entry>> collections) {
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
		return keys;
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
			// we don't recurse _inside_ collections that have their own detailed view
			if (!child.isNode() && (collectionManager == null || !collectionManager.hasDetailView())) {
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
	private TabPane tabs;
	private VBox thinContent;
	
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
							if (content != null) {
								drawAll(content, true);
							}
							if (thinContent != null) {
								drawAllThin(thinContent);
							}
						}
					});
				}
				return null;
			}
		});
		subscriptions.add(subscription);
		// also do for child tabs!
		if (tabs != null) {
			for (Tab tab : tabs.getTabs()) {
				if (tab.getUserData() instanceof CollectionManager) {
					((CollectionManager) tab.getUserData()).showDetail();
				}
			}
		}
	}

	@Override
	public void hideDetail() {
		for (EventSubscription<?, ?> subscription : subscriptions) {
			subscription.unsubscribe();
		}
		subscriptions.clear();
		// also do for child tabs!
		if (tabs != null) {
			for (Tab tab : tabs.getTabs()) {
				if (tab.getUserData() instanceof CollectionManager) {
					((CollectionManager) tab.getUserData()).hideDetail();
				}
			}
		}
	}

	@Override
	public Entry getEntry() {
		return entry;
	}
	
}
