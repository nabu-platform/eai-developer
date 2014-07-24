package be.nabu.eai.developer.managers.util;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.ContextMenu;
import be.nabu.jfx.control.tree.Tree;

public class RemoveTreeContextMenu {
	public static void removeOnHide(final Tree<?> tree) {
		tree.contextMenuProperty().addListener(new ChangeListener<ContextMenu>() {
			@Override
			public void changed(ObservableValue<? extends ContextMenu> arg0, ContextMenu arg1, ContextMenu arg2) {
				if (arg2 != null) {
					arg2.showingProperty().addListener(new ChangeListener<Boolean>() {
						@Override
						public void changed(ObservableValue<? extends Boolean> arg0, Boolean arg1, Boolean arg2) {
							if (arg1 != null && arg1) {
								tree.setContextMenu(null);
							}
						}
					});
				}
			}
		});
	}
}
