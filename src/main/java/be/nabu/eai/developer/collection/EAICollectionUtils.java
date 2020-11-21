package be.nabu.eai.developer.collection;

import be.nabu.eai.api.NamingConvention;
import be.nabu.eai.developer.MainController;
import be.nabu.eai.developer.api.CollectionManager;
import be.nabu.eai.repository.api.Collection;
import be.nabu.eai.repository.api.Entry;
import be.nabu.eai.repository.api.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;

public class EAICollectionUtils {

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
}
