package be.nabu.eai.developer.managers;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;

import javafx.geometry.Orientation;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import be.nabu.eai.developer.MainController;
import be.nabu.eai.developer.managers.base.BaseArtifactGUIInstance;
import be.nabu.eai.developer.managers.base.BaseGUIManager;
import be.nabu.eai.developer.managers.util.ElementClipboardHandler;
import be.nabu.eai.developer.managers.util.RootElementWithPush;
import be.nabu.eai.repository.api.Entry;
import be.nabu.eai.repository.api.ResourceEntry;
import be.nabu.eai.repository.managers.ServiceInterfaceManager;
import be.nabu.eai.repository.resources.RepositoryEntry;
import be.nabu.jfx.control.tree.Tree;
import be.nabu.libs.property.api.Property;
import be.nabu.libs.property.api.Value;
import be.nabu.libs.services.api.DefinedServiceInterface;
import be.nabu.libs.services.vm.Pipeline;
import be.nabu.libs.types.api.Element;
import be.nabu.libs.types.api.ModifiableComplexType;
import be.nabu.libs.types.structure.Structure;

public class ServiceInterfaceGUIManager extends BaseGUIManager<DefinedServiceInterface, BaseArtifactGUIInstance<DefinedServiceInterface>> {

	public ServiceInterfaceGUIManager() {
		super("Service Interface", DefinedServiceInterface.class, new ServiceInterfaceManager());
	}

	@Override
	protected List<Property<?>> getCreateProperties() {
		return null;
	}

	@Override
	protected BaseArtifactGUIInstance<DefinedServiceInterface> newGUIInstance(Entry entry) {
		return new BaseArtifactGUIInstance<DefinedServiceInterface>(this, getArtifactManager(), entry);
	}

	@Override
	protected void setEntry(BaseArtifactGUIInstance<DefinedServiceInterface> guiInstance, ResourceEntry entry) {
		guiInstance.setEntry(entry);
	}

	@Override
	protected DefinedServiceInterface newInstance(MainController controller, RepositoryEntry entry, Value<?>...values) throws IOException {
		return new ServiceInterfaceManager.DefinedServiceInterfaceImpl(entry.getId(), new Pipeline(new Structure(), new Structure()));
	}

	@Override
	protected DefinedServiceInterface display(MainController controller, AnchorPane pane, Entry entry) throws IOException, ParseException {
		DefinedServiceInterface instance = (DefinedServiceInterface) entry.getNode().getArtifact();
		SplitPane split = new SplitPane();
		split.setOrientation(Orientation.HORIZONTAL);
		
		// show the input & output
		StructureGUIManager structureManager = new StructureGUIManager();
		VBox input = new VBox();
		RootElementWithPush element = new RootElementWithPush(
			instance.getInputDefinition(), 
			false,
			instance.getInputDefinition().getProperties()
		);
		// block all properties for the input field
		element.getBlockedProperties().addAll(element.getSupportedProperties());
		
		Tree<Element<?>> inputTree = structureManager.display(controller, input, element, instance.getInputDefinition() instanceof ModifiableComplexType, false);
		inputTree.setClipboardHandler(new ElementClipboardHandler(inputTree));
		split.getItems().add(input);
		
		AnchorPane.setTopAnchor(split, 0d);
		AnchorPane.setBottomAnchor(split, 0d);
		AnchorPane.setLeftAnchor(split, 0d);
		AnchorPane.setRightAnchor(split, 0d);
		
		VBox output = new VBox();
		element = new RootElementWithPush(
			instance.getOutputDefinition(), 
			false,
			instance.getOutputDefinition().getProperties()
		);
		// block all properties for the output field
		element.getBlockedProperties().addAll(element.getSupportedProperties());
		
		Tree<Element<?>> outputTree = structureManager.display(controller, output, element, instance.getOutputDefinition() instanceof ModifiableComplexType, false);
		outputTree.setClipboardHandler(new ElementClipboardHandler(outputTree));
		split.getItems().add(output);
		
		pane.getChildren().add(split);
		return instance;
	}

	@Override
	protected void setInstance(BaseArtifactGUIInstance<DefinedServiceInterface> guiInstance, DefinedServiceInterface instance) {
		guiInstance.setArtifact(instance);
	}
}
