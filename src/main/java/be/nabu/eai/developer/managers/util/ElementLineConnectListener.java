package be.nabu.eai.developer.managers.util;

import javafx.scene.input.TransferMode;
import javafx.scene.layout.Pane;
import be.nabu.jfx.control.line.Line;
import be.nabu.jfx.control.tree.TreeCell;
import be.nabu.jfx.control.tree.drag.TreeDragDrop;
import be.nabu.jfx.control.tree.drag.TreeDragListener;
import be.nabu.libs.types.api.Element;

public final class ElementLineConnectListener implements TreeDragListener<Element<?>> {
	private final Pane target;
	private Line line;
	
	public ElementLineConnectListener(Pane target) {
		this.target = target;
	}

	@Override
	public boolean canDrag(TreeCell<Element<?>> arg0) {
		return true;
	}

	@Override
	public void drag(TreeCell<Element<?>> dragged) {
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
			.subtract(dragged.getTree().localToParentTransformProperty().get().getTy())
		);
		target.getChildren().add(line);
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
	}
}
