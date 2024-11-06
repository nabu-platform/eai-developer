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
