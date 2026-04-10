package shared;

import java.io.Serializable;

public class ServerResponse implements Serializable {
	private static final long serialVersionUID = 1L;

	private boolean success;
	private String message;
	private SceneData scene;

	public ServerResponse(boolean success, String message, SceneData scene) {
		this.success = success;
		this.message = message;
		this.scene = scene;
	}

	public boolean isSuccess() {
		return success;
	}

	public String getMessage() {
		return message;
	}

	public SceneData getScene() {
		return scene;
	}
}