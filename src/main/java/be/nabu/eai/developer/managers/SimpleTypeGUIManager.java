package be.nabu.eai.developer.managers;

import java.io.IOException;
import java.text.ParseException;

import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import be.nabu.eai.developer.MainController;
import be.nabu.eai.developer.api.ArtifactGUIInstance;
import be.nabu.eai.developer.api.ArtifactGUIManager;
import be.nabu.eai.repository.api.ArtifactManager;
import be.nabu.eai.repository.api.Entry;
import be.nabu.jfx.control.tree.TreeItem;
import be.nabu.libs.property.api.Value;
import be.nabu.libs.types.api.DefinedSimpleType;
import be.nabu.libs.types.api.DefinedType;

@SuppressWarnings("rawtypes")
public class SimpleTypeGUIManager implements ArtifactGUIManager<DefinedSimpleType> {

	@Override
	public ArtifactManager<DefinedSimpleType> getArtifactManager() {
		return null;
	}

	@Override
	public String getArtifactName() {
		return "Simple Type";
	}

	@Override
	public ImageView getGraphic() {
		return MainController.loadGraphic("simpleType.png");
	}

	@Override
	public Class<DefinedSimpleType> getArtifactClass() {
		return DefinedSimpleType.class;
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
		
		VBox vbox = new VBox();
		DefinedSimpleType type = (DefinedSimpleType) target.itemProperty().get().getNode().getArtifact();
		if (type.getSuperType() instanceof DefinedType) {
			vbox.getChildren().add(new Label("Extends: " + ((DefinedType) type.getSuperType()).getId()));
		}
		for (Value<?> value : type.getProperties()) {
			vbox.getChildren().add(new Label(value.getProperty().getName() + " = " + value.getValue()));
		}
		pane.getChildren().add(vbox);
		return instance;
	}

}
