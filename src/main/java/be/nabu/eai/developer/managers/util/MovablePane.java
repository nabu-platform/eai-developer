package be.nabu.eai.developer.managers.util;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import be.nabu.jfx.control.tree.drag.MouseLocation;
import be.nabu.jfx.control.tree.drag.TreeDragDrop;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.input.DataFormat;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Pane;

public class MovablePane {
	
	private Pane target;

	private DoubleProperty x = new SimpleDoubleProperty(), y = new SimpleDoubleProperty();
	
	private static Map<Pane, MovablePane> targets = new HashMap<Pane, MovablePane>();
	
	public static MovablePane makeMovable(Pane target) {
		if (!targets.containsKey(target)) {
			targets.put(target, new MovablePane(target));
		}
		return targets.get(target);
	}
	private MovablePane(Pane target) {
		this.target = target;
		initialize();
	}
	
	private void initialize() {
		if (target.getId() == null) {
			target.setId(UUID.randomUUID().toString());
		}
		target.addEventHandler(MouseEvent.ANY, MouseLocation.getInstance(target.getScene()).getMouseHandler());
		target.addEventHandler(DragEvent.ANY, MouseLocation.getInstance(target.getScene()).getDragHandler());
		target.addEventHandler(MouseEvent.DRAG_DETECTED, new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				target.layoutXProperty().bind(MouseLocation.getInstance(target.getScene()).xProperty().subtract(target.getParent().localToSceneTransformProperty().get().getTx()));
				target.layoutYProperty().bind(MouseLocation.getInstance(target.getScene()).yProperty().subtract(target.getParent().localToSceneTransformProperty().get().getTy()));
				// for some reason the listener is triggered twice for each move: once with a positive number and once with a negative
				// fetching the last position when unbinding yields the negative one, but we need the positive one, so store it on each change
				target.layoutXProperty().addListener(new ChangeListener<Number>() {
					@Override
					public void changed(ObservableValue<? extends Number> arg0, Number arg1, Number arg2) {
						if (arg2.doubleValue() >= 0) {
							x.set(arg2.doubleValue());
						}
					}
				});
				target.layoutYProperty().addListener(new ChangeListener<Number>() {
					@Override
					public void changed(ObservableValue<? extends Number> arg0, Number arg1, Number arg2) {
						if (arg2.doubleValue() >= 0) {
							y.set(arg2.doubleValue());
						}
					}
				});
				Dragboard dragboard = target.startDragAndDrop(TransferMode.MOVE);
				Map<DataFormat, Object> content = new HashMap<DataFormat, Object>();
				content.put(TreeDragDrop.getDataFormat("pane"), target.getId());
				dragboard.setContent(content);
				event.consume();
			}
		});
		target.getScene().addEventHandler(DragEvent.DRAG_DONE, new EventHandler<DragEvent>() {
			@Override
			public void handle(DragEvent event) {
				target.layoutXProperty().unbind();
				target.layoutYProperty().unbind();
				target.layoutXProperty().set(x.get());
				target.layoutYProperty().set(y.get());
				event.consume();
			}
		});
	}

	public ReadOnlyDoubleProperty xProperty() {
		return x;
	}
	public ReadOnlyDoubleProperty yProperty() {
		return y;
	}
}
