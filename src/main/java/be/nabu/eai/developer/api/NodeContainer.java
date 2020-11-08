package be.nabu.eai.developer.api;

import javafx.scene.Node;

public interface NodeContainer<T> {
	public void close();
	public void activate();
	public Node getContent();
	public void setContent(Node node);
	public void setChanged(boolean changed);
	public boolean isFocused();
	public boolean isChanged();
	public T getContainer();
	public String getId();
	public Object getUserData();
}
