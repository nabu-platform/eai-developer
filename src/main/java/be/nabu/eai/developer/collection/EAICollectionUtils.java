package be.nabu.eai.developer.collection;

import be.nabu.eai.developer.MainController;
import be.nabu.eai.developer.api.CollectionManager;
import be.nabu.eai.repository.api.Entry;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;

public class EAICollectionUtils {
	
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
	
}
