package be.nabu.eai.developer.api;

public interface FindFilter<T> {
	public boolean accept(T item, String newValue);
}
