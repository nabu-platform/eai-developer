package be.nabu.eai.developer.api;

import javafx.scene.control.MenuItem;
import be.nabu.eai.repository.api.Entry;

public interface EntryContextMenuProvider {
	public MenuItem getContext(Entry entry);
}
