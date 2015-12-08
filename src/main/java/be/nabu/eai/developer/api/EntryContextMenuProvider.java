package be.nabu.eai.developer.api;

import javafx.scene.control.Menu;
import be.nabu.eai.repository.api.Entry;

public interface EntryContextMenuProvider {
	public Menu getContext(Entry entry);
}
