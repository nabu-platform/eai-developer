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

import javafx.scene.input.TransferMode;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import be.nabu.jfx.control.line.CubicCurve;
import be.nabu.jfx.control.line.Line;
import be.nabu.jfx.control.tree.TreeCell;
import be.nabu.jfx.control.tree.drag.TreeDragDrop;
import be.nabu.jfx.control.tree.drag.TreeDragListener;
import be.nabu.libs.types.api.Element;

public final class ElementLineConnectListener implements TreeDragListener<Element<?>> {
	private final Pane target;
	private Line line;
	private CubicCurve curve;
	private Circle circle;
	private boolean useCurves = true;
	
	public ElementLineConnectListener(Pane target) {
		this.target = target;
	}

	@Override
	public boolean canDrag(TreeCell<Element<?>> arg0) {
		return true;
	}

	@Override
	public void drag(TreeCell<Element<?>> dragged) {
		if (!useCurves) {
			line = new Line();
			line.startXProperty().bind(dragged.rightAnchorXProperty().add(10)
				.add(dragged.getTree().localToSceneTransformProperty().get().getTx())
				.subtract(target.localToSceneTransformProperty().get().getTx())
				.subtract(dragged.getTree().localToParentTransformProperty().get().getTx())
			);
			line.startYProperty().bind(dragged.rightAnchorYProperty()
				.add(dragged.getTree().localToSceneTransformProperty().get().getTy())
				.subtract(target.localToSceneTransformProperty().get().getTy())
				.subtract(dragged.getTree().localToParentTransformProperty().get().getTy())
			);
			// move it by one pixel, otherwise it will keep triggering events on the line instead of what is underneath it
			line.endXProperty().bind(TreeDragDrop.getMouseLocation(dragged.getTree()).xProperty().subtract(1)
				.subtract(target.localToSceneTransformProperty().get().getTx())
	//			.subtract(dragged.getTree().localToParentTransformProperty().get().getTx())
			);
			line.endYProperty().bind(TreeDragDrop.getMouseLocation(dragged.getTree()).yProperty()
				.subtract(target.localToSceneTransformProperty().get().getTy())
	//			.subtract(dragged.getTree().localToParentTransformProperty().get().getTy())
			);
			target.getChildren().add(line);
		}
		else {
			curve = new CubicCurve();
			curve.getStyleClass().add("connectionLine");
			curve.startXProperty().bind(dragged.rightAnchorXProperty().add(10)
				.add(dragged.getTree().localToSceneTransformProperty().get().getTx())
				.subtract(target.localToSceneTransformProperty().get().getTx())
				.subtract(dragged.getTree().localToParentTransformProperty().get().getTx())
			);
			curve.startYProperty().bind(dragged.rightAnchorYProperty()
				.add(dragged.getTree().localToSceneTransformProperty().get().getTy())
				.subtract(target.localToSceneTransformProperty().get().getTy())
				.subtract(dragged.getTree().localToParentTransformProperty().get().getTy())
			);
			// move it by one pixel, otherwise it will keep triggering events on the line instead of what is underneath it
			curve.endXProperty().bind(TreeDragDrop.getMouseLocation(dragged.getTree()).xProperty().subtract(1)
				.subtract(target.localToSceneTransformProperty().get().getTx())
	//				.subtract(dragged.getTree().localToParentTransformProperty().get().getTx())
			);
			curve.endYProperty().bind(TreeDragDrop.getMouseLocation(dragged.getTree()).yProperty().subtract(1)
				.subtract(target.localToSceneTransformProperty().get().getTy())
	//				.subtract(dragged.getTree().localToParentTransformProperty().get().getTy())
			);
			curve.controlX1Property().bind(curve.startXProperty().add(100));
			curve.controlY1Property().bind(curve.startYProperty());
			// we don't want curvature at the end! the mouse is hovering over it anyway and we would need to calculate whether its left or right etc
			curve.controlX2Property().bind(curve.endXProperty());
			curve.controlY2Property().bind(curve.endYProperty());
			curve.setFill(null);
			
			circle = new Circle();
			circle.centerXProperty().bind(curve.startXProperty());
			circle.centerYProperty().bind(curve.startYProperty());
			circle.setRadius(2);
			circle.getStyleClass().add("connectionCircle");
			
			target.getChildren().addAll(curve, circle);
		}
	}

	@Override
	public String getDataType(TreeCell<Element<?>> arg0) {
		return "type";
	}

	@Override
	public TransferMode getTransferMode() {
		return TransferMode.LINK;
	}

	@Override
	public void stopDrag(TreeCell<Element<?>> arg0, boolean arg1) {
		if (line != null && line.getParent() != null && ((Pane) line.getParent()).getChildren() != null) {
			((Pane) line.getParent()).getChildren().remove(line);
			line = null;
		}
		if (curve != null && curve.getParent() != null && ((Pane) curve.getParent()).getChildren() != null) {
			((Pane) curve.getParent()).getChildren().remove(curve);
			curve = null;
		}
		if (circle != null && circle.getParent() != null && ((Pane) circle.getParent()).getChildren() != null) {
			((Pane) circle.getParent()).getChildren().remove(circle);
			circle = null;
		}
	}
}
