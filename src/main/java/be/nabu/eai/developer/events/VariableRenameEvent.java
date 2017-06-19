package be.nabu.eai.developer.events;

public class VariableRenameEvent {
	private String oldPath, newPath, impactedArtifactId;

	public VariableRenameEvent(String impactedArtifactId, String oldPath, String newPath) {
		this.impactedArtifactId = impactedArtifactId;
		this.oldPath = oldPath;
		this.newPath = newPath;
	}

	public String getOldPath() {
		return oldPath;
	}

	public void setOldPath(String oldPath) {
		this.oldPath = oldPath;
	}

	public String getNewPath() {
		return newPath;
	}

	public void setNewPath(String newPath) {
		this.newPath = newPath;
	}

	public String getImpactedArtifactId() {
		return impactedArtifactId;
	}

	public void setImpactedArtifactId(String impactedArtifactId) {
		this.impactedArtifactId = impactedArtifactId;
	}
}
