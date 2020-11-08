package be.nabu.eai.developer.impl;

import be.nabu.eai.developer.api.NodeContainer;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

public class TabNodeContainer implements NodeContainer<Tab> {

	private TabPane parent;
	private Tab tab;

	public TabNodeContainer(Tab tab, TabPane parent) {
		this.tab = tab;
		this.parent = parent;
	}
	
	@Override
	public void close() {
		parent.getTabs().remove(tab);
	}

	@Override
	public void activate() {
		parent.getSelectionModel().select(tab);
	}

	@Override
	public Node getContent() {
		return tab.getContent();
	}

	@Override
	public void setChanged(boolean changed) {
		if (changed) {
			if (!tab.getText().endsWith("*")) {
				tab.setText(tab.getText() + " *");
			}
		}
		else {
			if (tab.getText().endsWith("*")) {
				tab.setText(tab.getText().replaceAll("[\\s]*\\*$", ""));
			}
		}
	}

	@Override
	public boolean isFocused() {
		return tab.isSelected();
	}

	@Override
	public void setContent(Node node) {
		tab.setContent(node);
	}

	@Override
	public Tab getContainer() {
		return tab;
	}

	@Override
	public String getId() {
		return tab.getId();
	}

	@Override
	public boolean isChanged() {
		return tab.getText().endsWith("*");
	}

	@Override
	public Object getUserData() {
		return tab.getUserData();
	}

}
