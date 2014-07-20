package be.nabu.eai.developer.controllers;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Control;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import be.nabu.eai.developer.api.Component;
import be.nabu.eai.developer.api.Controller;
import be.nabu.libs.validator.api.ValidationMessage;

/**
 * For scrollpanes: on the scrollpane you need to set "fit left" and "fit right" in the "layout" tab of the editor
 * 		on the anchorpane in the scrollpane, you _need_ to set "use computed sizes" from the right click menu, or it won't detect changes
 *
 */
public class VMServiceController implements Initializable, Controller {

	private Stage stage;
	
	@FXML
	private Pane panService, panLeft, panRight, panMiddle, panInput, panOutput, panMap;
	
	@FXML
	private HBox hbxButtons;
	
	@FXML
	private Tab tabMap;
	
	@FXML
	private ScrollPane scrLeft, scrRight;
	
	private List<Node> content;
	private Scene scene;
	
	@Override
	public <C extends Controller, T extends Control> Component<C, T> getComponent( String name) {
		return null;
	}

	@Override
	public void setStage(Stage stage) {
		this.stage = stage;
	}

	@Override
	public void notify(ValidationMessage... messages) {
		// do nothing
	}

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		getTabMap().getContent().addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent event) {
				if (event.getCode() == KeyCode.F11) {
					if (content == null) {
						content = new ArrayList<Node>(getTabMap().getContent().getScene().getRoot().getChildrenUnmodifiable());
						scene = getTabMap().getContent().getScene();
						((Pane) scene.getRoot()).getChildren().clear();
						((Pane) scene.getRoot()).getChildren().add(getTabMap().getContent());
						AnchorPane.setBottomAnchor(getTabMap().getContent(), 0d);
						AnchorPane.setTopAnchor(getTabMap().getContent(), 0d);
						AnchorPane.setLeftAnchor(getTabMap().getContent(), 0d);
						AnchorPane.setRightAnchor(getTabMap().getContent(), 0d);
						((Pane) getTabMap().getContent()).prefWidthProperty().bind(scene.widthProperty());
						((Pane) getTabMap().getContent()).prefHeightProperty().bind(scene.heightProperty());
					}
					else {
						((Pane) scene.getRoot()).getChildren().clear();
						((Pane) scene.getRoot()).getChildren().addAll(content);
						content = null;
						Node content = getTabMap().getContent();
						getTabMap().setContent(new VBox());
						getTabMap().setContent(content);
					}
				}
			}
		});
		
		// make sure the left pane resizes to fit the content
		scrLeft.minWidthProperty().bind(getPanLeft().prefWidthProperty());
		scrLeft.prefWidthProperty().bind(getPanLeft().prefWidthProperty());
		scrLeft.maxWidthProperty().bind(getPanLeft().prefWidthProperty());
		
		scrRight.minWidthProperty().bind(getPanRight().prefWidthProperty());
		scrRight.prefWidthProperty().bind(getPanRight().prefWidthProperty());
		scrRight.maxWidthProperty().bind(getPanRight().prefWidthProperty());
	}

	public Stage getStage() {
		return stage;
	}

	public HBox getHbxButtons() {
		return hbxButtons;
	}

	public Pane getPanService() {
		return panService;
	}

	public Pane getPanLeft() {
		return panLeft;
	}

	public Pane getPanRight() {
		return panRight;
	}

	public Pane getPanMiddle() {
		return panMiddle;
	}

	public Pane getPanInput() {
		return panInput;
	}

	public Pane getPanOutput() {
		return panOutput;
	}

	public Pane getPanMap() {
		return panMap;
	}

	public Tab getTabMap() {
		return tabMap;
	}
}
