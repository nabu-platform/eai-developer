package be.nabu.eai.developer.events;

public class ArtifactMoveEvent {
	private String oldId, newId;

	public ArtifactMoveEvent(String oldId, String newId) {
		this.oldId = oldId;
		this.newId = newId;
	}

	public String getOldId() {
		return oldId;
	}

	public void setOldId(String oldId) {
		this.oldId = oldId;
	}

	public String getNewId() {
		return newId;
	}

	public void setNewId(String newId) {
		this.newId = newId;
	}
	
}
