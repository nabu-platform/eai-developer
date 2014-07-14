package be.nabu.eai.developer.handlers;

import java.io.IOException;

import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import be.nabu.eai.developer.MainController;
import be.nabu.eai.developer.api.ArtifactGUIManager;
import be.nabu.eai.developer.controllers.NameOnlyCreateController;
import be.nabu.eai.repository.api.ArtifactManager;
import be.nabu.eai.repository.handlers.StructureManager;
import be.nabu.eai.repository.resources.RepositoryEntry;
import be.nabu.jfx.control.tree.TreeItem;
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
				}
				catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		});
	}

	@Override
	public void view(MainController controller, TreeItem<RepositoryEntry> target) {
		// TODO Auto-generated method stub
		
	}
}
