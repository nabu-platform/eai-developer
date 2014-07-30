package be.nabu.eai.developer.managers;

import java.io.IOException;
import java.text.ParseException;

import javafx.scene.image.ImageView;
import be.nabu.eai.developer.MainController;
import be.nabu.eai.developer.api.ArtifactGUIInstance;
import be.nabu.eai.developer.api.ArtifactGUIManager;
import be.nabu.eai.repository.api.ArtifactManager;
import be.nabu.eai.repository.api.Entry;
import be.nabu.eai.repository.managers.WSDLClientManager;
import be.nabu.eai.repository.managers.util.WSDLClient;
import be.nabu.jfx.control.tree.TreeItem;

public class WSDLClientGUIManager implements ArtifactGUIManager<WSDLClient> {

	@Override
	public ArtifactManager<WSDLClient> getArtifactManager() {
		return new WSDLClientManager();
	}

	@Override
	public String getArtifactName() {
		return "WSDL Client";
	}

	@Override
	public ImageView getGraphic() {
		return MainController.loadGraphic("wsdlclient.png");
	}

	@Override
	public Class<WSDLClient> getArtifactClass() {
		return WSDLClient.class;
	}

	@Override
	public ArtifactGUIInstance create(MainController controller, TreeItem<Entry> target) throws IOException {
		// TODO: first one that is a bit special: need name field & endpoint (& proxy (general settings) & auth?)
		// TODO: reuse the (currently non-existent) http client! it will use centralized settings for proxy, security etc
		return null;
	}

	@Override
	public ArtifactGUIInstance view(MainController controller, TreeItem<Entry> target) throws IOException, ParseException {
		// TODO: offer a "refresh" button so it can be reloaded from the original endpoint 
		return new ReadOnlyGUIInstance(target.itemProperty().get().getId());
	}

}
