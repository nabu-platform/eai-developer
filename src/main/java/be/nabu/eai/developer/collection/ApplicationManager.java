package be.nabu.eai.developer.collection;

import java.util.ArrayList;
import java.util.List;

import be.nabu.eai.developer.MainController;
import be.nabu.eai.developer.api.ApplicationProvider;
import be.nabu.eai.developer.api.CollectionManager;
import be.nabu.eai.repository.EAIRepositoryUtils;
import be.nabu.eai.repository.api.Collection;
import be.nabu.eai.repository.api.Entry;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;

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
		HBox buttons = new HBox();
		buttons.getStyleClass().add("collection-buttons");
		box.getChildren().add(buttons);
		// a button to open the application collections
		Button view = new Button();
		view.setGraphic(MainController.loadFixedSizeGraphic("icons/eye.png", 16));
		buttons.getChildren().add(view);
		// the summary buttons
		
		Button remove = new Button();
		remove.setGraphic(MainController.loadFixedSizeGraphic("icons/delete.png", 16));
		buttons.getChildren().add(remove);
		// a button to delete it
		return box;
	}

	public static Node newNode(String icon, String name, String title) {
		VBox box = new VBox();
		box.getStyleClass().addAll("collection-action");
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
			box.getChildren().add(wrapperPane);
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
