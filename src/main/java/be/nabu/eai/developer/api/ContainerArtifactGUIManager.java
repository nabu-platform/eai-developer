package be.nabu.eai.developer.api;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;

import javafx.geometry.Side;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.layout.AnchorPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import be.nabu.eai.developer.MainController;
import be.nabu.eai.developer.managers.base.BaseArtifactGUIInstance;
import be.nabu.eai.developer.managers.base.BasePortableGUIManager;
import be.nabu.eai.repository.api.ArtifactManager;
import be.nabu.eai.repository.api.ContainerArtifact;
import be.nabu.eai.repository.api.Entry;
import be.nabu.eai.repository.api.ResourceEntry;
import be.nabu.libs.artifacts.api.Artifact;
import be.nabu.libs.validator.api.Validation;
import be.nabu.libs.validator.api.ValidationMessage;
import be.nabu.libs.validator.api.ValidationMessage.Severity;

abstract public class ContainerArtifactGUIManager<T extends ContainerArtifact> extends BasePortableGUIManager<T, BaseArtifactGUIInstance<T>> {

	private TabPane tabs;
	private Logger logger = LoggerFactory.getLogger(getClass());

	public ContainerArtifactGUIManager(String name, Class<T> artifactClass, ArtifactManager<T> artifactManager) {
		super(name, artifactClass, artifactManager);
	}
	
	protected String getTabName(String id) {
		String partial = id.replaceAll("^.*:", "");
		return partial.substring(0, 1).toUpperCase() + partial.substring(1);
	}

	@Override
	protected BaseArtifactGUIInstance<T> newGUIInstance(Entry entry) {
		return new BaseArtifactGUIInstance<T>(this, entry) {
			@Override
			public List<Validation<?>> save() throws IOException {
				List<Validation<?>> messages = super.save();
				String selected = tabs.getSelectionModel().getSelectedItem().getText();
				tabs.getTabs().clear();
				for (Artifact child : getArtifact().getContainedArtifacts()) {
					try {
						drawChild(MainController.getInstance(), tabs, child, getArtifact());
					}
					catch (ParseException e) {
						logger.error("Could not redraw child", e);
						messages.add(new ValidationMessage(Severity.ERROR, "Could not redraw child"));
					}
				}
				// reselect tab that was open
				for (Tab tab : tabs.getTabs()) {
					if (tab.getText().equals(selected)) {
						tabs.getSelectionModel().select(tab);
					}
				}
				return messages;
			}
		};
	}

	@Override
	protected void setEntry(BaseArtifactGUIInstance<T> guiInstance, ResourceEntry entry) {
		guiInstance.setEntry(entry);
	}

	@Override
	protected void setInstance(BaseArtifactGUIInstance<T> guiInstance, T instance) {
		guiInstance.setArtifact(instance);
	}

	@Override
	public void display(MainController controller, AnchorPane pane, T artifact) throws IOException, ParseException {
		tabs = new TabPane();
		tabs.setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);
		tabs.setSide(Side.RIGHT);
		for (Artifact child : artifact.getContainedArtifacts()) {
			drawChild(controller, tabs, child, artifact);
		}
		AnchorPane.setBottomAnchor(tabs, 0d);
		AnchorPane.setLeftAnchor(tabs, 0d);
		AnchorPane.setRightAnchor(tabs, 0d);
		AnchorPane.setTopAnchor(tabs, 0d);
		pane.getChildren().add(tabs);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void drawChild(MainController controller, TabPane tabs, Artifact child, T container) throws IOException, ParseException {
		ArtifactGUIManager<?> guiManager = controller.getGUIManager(child.getClass());
		if (guiManager instanceof PortableArtifactGUIManager) {
			if (guiManager instanceof ConfigurableGUIManager) {
				((ConfigurableGUIManager) guiManager).setConfiguration(container.getConfiguration(child));
			}
			Tab tab = new Tab(getTabName(child.getId()));
			AnchorPane childPane = new AnchorPane();
			((PortableArtifactGUIManager) guiManager).display(controller, childPane, child);
			tab.setContent(childPane);
			tabs.getTabs().add(tab);
		}
	}

}