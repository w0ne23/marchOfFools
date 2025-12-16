package marchoffools.server.game;

import java.util.Random;
import java.util.UUID;

import marchoffools.common.message.GameStateMessage.ObstacleData;
import marchoffools.common.model.GameEntity;
import marchoffools.common.model.ObstacleType;

/**
 * 장애물 생성 시스템
 */
public class ObstacleSpawner {

    private static final long SPAWN_INTERVAL = 1000; // 1초마다
    private static final double SPAWN_X = 1366; // 화면 오른쪽 끝

    private static final Random random = new Random();
    
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
            
            System.out.println("[Spawner] Spawned: type=" + obstacle.getType() + " at x=" + obstacle.getX());
        }
    }

    /**
     * 장애물 생성
     */
    private ObstacleData createObstacle() {
        // 임시 로직: 일정한 간격으로 랜덤 타입 장애물 생성....
        ObstacleType type = ObstacleType.getById(random.nextInt(4));
        GameEntity entity = GameEntity.getByType(type);

        String id = UUID.randomUUID().toString();
        ObstacleData data = new ObstacleData(id, SPAWN_X, type.getY(), type.getId());
        
        System.out.println("[Spawner] Created " + entity.getName() + 
                         " (Type: " + type.getDisplayName() + 
                         ", Size: " + entity.getWidth() + "x" + entity.getHeight() + ")");
        
        return data;
    }
}