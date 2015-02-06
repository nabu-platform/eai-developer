package be.nabu.eai.developer.managers.util;

import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Shape;
import be.nabu.jfx.control.line.CubicCurve;
import be.nabu.jfx.control.line.Line;
import be.nabu.jfx.control.line.QuadCurve;
import be.nabu.jfx.control.tree.TreeCell;
import be.nabu.libs.types.api.Element;

public class Mapping {
	
	public enum CurveType {
		STRAIGHT,
		QUAD,
		CUBIC
	}

	public static CurveType curveType = CurveType.QUAD;
	
	private SimpleDoubleProperty sourceX = new SimpleDoubleProperty(),
			sourceY = new SimpleDoubleProperty(),
			targetX = new SimpleDoubleProperty(),
			targetY = new SimpleDoubleProperty();

	private boolean selectOnClick = false;

	private RemoveMapping removeMapping;
	
	private Shape shape;
	
	private Circle fromCircle, toCircle; 
	
	private TreeCell<Element<?>> from;
	private TreeCell<Element<?>> to;
	
	private Pane target;

	public Mapping(Pane target, TreeCell<Element<?>> from, TreeCell<Element<?>> to) {
		this.target = target;
		this.from = from;
		this.to = to;

		drawCircles();
		
		switch(curveType) {
			case STRAIGHT:
				shape = drawLine();
			break;
			case QUAD:
				shape = drawQuadCurve();
			break;
			case CUBIC:
				shape = drawCubicCurve();
			break;
		}
		setEvents(shape);
		
		target.getChildren().add(shape);
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
	
	private Shape drawCubicCurve() {
		CubicCurve curve = new CubicCurve();
		curve.eventSizeProperty().set(10);
		curve.setFill(null);
		curve.startXProperty().bind(sourceXProperty());
		curve.startYProperty().bind(sourceYProperty());
		curve.endXProperty().bind(targetXProperty());
		curve.endYProperty().bind(targetYProperty());
		curve.setManaged(false);
		// still not optimal
		curve.controlX1Property().bind(targetXProperty().subtract(sourceXProperty()));
		curve.controlY1Property().bind(sourceYProperty());
		curve.controlX2Property().bind(targetXProperty());
		curve.controlY2Property().bind(targetYProperty());
		return curve;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Shape drawQuadCurve() {
		QuadCurve curve = new QuadCurve();
		curve.eventSizeProperty().set(10);
		curve.setFill(null);
		curve.startXProperty().bind(sourceXProperty());
		curve.startYProperty().bind(sourceYProperty());
		curve.endXProperty().bind(targetXProperty());
		curve.endYProperty().bind(targetYProperty());
		curve.setManaged(false);
		curve.controlYProperty().bind(new ComparableAmountListener(targetYProperty(), sourceYProperty()).maxProperty());
		curve.controlXProperty().bind(sourceXProperty().add(targetXProperty().subtract(sourceXProperty()).divide(2d)));
		return curve;
	}
	
	private Shape drawLine() {
		Line line = new Line();
		line.eventSizeProperty().set(10);
		line.startXProperty().bind(sourceXProperty());
		line.startYProperty().bind(sourceYProperty());
		line.endXProperty().bind(targetXProperty());
		line.endYProperty().bind(targetYProperty());
		line.setManaged(false);
		return line;
	}
	
	private void setEvents(Shape shape) {		
		shape.getStyleClass().add("connectionLine");
		shape.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				event.consume();
				if (selectOnClick) {
					from.select();
					to.select();
				}
				else {
					from.show();
					to.show();
				}
				shape.requestFocus();
			}
		});
		shape.addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent event) {
				System.out.println("key down!");
				if (event.getCode() == KeyCode.DELETE) {
					if (removeMapping != null) {
						if (removeMapping.remove(Mapping.this)) {
							remove();
						}
					}
					event.consume();
				}
			}
		});
		shape.setOnMouseEntered(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent arg0) {
				from.getCellValue().getNode().getStyleClass().remove("lineDehover");
				to.getCellValue().getNode().getStyleClass().remove("lineDehover");
				from.getCellValue().getNode().getStyleClass().add("lineHover");
				to.getCellValue().getNode().getStyleClass().add("lineHover");
			}
		});
		shape.setOnMouseExited(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent arg0) {
				from.getCellValue().getNode().getStyleClass().remove("lineHover");
				to.getCellValue().getNode().getStyleClass().remove("lineHover");
				from.getCellValue().getNode().getStyleClass().add("lineDehover");
				to.getCellValue().getNode().getStyleClass().add("lineDehover");
			}		
		});
	}
	
	public Shape getShape() {
		return shape;
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
	public RemoveMapping getRemoveMapping() {
		return removeMapping;
	}
	public void setRemoveMapping(RemoveMapping removeMapping) {
		this.removeMapping = removeMapping;
	}
	public void remove() {
		target.getChildren().remove(shape);
		target.getChildren().remove(fromCircle);
		target.getChildren().remove(toCircle);
	}
	
	public static interface RemoveMapping {
		public boolean remove(Mapping mapping);
	}
	
}
