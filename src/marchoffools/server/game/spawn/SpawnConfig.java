package marchoffools.server.game.spawn;

import java.util.Map;
import marchoffools.common.model.ObstacleType;

/**
 * 스폰 설정 데이터 클래스
 */
public class SpawnConfig {
    private final long obstacleMinInterval;
    private final long obstacleMaxInterval;
    private final long monsterMinInterval;
    private final long monsterMaxInterval;
    
    // 타입별 가중치
    private final int groundWeight;
    private final int airWeight;
    private final int softWeight;
    private final int hardWeight;
    
    public SpawnConfig(long obsMin, long obsMax, 
                      long monMin, long monMax,
                      Map<ObstacleType, Integer> weights) {
        this.obstacleMinInterval = obsMin;
        this.obstacleMaxInterval = obsMax;
        this.monsterMinInterval = monMin;
        this.monsterMaxInterval = monMax;
        
        // 가중치 추출
        if (weights != null) {
            this.groundWeight = weights.getOrDefault(ObstacleType.GROUND_OBSTACLE, 0);
            this.airWeight = weights.getOrDefault(ObstacleType.AIR_OBSTACLE, 0);
            this.softWeight = weights.getOrDefault(ObstacleType.SOFT_MONSTER, 0);
            this.hardWeight = weights.getOrDefault(ObstacleType.HARD_MONSTER, 0);
        } else {
            this.groundWeight = 1;
            this.airWeight = 1;
            this.softWeight = 1;
            this.hardWeight = 1;
        }
    }
    
    // Getters
    public long getObstacleMinInterval() { return obstacleMinInterval; }
    public long getObstacleMaxInterval() { return obstacleMaxInterval; }
    public long getMonsterMinInterval() { return monsterMinInterval; }
    public long getMonsterMaxInterval() { return monsterMaxInterval; }
    
    public int getGroundWeight() { return groundWeight; }
    public int getAirWeight() { return airWeight; }
    public int getSoftWeight() { return softWeight; }
    public int getHardWeight() { return hardWeight; }
    
    public boolean hasObstacles() {
        return groundWeight > 0 || airWeight > 0;
    }
    
    public boolean hasMonsters() {
        return softWeight > 0 || hardWeight > 0;
    }
    
    @Override
    public String toString() {
        return String.format("SpawnConfig{obs=%d~%d(G:%d,A:%d), mon=%d~%d(S:%d,H:%d)}", 
            obstacleMinInterval, obstacleMaxInterval, groundWeight, airWeight,
            monsterMinInterval, monsterMaxInterval, softWeight, hardWeight);
    }
}