package be.nabu.eai.developer.collection;

import java.util.ArrayList;
import java.util.List;

import be.nabu.eai.developer.CollectionActionImpl;
import be.nabu.eai.developer.MainController;
import be.nabu.eai.developer.api.CollectionAction;
import be.nabu.eai.developer.api.CollectionManager;
import be.nabu.eai.developer.api.CollectionManagerFactory;
import be.nabu.eai.repository.api.Collection;
import be.nabu.eai.repository.api.Entry;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class ProjectManagerFactory implements CollectionManagerFactory {

	@Override
	public CollectionManager getCollectionManager(Entry entry) {
		Collection collection = entry.getCollection();
		if (collection != null && "project".equals(collection.getType()) && "standard".equals(collection.getSubType())) {
			return new ProjectManager(entry);
		}
		else if (collection != null && "application".equals(collection.getType()) && "standard".equals(collection.getSubType())) {
			return new ApplicationManager(entry);
		}
		return null;
	}

	@Override
	public List<CollectionAction> getActionsFor(Entry entry) {
		List<CollectionAction> actions = new ArrayList<CollectionAction>();
		Collection collection = entry.getCollection();
		// for projects, you can add applications
		if (collection != null && collection.getType().equals("project")) {
			VBox box = new VBox();
			box.getStyleClass().add("collection-action");
			Label title = new Label("Add Application");
			title.getStyleClass().add("collection-action-title");
			box.getChildren().addAll(MainController.loadFixedSizeGraphic("application/application-big.png", 64), title);
			actions.add(new CollectionActionImpl(box, new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent arg0) {
					// TODO
					// for an application we only need a name?
				}
			}));
		}
		return actions;
	}

}
