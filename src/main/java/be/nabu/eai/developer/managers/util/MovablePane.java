/*
* Copyright (C) 2014 Alexander Verbruggen
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU Lesser General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public License
* along with this program. If not, see <https://www.gnu.org/licenses/>.
*/

package be.nabu.eai.developer.managers.util;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import be.nabu.eai.developer.MainController;
import be.nabu.jfx.control.tree.drag.MouseLocation;
import be.nabu.jfx.control.tree.drag.TreeDragDrop;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.input.DataFormat;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;

public class MovablePane {
	
	private Node target;

	private DoubleProperty x = new SimpleDoubleProperty(), y = new SimpleDoubleProperty();
	
	private static Map<Node, MovablePane> targets = new HashMap<Node, MovablePane>();
	
	private ReadOnlyBooleanProperty lock;
	
	private int gridSize;
	
	private boolean managedOnStart;
	
	private EventHandler<DragEvent> dragDoneHandler;

	private Scene scene;
	
	public static MovablePane makeMovable(Node target, ReadOnlyBooleanProperty lock) {
		if (!targets.containsKey(target)) {
			targets.put(target, new MovablePane(target, lock));
		}
		return targets.get(target);
	}
	private MovablePane(Node target, ReadOnlyBooleanProperty lock) {
		this.target = target;
		this.lock = lock;
		initialize();
	}
	
	private void initialize() {
		if (target.getId() == null) {
			target.setId(UUID.randomUUID().toString());
		}
		scene = target.getScene() == null ? MainController.getInstance().getStage().getScene() : target.getScene();
		target.addEventHandler(MouseEvent.ANY, MouseLocation.getInstance(scene).getMouseHandler());
		target.addEventHandler(DragEvent.ANY, MouseLocation.getInstance(scene).getDragHandler());
		EventHandler<DragEvent> sceneDragListener = new EventHandler<DragEvent>() {
			@Override
			public void handle(DragEvent event) {
				Object content = event.getDragboard().getContent(TreeDragDrop.getDataFormat("pane"));
				if (content != null && target.layoutXProperty().isBound()) {
					target.layoutXProperty().unbind();
					target.layoutYProperty().unbind();
//					target.layoutXProperty().set(x.get());
//					target.layoutYProperty().set(y.get());
					event.consume();
					if (managedOnStart) {
						target.setManaged(true);
						// trigger a layout?
						target.getParent().layout();
					}
					if (dragDoneHandler != null) {
						dragDoneHandler.handle(event);
					}
				}
			}
		};
		scene.addEventHandler(DragEvent.DRAG_DONE, sceneDragListener);
		
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
		target.addEventHandler(MouseEvent.DRAG_DETECTED, new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				if (lock.get()) {
					if (!event.isControlDown()) {
						// make sure we don't have it managed during drag/drop
						if (target.isManaged()) {
							managedOnStart = true;
							target.setManaged(false);
						}
						target.layoutXProperty().bind(new IncrementalAmountListener<Number>(
							MouseLocation.getInstance(scene).xProperty().subtract(target.getParent().localToSceneTransformProperty().get().getTx()), 
							gridSize,
							target.getLayoutX()).valueProperty());
						target.layoutYProperty().bind(new IncrementalAmountListener<Number>(
							MouseLocation.getInstance(scene).yProperty().subtract(target.getParent().localToSceneTransformProperty().get().getTy()), 
							gridSize,
							target.getLayoutY()).valueProperty());
						Dragboard dragboard = target.startDragAndDrop(TransferMode.MOVE);
						Map<DataFormat, Object> content = new HashMap<DataFormat, Object>();
						content.put(TreeDragDrop.getDataFormat("pane"), target.getId());
						dragboard.setContent(content);
						event.consume();
					}
				}
			}
		});
		target.addEventHandler(DragEvent.DRAG_DROPPED, new EventHandler<DragEvent>() {
			@Override
			public void handle(DragEvent event) {
				Object content = event.getDragboard().getContent(TreeDragDrop.getDataFormat("pane"));
				// it is possible that something else (e.g. an incoming line) is dropped on here
				if (content != null && target.layoutXProperty().isBound()) {
					target.layoutXProperty().unbind();
					target.layoutYProperty().unbind();
//					target.layoutXProperty().set(x.get());
//					target.layoutYProperty().set(y.get());
					event.consume();
					if (managedOnStart) {
						target.setManaged(true);
						// trigger a layout?
						target.getParent().layout();
					}
					if (dragDoneHandler != null) {
						dragDoneHandler.handle(event);
					}
				}
			}
		});
		// allow updating of scene
		target.sceneProperty().addListener(new ChangeListener<Scene>() {
			@Override
			public void changed(ObservableValue<? extends Scene> arg0, Scene arg1, Scene arg2) {
				// unregister previous listeners
				if (arg1 != null) {
					target.removeEventHandler(MouseEvent.ANY, MouseLocation.getInstance(arg1).getMouseHandler());
					target.removeEventHandler(DragEvent.ANY, MouseLocation.getInstance(arg1).getDragHandler());
					arg1.removeEventHandler(DragEvent.DRAG_DONE, sceneDragListener);
				}
				// update scene
				scene = arg2 == null ? MainController.getInstance().getStage().getScene() : arg2;
				// set new listeners
				target.addEventHandler(MouseEvent.ANY, MouseLocation.getInstance(scene).getMouseHandler());
				target.addEventHandler(DragEvent.ANY, MouseLocation.getInstance(scene).getDragHandler());
				scene.addEventHandler(DragEvent.DRAG_DONE, sceneDragListener);
			}
		});
	}

	public ReadOnlyDoubleProperty xProperty() {
		return x;
	}
	public ReadOnlyDoubleProperty yProperty() {
		return y;
	}
	public int getGridSize() {
		return gridSize;
	}
	public void setGridSize(int gridSize) {
		this.gridSize = gridSize;
	}
	public EventHandler<DragEvent> getDragDoneHandler() {
		return dragDoneHandler;
	}
	public void setDragDoneHandler(EventHandler<DragEvent> dragDoneHandler) {
		this.dragDoneHandler = dragDoneHandler;
	}
}
