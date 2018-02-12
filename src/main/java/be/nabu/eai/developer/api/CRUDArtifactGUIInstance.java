package be.nabu.eai.developer.api;

public interface CRUDArtifactGUIInstance extends ArtifactGUIInstance {
	public void created(String id, String message, String content);
	public void updated(String id, String message, String content);
	public void deleted(String id, String message);
}
