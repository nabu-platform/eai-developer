package be.nabu.eai.developer.managers.util;

import be.nabu.jfx.control.line.Line;
import be.nabu.jfx.control.tree.TreeCell;
import be.nabu.libs.types.api.Element;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;

public class Mapping {
	private SimpleDoubleProperty sourceX = new SimpleDoubleProperty(),
			sourceY = new SimpleDoubleProperty(),
			targetX = new SimpleDoubleProperty(),
			targetY = new SimpleDoubleProperty();

	private boolean selectOnClick = true;

	private Line line;
	
	private Circle fromCircle, toCircle; 
	
	private TreeCell<Element<?>> from;
	private TreeCell<Element<?>> to;
	
	private Pane target;

	public Mapping(Pane target, TreeCell<Element<?>> from, TreeCell<Element<?>> to) {
		this.target = target;
		this.from = from;
		this.to = to;

		drawCircles();
		drawLine();
		target.getChildren().add(line);
		target.getChildren().add(fromCircle);
		target.getChildren().add(toCircle);

		// we need to add the offset to the parent
		// instead of recursively determining this, first add a toscene of the tree, then substract a toscene of the target and also substract the parent offset
		RelativeLocationListener targetTransform = new RelativeLocationListener(target.localToSceneTransformProperty());
		RelativeLocationListener fromSceneTransform = new RelativeLocationListener(from.getTree().localToSceneTransformProperty());
		RelativeLocationListener toSceneTransform = new RelativeLocationListener(to.getTree().localToSceneTransformProperty());
		RelativeLocationListener fromParentTransform = new RelativeLocationListener(from.getTree().localToParentTransformProperty());
		RelativeLocationListener toParentTransform = new RelativeLocationListener(to.getTree().localToParentTransformProperty());
		
		sourceX.bind(from.rightAnchorXProperty().add(10)
			.add(fromSceneTransform.xProperty())
			.subtract(targetTransform.xProperty())
			.subtract(fromParentTransform.xProperty()));
		sourceY.bind(from.rightAnchorYProperty()
			.add(fromSceneTransform.yProperty())
			.subtract(targetTransform.yProperty())
			.subtract(fromParentTransform.yProperty()));
		targetX.bind(to.leftAnchorXProperty().subtract(10)
			.add(toSceneTransform.xProperty())
			.subtract(targetTransform.xProperty())
			.subtract(toParentTransform.xProperty()));
		targetY.bind(to.leftAnchorYProperty()
			.add(toSceneTransform.yProperty())
			.subtract(targetTransform.yProperty())
			.subtract(toParentTransform.yProperty()));
	}
	private void drawLine() {
		line = new Line();
		line.eventSizeProperty().set(10);
		line.startXProperty().bind(sourceXProperty());
		line.startYProperty().bind(sourceYProperty());
		line.endXProperty().bind(targetXProperty());
		line.endYProperty().bind(targetYProperty());
		line.setManaged(false);
		
		line.getStyleClass().add("connectionLine");
		line.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				if (selectOnClick) {
					from.select();
					to.select();
				}
				else {
					from.show();
					to.show();
				}
				event.consume();
			}
		});
		line.setOnMouseEntered(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent arg0) {
				from.getCellValue().getNode().getStyleClass().remove("lineDehover");
				to.getCellValue().getNode().getStyleClass().remove("lineDehover");
				from.getCellValue().getNode().getStyleClass().add("lineHover");
				to.getCellValue().getNode().getStyleClass().add("lineHover");
			}
		});
		line.setOnMouseExited(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent arg0) {
				from.getCellValue().getNode().getStyleClass().remove("lineHover");
				to.getCellValue().getNode().getStyleClass().remove("lineHover");
				from.getCellValue().getNode().getStyleClass().add("lineDehover");
				to.getCellValue().getNode().getStyleClass().add("lineDehover");
			}		
		});
	}
	
	private void drawCircles() {
		fromCircle = new Circle();
		fromCircle.centerXProperty().bind(sourceXProperty());
		fromCircle.centerYProperty().bind(sourceYProperty());
		fromCircle.setRadius(2);
		fromCircle.getStyleClass().add("connectionCircle");
		fromCircle.setManaged(false);
		
		toCircle = new Circle();
		toCircle.centerXProperty().bind(targetXProperty());
		toCircle.centerYProperty().bind(targetYProperty());
		toCircle.setRadius(2);
		toCircle.getStyleClass().add("connectionCircle");
		toCircle.setManaged(false);
	}
	
	public static void addToParent(Node currentNode, Node node) {
		if (currentNode.getParent() instanceof Pane)
			((Pane) currentNode.getParent()).getChildren().add(node);
		else
			addToParent(currentNode.getParent(), node);
	}
	public static Pane getPaneParent(Node currentNode) {
		if (currentNode.getParent() instanceof Pane)
			return (Pane) currentNode.getParent();
		else
			return getPaneParent(currentNode.getParent());
	}
	
	public ReadOnlyDoubleProperty sourceYProperty() {
		return sourceY;
	}
	public ReadOnlyDoubleProperty sourceXProperty() {
		return sourceX;
	}
	public ReadOnlyDoubleProperty targetYProperty() {
		return targetY;
	}
	public ReadOnlyDoubleProperty targetXProperty() {
		return targetX;
	}

	public TreeCell<Element<?>> getFrom() {
		return from;
	}
	
	public TreeCell<Element<?>> getTo() {
		return to;
	}

	public boolean isSelectOnClick() {
		return selectOnClick;
	}

	public void setSelectOnClick(boolean selectOnClick) {
		this.selectOnClick = selectOnClick;
	}	

	public void remove() {
		target.getChildren().remove(line);
		target.getChildren().remove(fromCircle);
		target.getChildren().remove(toCircle);
	}
}
