package be.nabu.eai.developer.managers;

import java.io.IOException;
import java.text.ParseException;

import javafx.scene.control.Tab;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import be.nabu.eai.developer.MainController;
import be.nabu.eai.developer.api.ArtifactGUIInstance;
import be.nabu.eai.developer.api.ArtifactGUIManager;
import be.nabu.eai.developer.managers.util.ElementMarshallable;
import be.nabu.eai.developer.util.ElementSelectionListener;
import be.nabu.eai.developer.util.ElementTreeItem;
import be.nabu.eai.repository.api.ArtifactManager;
import be.nabu.eai.repository.api.Entry;
import be.nabu.jfx.control.tree.Tree;
import be.nabu.jfx.control.tree.TreeItem;
import be.nabu.libs.types.api.ComplexType;
import be.nabu.libs.types.api.DefinedType;
import be.nabu.libs.types.api.Element;
import be.nabu.libs.types.base.RootElement;

public class TypeGUIManager implements ArtifactGUIManager<DefinedType> {

	@Override
	public ArtifactManager<DefinedType> getArtifactManager() {
		return null;
	}

	@Override
	public String getArtifactName() {
		return "Type";
	}

	@Override
	public ImageView getGraphic() {
		return MainController.loadGraphic("complexType.png");
	}

	@Override
	public Class<DefinedType> getArtifactClass() {
		return DefinedType.class;
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
		tab.setContent(pane);
		
		DefinedType type = (DefinedType) target.itemProperty().get().getNode().getArtifact();
		Tree<Element<?>> tree = new Tree<Element<?>>(new ElementMarshallable());
		tree.rootProperty().set(new ElementTreeItem(new RootElement((ComplexType) type), null, false, false));
		tree.getSelectionModel().selectedItemProperty().addListener(new ElementSelectionListener(controller, false));
		pane.getChildren().add(tree);
		
		AnchorPane.setBottomAnchor(tree, 0d);
		AnchorPane.setTopAnchor(tree, 0d);
		AnchorPane.setLeftAnchor(tree, 0d);
		AnchorPane.setRightAnchor(tree, 0d);
		
		return instance;
	}

}
