package marchoffools.client.core;

public interface SceneContext {
	void switchScene(Scene newScene);
	void switchSceneWithoutHistory(Scene newScene);
    void clearHistory();
	void goBack();
	Scene getCurrentScene();
}