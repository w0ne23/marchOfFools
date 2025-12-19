package marchoffools.server.game.spawn;

import marchoffools.common.message.GameStateMessage.ObstacleData;

public interface SpawnStrategy {
	void startSpawn(long initialDelay);
    boolean shouldSpawn(long currentTime);
    ObstacleData createObstacle();
    void reset();
}