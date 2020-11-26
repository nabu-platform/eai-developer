package be.nabu.eai.developer.collection;

import java.io.IOException;

import be.nabu.eai.api.NamingConvention;
import be.nabu.eai.developer.MainController;
import be.nabu.eai.developer.api.CollectionManager;
import be.nabu.eai.developer.impl.CustomTooltip;
import be.nabu.eai.developer.util.Confirm;
import be.nabu.eai.developer.util.EAIDeveloperUtils;
import be.nabu.eai.developer.util.Confirm.ConfirmType;
import be.nabu.eai.repository.api.Collection;
import be.nabu.eai.repository.api.Entry;
import be.nabu.eai.repository.api.ExtensibleEntry;
import be.nabu.eai.repository.api.Node;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

public class EAICollectionUtils {

	public static boolean isProject(Entry entry) {
		Collection collection = entry.getCollection();
		if (collection != null && "project".equals(collection.getType())) {
			return true;
		}
		// any root folder that is not nabu is currently flagged as a project (retroactive shizzle!)
		else if (entry.getParent() != null && entry.getParent().getParent() == null && !"nabu".equals(entry.getName())) {
			return true;
		}
		return false;
	}
	
	public static String normalize(String name) {
		return NamingConvention.LOWER_CAMEL_CASE.apply(NamingConvention.UNDERSCORE.apply(name.trim()));
	}
	
	public static String getPrettyName(Entry entry) {
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
		return NamingConvention.UPPER_TEXT.apply(NamingConvention.UNDERSCORE.apply(entry.getName()));
	}
	
	public static Button newDeleteButton(Entry entry, String tooltip) {
		Button remove = new Button();
		remove.setGraphic(MainController.loadFixedSizeGraphic("icons/delete.png", 16));
		new CustomTooltip(tooltip == null ? "Remove this entry" : tooltip).install(remove);
		remove.addEventHandler(ActionEvent.ANY, new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
				Confirm.confirm(ConfirmType.WARNING, "Delete " + getPrettyName(entry), "Are you sure you want to delete this entry? This action can not be undone.", new EventHandler<ActionEvent>() {
					@Override
					public void handle(ActionEvent arg0) {
						try {
							((ExtensibleEntry) entry.getParent()).deleteChild(entry.getName(), true);
							EAIDeveloperUtils.deleted(entry.getId());
						}
						catch (IOException e) {
							MainController.getInstance().notify(e);
						}
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
	
	public static VBox newActionTile(String icon, String name, String title) {
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
		Collection collection = entry.getCollection();
		VBox box = new VBox();
		box.getStyleClass().addAll("collection-summary", "tile-medium");
		box.setAlignment(Pos.CENTER);
		Label nameLabel = new Label(EAICollectionUtils.getPrettyName(entry));
		nameLabel.getStyleClass().add("collection-title");
		box.getChildren().addAll(nameLabel, MainController.loadFixedSizeGraphic(icon, 64));
		if (collection != null && collection.getSummary() != null) {
			Label titleLabel = new Label(collection.getSummary());
			titleLabel.getStyleClass().add("subscript");
			titleLabel.setMaxWidth(192);
			titleLabel.setWrapText(true);
			titleLabel.setTextAlignment(TextAlignment.CENTER);
			titleLabel.setAlignment(Pos.CENTER);
			box.getChildren().add(wrapIt(titleLabel));
		}
		// the summary buttons
		if (summaryButtons != null && summaryButtons.length > 0) {
			HBox buttons = new HBox();
			buttons.getStyleClass().add("collection-buttons");
			buttons.getChildren().addAll(summaryButtons);
			box.getChildren().add(buttons);
		}
		return box;
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
}
