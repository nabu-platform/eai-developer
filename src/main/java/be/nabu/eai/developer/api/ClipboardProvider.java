package be.nabu.eai.developer.api;

public interface ClipboardProvider<T> {
	public String getDataType();
	public String serialize(T instance);
	public T deserialize(String content);
	public Class<T> getClipboardClass();
}
