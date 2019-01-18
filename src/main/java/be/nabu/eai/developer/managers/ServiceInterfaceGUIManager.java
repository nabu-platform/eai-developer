package be.nabu.eai.developer.managers;

import java.io.IOException;
import java.text.ParseException;

import javafx.geometry.Orientation;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.image.ImageView;
import be.nabu.eai.developer.MainController;
import be.nabu.eai.developer.api.ArtifactGUIInstance;
import be.nabu.eai.developer.api.ArtifactGUIManager;
import be.nabu.eai.developer.managers.util.ElementMarshallable;
import be.nabu.eai.developer.util.EAIDeveloperUtils;
import be.nabu.eai.developer.util.ElementClipboardHandler;
import be.nabu.eai.developer.util.ElementSelectionListener;
import be.nabu.eai.developer.util.ElementTreeItem;
import be.nabu.eai.repository.api.ArtifactManager;
import be.nabu.eai.repository.api.Entry;
import be.nabu.jfx.control.tree.Tree;
import be.nabu.jfx.control.tree.TreeItem;
import be.nabu.libs.services.api.DefinedServiceInterface;
import be.nabu.libs.types.api.Element;
import be.nabu.libs.types.base.RootElement;

public class ServiceInterfaceGUIManager implements ArtifactGUIManager<DefinedServiceInterface> {

	@Override
	public ArtifactManager<DefinedServiceInterface> getArtifactManager() {
		return null;
	}

	@Override
	public String getArtifactName() {
		return "Service Interface";
	}

	@Override
	public ImageView getGraphic() {
		return MainController.loadGraphic("serviceinterfacereadonly.png");
	}

	@Override
	public ArtifactGUIInstance create(MainController controller, TreeItem<Entry> target) throws IOException {
		return null;
	}

	@Override
	public ArtifactGUIInstance view(MainController controller, TreeItem<Entry> target) throws IOException, ParseException {
		ReadOnlyGUIInstance instance = new ReadOnlyGUIInstance(target.itemProperty().get().getId());
		Tab tab = controller.newTab(target.itemProperty().get().getId(), instance);
		SplitPane split = new SplitPane();
		split.setOrientation(Orientation.HORIZONTAL);
		tab.setContent(split);
		DefinedServiceInterface iface = (DefinedServiceInterface) target.itemProperty().get().getNode().getArtifact();
		Tree<Element<?>> input = new Tree<Element<?>>(new ElementMarshallable());
		EAIDeveloperUtils.addElementExpansionHandler(input);
		input.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		input.rootProperty().set(new ElementTreeItem(new RootElement(iface.getInputDefinition(), "input"), null, false, false));
		input.getSelectionModel().selectedItemProperty().addListener(new ElementSelectionListener(controller, false));
		input.setClipboardHandler(new ElementClipboardHandler(input, false));
		
		Tree<Element<?>> output = new Tree<Element<?>>(new ElementMarshallable());
		EAIDeveloperUtils.addElementExpansionHandler(output);
		output.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		output.rootProperty().set(new ElementTreeItem(new RootElement(iface.getOutputDefinition(), "output"), null, false, false));
		output.getSelectionModel().selectedItemProperty().addListener(new ElementSelectionListener(controller, false));
		output.setClipboardHandler(new ElementClipboardHandler(output, false));
		
		ScrollPane inputScroll = new ScrollPane();
		ScrollPane outputScroll = new ScrollPane();
		inputScroll.setContent(input);
		outputScroll.setContent(output);
		output.prefWidthProperty().bind(outputScroll.widthProperty());
		input.prefWidthProperty().bind(inputScroll.widthProperty());
		split.getItems().addAll(inputScroll, outputScroll);
		
		return instance;
	}

	@Override
	public Class<DefinedServiceInterface> getArtifactClass() {
		return DefinedServiceInterface.class;
	}
	
}
