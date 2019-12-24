package be.nabu.eai.developer.api;

import javafx.scene.Node;

public interface NodeContainer {
	public void close();
	public void activate();
	public Node getContent();
	public void setChanged(boolean changed);
	public boolean isFocused();
}
