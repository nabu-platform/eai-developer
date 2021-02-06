package be.nabu.eai.developer.managers;

import java.io.IOException;
import java.text.ParseException;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import be.nabu.eai.developer.MainController;
import be.nabu.eai.developer.api.ArtifactGUIInstance;
import be.nabu.eai.developer.api.PortableArtifactGUIManager;
import be.nabu.eai.developer.managers.util.ElementMarshallable;
import be.nabu.eai.developer.util.EAIDeveloperUtils;
import be.nabu.eai.developer.util.ElementClipboardHandler;
import be.nabu.eai.developer.util.ElementSelectionListener;
import be.nabu.eai.developer.util.ElementTreeItem;
import be.nabu.eai.developer.util.RunService;
import be.nabu.eai.repository.api.ArtifactManager;
import be.nabu.eai.repository.api.Entry;
import be.nabu.jfx.control.tree.Tree;
import be.nabu.jfx.control.tree.TreeItem;
import be.nabu.libs.services.api.DefinedService;
import be.nabu.libs.services.api.Service;
import be.nabu.libs.types.api.Element;
import be.nabu.libs.types.base.RootElement;

public class ServiceGUIManager implements PortableArtifactGUIManager<DefinedService> {

	public static final String DATA_TYPE_SERVICE = "service";
	
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
		ReadOnlyGUIInstance instance = new ReadOnlyGUIInstance(target.itemProperty().get().getId());
		Tab tab = controller.newTab(target.itemProperty().get().getId(), instance);
		AnchorPane pane = new AnchorPane();
		display(controller, pane, (DefinedService) target.itemProperty().get().getNode().getArtifact());
		tab.setContent(pane);
		return instance;
	}
	

	@Override
	public void display(MainController controller, AnchorPane pane, DefinedService service) throws IOException, ParseException {
		SplitPane split = new SplitPane();
		split.setOrientation(Orientation.HORIZONTAL);
		Tree<Element<?>> input = new Tree<Element<?>>(new ElementMarshallable());
		EAIDeveloperUtils.addElementExpansionHandler(input);
		input.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		input.rootProperty().set(new ElementTreeItem(new RootElement(service.getServiceInterface().getInputDefinition(), "input"), null, false, false));
		input.getSelectionModel().selectedItemProperty().addListener(new ElementSelectionListener(controller, false));
		input.setClipboardHandler(new ElementClipboardHandler(input, false));
		input.getRootCell().expandedProperty().set(true);
		
		Tree<Element<?>> output = new Tree<Element<?>>(new ElementMarshallable());
		EAIDeveloperUtils.addElementExpansionHandler(output);
		output.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		output.rootProperty().set(new ElementTreeItem(new RootElement(service.getServiceInterface().getOutputDefinition(), "output"), null, false, false));
		output.getSelectionModel().selectedItemProperty().addListener(new ElementSelectionListener(controller, false));
		output.setClipboardHandler(new ElementClipboardHandler(output, false));
		output.getRootCell().expandedProperty().set(true);
		
		ScrollPane inputScroll = new ScrollPane();
		ScrollPane outputScroll = new ScrollPane();
		inputScroll.setContent(input);
		outputScroll.setContent(output);
		output.prefWidthProperty().bind(outputScroll.widthProperty());
		input.prefWidthProperty().bind(inputScroll.widthProperty());
		split.getItems().addAll(inputScroll, outputScroll);
//		makeRunnable(tab, service, controller);
		
		pane.getChildren().add(split);
		AnchorPane.setBottomAnchor(split, 0d);
		AnchorPane.setLeftAnchor(split, 0d);
		AnchorPane.setRightAnchor(split, 0d);
		AnchorPane.setTopAnchor(split, 0d);
	}

	@Override
	public Class<DefinedService> getArtifactClass() {
		return DefinedService.class;
	}
	
	public static void makeRunnable(final Tab tab, final Service service, final MainController controller) {
		// should not longer be needed as we have a global shortcut
		// additionally there is a problem with the listeners here, if you have tabs open for long, saving left and right, it appears to be stacking on a lot of listeners
		// after a while, e.g. saving that tab becomes very sluggish
//		tab.contentProperty().addListener(new ChangeListener<Node>() {
//			@Override
//			public void changed(ObservableValue<? extends Node> arg0, Node arg1, Node arg2) {
//				makeRunnable(tab, service, controller);
//			}
//		});
//		tab.getContent().addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
//			@Override
//			public void handle(KeyEvent event) {
////				if (!event.isConsumed() && event.getCode() == KeyCode.R && event.isControlDown() && !event.isShiftDown() && !event.isAltDown()) {
//				if (!event.isConsumed() && event.getCode() == KeyCode.R && event.isMetaDown()) {
//					Service serviceToRun = service;
//					// refresh the service if you can to prevent "stale" services
//					// if you save something in developer, a refresh is automatically sent to the server so it is always running the latest version
//					// if however you opened the tab a while ago, the service instance we have here may be deprecated
//					// note that this is a temporary solution as the ctrl+R shortkey should be moved to global key handlers and it should use the tab id to resolve the service
//					// this will make sure we always run the latest version
//					if (service instanceof DefinedService) {
//						Entry entry = MainController.getInstance().getRepository().getEntry(((DefinedService) service).getId());
//						if (entry != null && entry.isNode()) {
//							try {
//								serviceToRun = (Service) entry.getNode().getArtifact();
//							}
//							catch (Exception e) {
//								e.printStackTrace();
//							}
//						}
//					}
//					new RunService(serviceToRun).build(controller);
//					event.consume();
//				}
//			}
//		});
	}

}
