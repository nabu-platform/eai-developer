package be.nabu.eai.developer.collection;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import be.nabu.eai.api.NamingConvention;
import be.nabu.eai.developer.MainController;
import be.nabu.eai.developer.api.CollectionManager;
import be.nabu.eai.developer.components.RepositoryBrowser;
import be.nabu.eai.developer.impl.CustomTooltip;
import be.nabu.eai.developer.util.Confirm;
import be.nabu.eai.developer.util.EAIDeveloperUtils;
import be.nabu.eai.developer.util.Confirm.ConfirmType;
import be.nabu.eai.repository.EAIRepositoryUtils;
import be.nabu.eai.repository.api.Collection;
import be.nabu.eai.repository.api.Entry;
import be.nabu.eai.repository.api.ExtensibleEntry;
import be.nabu.eai.repository.api.Node;
import be.nabu.jfx.control.tree.drag.TreeDragDrop;
import be.nabu.libs.artifacts.api.Artifact;
import be.nabu.libs.property.ValueUtils;
import be.nabu.libs.services.api.Service;
import be.nabu.libs.types.api.DefinedType;
import be.nabu.libs.types.api.Type;
import be.nabu.libs.types.base.TypeBaseUtils;
import be.nabu.libs.types.properties.LabelProperty;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TitledPane;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

public class EAICollectionUtils {

	public static boolean isProject(Entry entry) {
		return EAIRepositoryUtils.isProject(entry);
	}
	
	public static String normalize(String name) {
		return NamingConvention.LOWER_CAMEL_CASE.apply(NamingConvention.UNDERSCORE.apply(name.trim()));
	}

	public static String getPrettyName(String id) {
		Entry entry = MainController.getInstance().getRepository().getEntry(id);
		return entry == null ? id : getPrettyName(entry);
	}
	
	public static String getPrettyName(Type type) {
		String label = ValueUtils.getValue(LabelProperty.getInstance(), type.getProperties());
		if (label != null) {
			return label;
		}
		else if (type instanceof DefinedType) {
			Entry entry = MainController.getInstance().getRepository().getEntry(((DefinedType) type).getId());
			return getPrettyName(entry, false);
		}
		else {
			return TypeBaseUtils.getPrettyName(type);
		}
	}
	
	public static String getPrettyName(Entry entry) {
		return getPrettyName(entry, true);
	}
	
	public static String getPrettyName(Entry entry, boolean prettify) {
		Collection collection = entry.getCollection();
		if (collection != null && collection.getName() != null) {
			return collection.getName();
		}
		if (entry.isNode()) {
			Node node = entry.getNode();
			if (node != null && node.getName() != null) {
				return node.getName();
			}
		}
		return prettify ? NamingConvention.UPPER_TEXT.apply(NamingConvention.UNDERSCORE.apply(entry.getName())) : entry.getName();
	}
	
	public static List<Button> newViewButton(Entry entry, Class<?> artifactClass) {
		List<Button> buttons = new ArrayList<Button>();
		for (Entry child : entry) {
			if (child.isNode() && artifactClass.isAssignableFrom(child.getNode().getArtifactClass())) {
				Button button = newViewButton(child);
				buttons.add(button);
			}
		}
		return buttons;
	}

