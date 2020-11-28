package be.nabu.eai.developer.api;

import be.nabu.eai.repository.api.Entry;

public interface EntryAcceptor {
	public boolean accept(Entry entry);
}