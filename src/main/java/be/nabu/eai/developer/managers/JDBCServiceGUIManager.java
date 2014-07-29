package be.nabu.eai.developer.managers;

import java.io.IOException;
import java.text.ParseException;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Orientation;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import be.nabu.eai.developer.MainController;
import be.nabu.eai.developer.api.ArtifactGUIInstance;
import be.nabu.eai.developer.api.ArtifactGUIManager;
import be.nabu.eai.developer.controllers.NameOnlyCreateController;
import be.nabu.eai.developer.managers.util.ElementMarshallable;
import be.nabu.eai.developer.managers.util.ElementTreeItem;
import be.nabu.eai.repository.api.ArtifactManager;
import be.nabu.eai.repository.api.Entry;
import be.nabu.eai.repository.api.ResourceEntry;
import be.nabu.eai.repository.managers.JDBCServiceManager;
import be.nabu.eai.repository.resources.RepositoryEntry;
import be.nabu.jfx.control.tree.Tree;
import be.nabu.jfx.control.tree.TreeItem;
import be.nabu.libs.artifacts.jdbc.JDBCPool;
import be.nabu.libs.services.jdbc.JDBCService;
import be.nabu.libs.types.api.Element;
import be.nabu.libs.types.base.RootElement;

public class JDBCServiceGUIManager implements ArtifactGUIManager<JDBCService> {

	@Override
	public ArtifactManager<JDBCService> getArtifactManager() {
		return new JDBCServiceManager();
	}

	@Override
	public String getArtifactName() {
		return "JDBC Service";
	}

	@Override
	public ImageView getGraphic() {
		return MainController.loadGraphic("jdbcservice.png");
	}

	@Override
	public Class<JDBCService> getArtifactClass() {
		return JDBCService.class;
	}

	@Override
	public ArtifactGUIInstance create(final MainController controller, final TreeItem<Entry> target) throws IOException {
		FXMLLoader loader = controller.load("new.nameOnly.fxml", "Create JDBC Service", true);
		final NameOnlyCreateController createController = loader.getController();
		final JDBCServiceGUIInstance instance = new JDBCServiceGUIInstance((ResourceEntry) target.itemProperty().get());
		createController.getBtnCreate().addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent arg0) {
				try {
					String name = createController.getTxtName().getText();
					RepositoryEntry entry = ((RepositoryEntry) target.itemProperty().get()).createNode(name, getArtifactManager());
					JDBCService service = new JDBCService(target.itemProperty().get().getId());
					getArtifactManager().save(entry, service);
					controller.getRepositoryBrowser().refresh();
					createController.close();
					Tab tab = controller.newTab(entry.getId());
					AnchorPane pane = new AnchorPane();
					tab.setContent(pane);
					instance.setService(display(controller, pane, entry));
					ServiceGUIManager.makeRunnable(tab, service, controller);
				}
				catch (IOException e) {
					throw new RuntimeException(e);
				}
				catch (ParseException e) {
					throw new RuntimeException(e);
				}
			}
		});
		return instance;
	}

	@Override
	public ArtifactGUIInstance view(MainController controller, TreeItem<Entry> target) throws IOException, ParseException {
		JDBCServiceGUIInstance instance = new JDBCServiceGUIInstance((ResourceEntry) target.itemProperty().get());
		Tab tab = controller.newTab(target.itemProperty().get().getId());
		AnchorPane pane = new AnchorPane();
		tab.setContent(pane);
		instance.setService(display(controller, pane, target.itemProperty().get()));
		ServiceGUIManager.makeRunnable(tab, instance.getService(), controller);
		return instance;
	}

	private JDBCService display(final MainController controller, Pane pane, final Entry entry) throws IOException, ParseException {
		final JDBCService service = (JDBCService) entry.getNode().getArtifact();
		SplitPane main = new SplitPane();
		
		AnchorPane top = new AnchorPane();
		main.setOrientation(Orientation.VERTICAL);
		SplitPane iface = new SplitPane();
		iface.setOrientation(Orientation.HORIZONTAL);
		main.getItems().addAll(top, iface);
		
		AnchorPane left = new AnchorPane();
		final Tree<Element<?>> input = new Tree<Element<?>>(new ElementMarshallable());
		input.rootProperty().set(new ElementTreeItem(new RootElement(service.getInput()), null, false, false));
		left.getChildren().add(input);
		
		AnchorPane right = new AnchorPane();
		final Tree<Element<?>> output = new Tree<Element<?>>(new ElementMarshallable());
		output.rootProperty().set(new ElementTreeItem(new RootElement(service.getOutput()), null, false, false));
		right.getChildren().add(output);
		
		iface.getItems().addAll(left, right);
		
		VBox vbox = new VBox();
		HBox hbox = new HBox();
		TextField field = new TextField(service.getConnectionId());
		field.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> arg0, String arg1, String arg2) {
				if (arg2 != null) {
					try {
						if (entry.getRepository().getNode(arg2) != null && entry.getRepository().getNode(arg2).getArtifact() instanceof JDBCPool) {
							service.setConnectionId(arg2);
						}
					}
					catch (IOException e) {
						throw new RuntimeException(e);
					}
					catch (ParseException e) {
						throw new RuntimeException(e);
					}
				}
			}
		});
		hbox.getChildren().addAll(new Label("Connection ID: "), field);
		final TextArea area = new TextArea();
		VBox.setVgrow(area, Priority.ALWAYS);
		area.focusedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> arg0, Boolean arg1, Boolean arg2) {
				if (!arg2) {
					service.setSql(area.getText());
					input.getTreeCell(input.rootProperty().get()).refresh();
					output.getTreeCell(output.rootProperty().get()).refresh();
				}
			}
		});
		area.setText(service.getSql());
		vbox.getChildren().addAll(hbox, area);
		top.getChildren().add(vbox);
		AnchorPane.setBottomAnchor(vbox, 0d);
		AnchorPane.setTopAnchor(vbox, 0d);
		AnchorPane.setLeftAnchor(vbox, 0d);
		AnchorPane.setRightAnchor(vbox, 0d);
		
		
		pane.getChildren().add(main);
		
		AnchorPane.setBottomAnchor(main, 0d);
		AnchorPane.setTopAnchor(main, 0d);
		AnchorPane.setLeftAnchor(main, 0d);
		AnchorPane.setRightAnchor(main, 0d);
		return service;
	}
}
