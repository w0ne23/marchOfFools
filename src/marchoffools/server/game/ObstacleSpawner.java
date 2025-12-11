package marchoffools.server.game;

import java.util.UUID;

import marchoffools.common.message.GameStateMessage.ObstacleData;
import marchoffools.common.model.ObstacleType;

/**
 * 장애물 생성 시스템
 */
public class ObstacleSpawner {

    private static final long SPAWN_INTERVAL = 2000; // 2초마다
    private static final double SPAWN_X = 1366; // 화면 오른쪽 끝
    
    private long lastSpawnTime = 0;

    /**
     * 장애물 생성 시도
     */
    public void trySpawn(TrackController track) {
        long now = System.currentTimeMillis();
        
        if (now - lastSpawnTime >= SPAWN_INTERVAL) {
            ObstacleData obstacle = createObstacle();
            track.addObstacle(obstacle);
            lastSpawnTime = now;
            
            System.out.println("[ObstacleSpawner] Spawned: type=" + obstacle.getType() + " at x=" + obstacle.getX());
        }
    }

    /**
     * 장애물 생성
     */
    private ObstacleData createObstacle() {
        // TODO: 구체화
        
        // 현재는 랜덤 생성만
        ObstacleType type = ObstacleType.random();
        
        String id = UUID.randomUUID().toString();
        
        return new ObstacleData(id, SPAWN_X, type.getY(), type.getId());
    }
}