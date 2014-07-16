package be.nabu.eai.developer.managers;

import java.io.IOException;
import java.text.ParseException;

import javafx.fxml.FXMLLoader;
import javafx.scene.image.ImageView;
import be.nabu.eai.developer.MainController;
import be.nabu.eai.developer.api.ArtifactGUIInstance;
import be.nabu.eai.developer.api.ArtifactGUIManager;
import be.nabu.eai.repository.api.ArtifactManager;
import be.nabu.eai.repository.managers.VMServiceManager;
import be.nabu.eai.repository.resources.RepositoryEntry;
import be.nabu.jfx.control.tree.TreeItem;
import be.nabu.libs.services.vm.VMService;

public class VMServiceGUIManager implements ArtifactGUIManager<VMService> {

	@Override
	public ArtifactManager<VMService> getArtifactManager() {
		return new VMServiceManager();
	}

	@Override
	public String getArtifactName() {
		return "Service";
	}

	@Override
	public ImageView getGraphic() {
		return MainController.loadGraphic("step/sequence.gif");
	}

	@Override
	public ArtifactGUIInstance create(MainController controller, TreeItem<RepositoryEntry> target) throws IOException {
		FXMLLoader loader = controller.load("new.nameOnly.fxml", "Create Service");
		return null;
	}

	@Override
	public ArtifactGUIInstance view(MainController controller,
			TreeItem<RepositoryEntry> target) throws IOException,
			ParseException {
		// TODO Auto-generated method stub
		return null;
	}

}
