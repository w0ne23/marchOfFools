package marchoffools.server.game.spawn;

import java.util.Random;
import java.util.UUID;

import marchoffools.common.message.GameStateMessage.ObstacleData;
import marchoffools.common.model.ObstacleType;

public class ObstacleSpawnStrategy implements SpawnStrategy {
    private static final double SPAWN_X = 1366;
    private static final Random random = new Random();
    private long spawnStartTime = 0;
    
    private long lastSpawnTime = 0;
    private long nextSpawnTime = 0;
    
    private final long minInterval;
    private final long maxInterval;
    
    private int groundWeight;
    private int airWeight;
    
    public ObstacleSpawnStrategy(long minInterval, long maxInterval, 
                                int groundWeight, int airWeight) {
        this.minInterval = minInterval;
        this.maxInterval = maxInterval;
        this.groundWeight = groundWeight;
        this.airWeight = airWeight;
    }
    
    @Override
    public void startSpawn(long initialDelay) {
        long now = System.currentTimeMillis();
        spawnStartTime = now;
        lastSpawnTime = now;
        nextSpawnTime = now + initialDelay;
        
        System.out.println(String.format("[%s] Started - spawnStart: %d, nextSpawn: %d (delay: %d)",
            this.getClass().getSimpleName(), spawnStartTime, nextSpawnTime, initialDelay));
    }
    
    @Override
    public boolean shouldSpawn(long currentTime) {
        // 가중치 0이면 스폰 안함
        if (groundWeight + airWeight == 0) {
            return false;
        }
        
        if (currentTime < spawnStartTime) {
            return false;
        }
        
        if (currentTime >= nextSpawnTime) {
            lastSpawnTime = currentTime;
            calculateNextSpawnTime();
            return true;
        }
        return false;
    }
    
    @Override
    public ObstacleData createObstacle() {
        ObstacleType type;
        int total = groundWeight + airWeight;
        
        if (airWeight == 0) {
            type = ObstacleType.GROUND_OBSTACLE;
        } else if (groundWeight == 0) {
            type = ObstacleType.AIR_OBSTACLE;
        } else {
            type = random.nextInt(total) < groundWeight 
                ? ObstacleType.GROUND_OBSTACLE 
                : ObstacleType.AIR_OBSTACLE;
        }
        
        String id = UUID.randomUUID().toString();
        return new ObstacleData(id, SPAWN_X, type.getY(), type.getId());
    }
    
    private void calculateNextSpawnTime() {
        long variance = maxInterval - minInterval;
        long randomDelay = variance > 0 ? random.nextLong(variance + 1) : 0;
        nextSpawnTime = lastSpawnTime + minInterval + randomDelay;
    }
    
    @Override
    public void reset() {
        spawnStartTime = 0;
        lastSpawnTime = 0;
        nextSpawnTime = 0;
    }
}