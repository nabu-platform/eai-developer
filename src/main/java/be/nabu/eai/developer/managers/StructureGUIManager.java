package be.nabu.eai.developer.managers;

import java.io.IOException;

import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Tab;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import be.nabu.eai.developer.MainController;
import be.nabu.eai.developer.api.ArtifactGUIManager;
import be.nabu.eai.developer.controllers.NameOnlyCreateController;
import be.nabu.eai.developer.managers.util.ElementTreeItem;
import be.nabu.eai.repository.api.ArtifactManager;
import be.nabu.eai.repository.handlers.StructureManager;
import be.nabu.eai.repository.resources.RepositoryEntry;
import be.nabu.jfx.control.tree.Marshallable;
import be.nabu.jfx.control.tree.Tree;
import be.nabu.jfx.control.tree.TreeItem;
import be.nabu.libs.types.api.Element;
import be.nabu.libs.types.base.RootElement;
import be.nabu.libs.types.structure.DefinedStructure;

public class StructureGUIManager implements ArtifactGUIManager<DefinedStructure> {

	@Override
	public ArtifactManager<DefinedStructure> getArtifactManager() {
		return new StructureManager();
	}

	@Override
	public String getArtifactName() {
		return "Structure";
	}

	@Override
	public ImageView getGraphic() {
		return MainController.loadGraphic("types/structure.gif");
	}

	@Override
	public void create(final MainController controller, final TreeItem<RepositoryEntry> target) throws IOException {
		FXMLLoader loader = controller.load("new.nameOnly.fxml", "Create Structure");
		final NameOnlyCreateController createController = loader.getController();
		createController.getBtnCreate().addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent arg0) {
				try {
					String name = createController.getTxtName().getText();
					RepositoryEntry entry = target.itemProperty().get().createNode(name, getArtifactManager());
					DefinedStructure structure = new DefinedStructure();
					structure.setName("root");
					getArtifactManager().save(entry, structure);
					controller.getRepositoryBrowser().refresh();
					createController.close();
					Tab tab = controller.newTab(entry.getId());
					tab.setId(entry.getId());
					AnchorPane pane = new AnchorPane();
					tab.setContent(pane);
					display(pane, entry);
				}
				catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		});
	}

	@Override
	public void view(MainController controller, TreeItem<RepositoryEntry> target) {
		Tab tab = controller.newTab(target.itemProperty().get().getId());
		AnchorPane pane = new AnchorPane();
		tab.setContent(pane);
		display(pane, target.itemProperty().get());
	}
	
	public void display(Pane pane, RepositoryEntry entry) {
		DefinedStructure structure = (DefinedStructure) entry.getNode().getArtifact();
		Tree<Element<?>> tree = new Tree<Element<?>>(new Marshallable<Element<?>>() {
			@Override
			public String marshal(Element<?> arg0) {
				return arg0.getName();
			}
		});
		tree.rootProperty().set(new ElementTreeItem(new RootElement(structure), null, true));
		pane.getChildren().add(tree);
	}
}
