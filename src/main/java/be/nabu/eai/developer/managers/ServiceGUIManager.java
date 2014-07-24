package be.nabu.eai.developer.managers;

import java.io.IOException;
import java.text.ParseException;

import javafx.geometry.Orientation;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.image.ImageView;
import be.nabu.eai.developer.MainController;
import be.nabu.eai.developer.api.ArtifactGUIInstance;
import be.nabu.eai.developer.api.ArtifactGUIManager;
import be.nabu.eai.developer.managers.util.ElementMarshallable;
import be.nabu.eai.developer.managers.util.ElementSelectionListener;
import be.nabu.eai.developer.managers.util.ElementTreeItem;
import be.nabu.eai.repository.api.ArtifactManager;
import be.nabu.eai.repository.api.Entry;
import be.nabu.jfx.control.tree.Tree;
import be.nabu.jfx.control.tree.TreeItem;
import be.nabu.libs.services.api.DefinedService;
import be.nabu.libs.types.api.Element;
import be.nabu.libs.types.base.RootElement;

public class ServiceGUIManager implements ArtifactGUIManager<DefinedService> {

	@Override
	public ArtifactManager<DefinedService> getArtifactManager() {
		return null;
	}

	@Override
	public String getArtifactName() {
		return "Service";
	}

	@Override
	public ImageView getGraphic() {
		return MainController.loadGraphic("service.png");
	}

	@Override
	public ArtifactGUIInstance create(MainController controller, TreeItem<Entry> target) throws IOException {
		return null;
	}

	@Override
	public ArtifactGUIInstance view(MainController controller, TreeItem<Entry> target) throws IOException, ParseException {
		Tab tab = controller.newTab(target.itemProperty().get().getId());
		SplitPane split = new SplitPane();
		split.setOrientation(Orientation.HORIZONTAL);
		tab.setContent(split);
		DefinedService service = (DefinedService) target.itemProperty().get().getNode().getArtifact();
		Tree<Element<?>> input = new Tree<Element<?>>(new ElementMarshallable());
		input.rootProperty().set(new ElementTreeItem(new RootElement(service.getServiceInterface().getInputDefinition(), "input"), null, false));
		input.getSelectionModel().selectedItemProperty().addListener(new ElementSelectionListener(controller, false));
		
		Tree<Element<?>> output = new Tree<Element<?>>(new ElementMarshallable());
		output.rootProperty().set(new ElementTreeItem(new RootElement(service.getServiceInterface().getOutputDefinition(), "output"), null, false));
		output.getSelectionModel().selectedItemProperty().addListener(new ElementSelectionListener(controller, false));
		
		split.getItems().addAll(input, output);
		
		return new ReadOnlyGUIInstance(target.itemProperty().get().getId());
	}

	@Override
	public Class<DefinedService> getArtifactClass() {
		return DefinedService.class;
	}

}