	public static Button newViewButton(Entry child) {
		Button button = new Button();
		button.setUserData(child.getId());
		button.setGraphic(MainController.loadFixedSizeGraphic("icons/eye.png", 12));
		// if you only have one child it is probably named "swagger" which is good
		// if you have multiple, you probably gave them a descriptive name, also good!
		new CustomTooltip("View " + EAICollectionUtils.getPrettyName(child)).install(button);
		button.addEventHandler(ActionEvent.ANY, new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
				MainController.getInstance().open(child.getId());
			}
		});
		return button;
	}
	
	public static void makeDraggable(javafx.scene.Node node, String id) {
		node.addEventHandler(MouseEvent.DRAG_DETECTED, new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				Artifact resolve = MainController.getInstance().getRepository().resolve(id);
				if (resolve != null) {
					ClipboardContent clipboard = new ClipboardContent();
					// in sync with repo tree
					Dragboard dragboard = node.startDragAndDrop(TransferMode.MOVE);
					String dataType = RepositoryBrowser.getDataType(resolve.getClass());
					// we emulate the repository tree drag/drop which indicates the item path
					clipboard.put(TreeDragDrop.getDataFormat(dataType), "/" + id.replace(".", "/"));
					clipboard.put(DataFormat.PLAIN_TEXT, dataType);
					dragboard.setContent(clipboard);
					event.consume();
				}
			}
		});
	}
	
	public static Button newDeleteButton(Entry entry, String tooltip) {
		Button remove = new Button();
		remove.setGraphic(MainController.loadFixedSizeGraphic("icons/delete.png", 12));
		new CustomTooltip(tooltip == null ? "Remove this entry" : tooltip).install(remove);
		remove.addEventHandler(ActionEvent.ANY, new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
				Confirm.confirm(ConfirmType.WARNING, "Delete " + getPrettyName(entry), "Are you sure you want to delete this entry? This action can not be undone.", new EventHandler<ActionEvent>() {
					@Override
					public void handle(ActionEvent arg0) {
						EAIDeveloperUtils.delete(entry.getId());
					}
				});
			}
		});
		return remove;
	}
	
	// we try to open it in the tabpane of the current colleciton overview (if any)
	public static Tab openNewDetail(Entry entry) {
		Stage activeStage = MainController.getInstance().getActiveStage();
		// if the main stage is active, we check the tabs
		String name = entry.getCollection() != null && entry.getCollection().getName() != null ? entry.getCollection().getName() : entry.getName();
		if (activeStage.equals(MainController.getInstance().getStage())) {
			Tab selectedItem = MainController.getInstance().getTabs().getSelectionModel().getSelectedItem();
			if (selectedItem != null && selectedItem.getUserData() instanceof CollectionManager && selectedItem.getContent() instanceof TabPane) {
				Entry potentialParent = ((CollectionManager) selectedItem.getUserData()).getEntry();
				if (entry.getId().startsWith(potentialParent.getId() + ".")) {
					Tab tab = new Tab(name);
					tab.setClosable(true);
					((TabPane) selectedItem.getContent()).getTabs().add(tab);
					((TabPane) selectedItem.getContent()).getSelectionModel().select(tab);
					return tab;
				}
			}
		}
		return MainController.getInstance().newTab(name);
	}
	
	public static Entry getProject(Entry entry) {
		Entry original = entry;
		while (entry != null) {
			be.nabu.eai.repository.api.Collection collection = entry.getCollection();
			if (collection != null && "project".equals(collection.getType())) {
				return entry;
			}
			entry = entry.getParent();
		}
		// if all else fails, we take the root folder (as per default)
		return original.getRepository().getEntry(original.getId().replaceAll("\\..*", ""));
	}
	
	public static Pane newActionTile(String icon, String name, String title) {
		return newMediumActionTile(icon, name, title);
	}
	
	private static HBox newMediumActionTile(String icon, String name, String title) {
		HBox box = new HBox();
		box.getStyleClass().addAll("collection-action", "tile-action");
		box.setAlignment(Pos.CENTER);
		javafx.scene.Node graphic = MainController.loadFixedSizeGraphic(icon == null ? "application/application-medium.png" : icon, 32);
		HBox.setMargin(graphic, new Insets(0, 10, 0, 0));
		box.getChildren().add(graphic);
		
		VBox description = new VBox();
		description.setAlignment(Pos.CENTER_LEFT);
		Label nameLabel = new Label(name);
		nameLabel.getStyleClass().add("collection-title");
		description.getChildren().addAll(nameLabel);
		if (title != null) {
			Label titleLabel = new Label(title);
			titleLabel.getStyleClass().add("subscript");
			titleLabel.setMaxWidth(162);
			titleLabel.setWrapText(true);
			titleLabel.setTextAlignment(TextAlignment.LEFT);
			titleLabel.setAlignment(Pos.CENTER_LEFT);
			// if you add a label with wrapText=true to an hbox or vbox and then set it as a button graphic, it expands wildly downwards, claiming tons of empty space
			// if we wrap it in an anchorpane, we get the layout we want...
			AnchorPane wrapperPane = wrapIt(titleLabel);
			VBox.setMargin(wrapperPane, new Insets(2, 0, 0, 0));
			description.getChildren().add(wrapperPane);
		}
		box.getChildren().add(description);
		return box;
	}

	private static VBox newBigActionTile(String icon, String name, String title) {
		VBox box = new VBox();
		box.getStyleClass().addAll("collection-action", "tile-medium");
		box.setAlignment(Pos.CENTER);
		Label nameLabel = new Label(name);
		nameLabel.getStyleClass().add("collection-title");
		box.getChildren().addAll(nameLabel, MainController.loadFixedSizeGraphic(icon == null ? "application/application-big.png" : icon, 64));
		if (title != null) {
			Label titleLabel = new Label(title);
			titleLabel.getStyleClass().add("subscript");
			titleLabel.setMaxWidth(192);
			titleLabel.setWrapText(true);
			titleLabel.setTextAlignment(TextAlignment.CENTER);
			titleLabel.setAlignment(Pos.CENTER);
			// if you add a label with wrapText=true to an hbox or vbox and then set it as a button graphic, it expands wildly downwards, claiming tons of empty space
			// if we wrap it in an anchorpane, we get the layout we want...
			AnchorPane wrapperPane = wrapIt(titleLabel);
			VBox.setMargin(wrapperPane, new Insets(10, 0, 0, 0));
			box.getChildren().add(wrapperPane);
		}
		return box;
	}
	
	public static VBox newSummaryTile(Entry entry, String icon, Button...summaryButtons) {
		return newSummaryTile(entry, icon, (Map<String, List<Entry>>) null, Arrays.asList(summaryButtons));
	}
	
	public static VBox newSummaryTile(Entry entry, String icon, List<Entry> services, List<Button> summaryButtons) {
		Map<String, List<Entry>> children = new HashMap<String, List<Entry>>();
		children.put("Services", services);
		return newSummaryTile(entry, icon, children, summaryButtons);
	}
	
	public static VBox newSummaryTile(Entry entry, String icon, Map<String, List<Entry>> children, List<Button> summaryButtons) {
		Collection collection = entry.getCollection();
		VBox box = new VBox();
		box.getStyleClass().addAll("collection-summary", "tile-medium");
		VBox content = new VBox();
		box.getChildren().add(content);
		content.getStyleClass().add("collection-summary-content");
		content.setAlignment(Pos.CENTER);
		Label nameLabel = new Label(EAICollectionUtils.getPrettyName(entry));
		nameLabel.getStyleClass().add("collection-title");
		content.getChildren().addAll(nameLabel, MainController.loadFixedSizeGraphic(icon, 64));
		if (collection != null && collection.getSummary() != null) {
			Label titleLabel = new Label(collection.getSummary());
			titleLabel.getStyleClass().add("subscript");
			titleLabel.setMaxWidth(192);
			titleLabel.setWrapText(true);
			titleLabel.setTextAlignment(TextAlignment.CENTER);
			titleLabel.setAlignment(Pos.CENTER);
			content.getChildren().add(wrapIt(titleLabel));
		}
		// the summary buttons
		if (summaryButtons != null && !summaryButtons.isEmpty()) {
			HBox buttons = new HBox();
			buttons.getStyleClass().add("collection-buttons");
			buttons.getChildren().addAll(summaryButtons);
			content.getChildren().add(buttons);
		}
		if (children != null && !children.isEmpty()) {
			Accordion accordion = new Accordion();
			accordion.expandedPaneProperty().addListener(new ChangeListener<TitledPane>() {
				@Override
				public void changed(ObservableValue<? extends TitledPane> arg0, TitledPane arg1, TitledPane arg2) {
					if (arg2 != null) {
						box.getStyleClass().add("unrestricted-height");
					}
					else {
						box.getStyleClass().remove("unrestricted-height");	
					}
				}
			});
			for (Map.Entry<String, List<Entry>> childEntry : children.entrySet()) {
				if (!childEntry.getValue().isEmpty()) {
					TitledPane pane = new TitledPane();
					pane.setText(childEntry.getKey() == null ? "Related" : childEntry.getKey());
					pane.getStyleClass().addAll("inline", "no-padding");
					accordion.getPanes().add(pane);
					
					VBox paneContent = new VBox();
					paneContent.getStyleClass().add("entry-list");
					for (Entry child : childEntry.getValue()) {
						HBox childBox = new HBox();
						childBox.getStyleClass().add("entry-list-item");
						Label childNameLabel = new Label(EAICollectionUtils.getPrettyName(child));
						MainController.addCopyHandler(childNameLabel, child.getId());
						childNameLabel.setGraphic(MainController.wrapInFixed(MainController.getInstance().getGraphicFor(child.getNode().getArtifactClass()), 25, 25));
						childBox.getChildren().add(childNameLabel);
						paneContent.getChildren().add(childBox);
						MainController.getInstance().addDragHandlerForEntry(childBox, child);
						
						childBox.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
							@Override
							public void handle(MouseEvent arg0) {
								javafx.scene.Node lookup = paneContent.lookup(".selected");
								if (lookup != null) {
									lookup.getStyleClass().remove("selected");
								}
								childNameLabel.getStyleClass().add("selected");
								if (arg0.getClickCount() == 2) {
									MainController.getInstance().open(child.getId());
								}
							}
						});
					}
					
					pane.setContent(paneContent);
				}
			}
			if (!accordion.getPanes().isEmpty()) {
				box.getChildren().add(accordion);
			}
		}
		// because flow panes are FUCKING IDIOTS, I need to wrap another vbox around the actual one, this resize vbox will always be resized by the fkin idiot flowpane
		VBox resizeWrapper = new VBox();
		resizeWrapper.getChildren().add(box);
		return resizeWrapper;
	}
	
	public static AnchorPane wrapIt(Label titleLabel) {
		AnchorPane wrapperPane = new AnchorPane();
		wrapperPane.getStyleClass().add("neutral");
		// if we don't let it take the full size, if there is less than one line, the label will not take in the full width and not be centered...**sigh**
		AnchorPane.setLeftAnchor(titleLabel, 0d);
		AnchorPane.setRightAnchor(titleLabel, 0d);
		wrapperPane.getChildren().add(titleLabel);
		return wrapperPane;
	}
	
	public static List<Entry> scanForServices(Entry entry) {
		List<Entry> services = new ArrayList<Entry>();
		for (Entry child : entry) {
			if (child.isNode() && Service.class.isAssignableFrom(child.getNode().getArtifactClass())) {
				services.add(child);
			}
			else if (!child.isNode()) {
				services.addAll(scanForServices(child));
			}
		}
		return services;
	}
}
