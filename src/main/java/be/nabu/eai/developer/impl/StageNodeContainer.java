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

package be.nabu.eai.developer.impl;

import be.nabu.eai.developer.api.NodeContainer;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ScrollPane;
import javafx.stage.Stage;

public class StageNodeContainer implements NodeContainer<Stage> {

	private Stage stage;

	public StageNodeContainer(Stage stage) {
		this.stage = stage;
	}
	
	@Override
	public void close() {
		stage.hide();
	}

	@Override
	public void activate() {
		stage.requestFocus();
	}

	@Override
	public Node getContent() {
		return stage.getScene().getRoot();
	}

	@Override
	public void setChanged(boolean changed) {
		if (changed) {
			if (!stage.getTitle().endsWith("*")) {
				stage.setTitle(stage.getTitle() + " *");
			}
		}
		else {
			if (stage.getTitle().endsWith("*")) {
				stage.setTitle(stage.getTitle().replaceAll("[\\s]*\\*$", ""));
			}
		}
	}

	@Override
	public boolean isFocused() {
		return stage.isFocused();
	}

	@Override
	public void setContent(Node node) {
		Node lookup = stage.getScene().getRoot().lookup("#content-wrapper");
		if (lookup instanceof ScrollPane) {
			((ScrollPane) lookup).setContent(node);
		}
		else {
			stage.getScene().setRoot((Parent) node);
		}
	}

	@Override
	public Stage getContainer() {
		return stage;
	}

	@Override
	public String getId() {
		return stage.getTitle();
	}

	@Override
	public boolean isChanged() {
		return stage.getTitle().endsWith("*");
	}

	@Override
	public Object getUserData() {
		return stage.getUserData();
	}
	
}
