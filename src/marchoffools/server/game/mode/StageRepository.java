package marchoffools.server.game.mode;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import marchoffools.common.model.ObstacleType;
import marchoffools.server.game.spawn.SpawnConfig;

/**
 * 스테이지 데이터 저장소
 * 모든 스테이지 설정을 중앙에서 관리
 */
public class StageRepository {
    
    /**
     * 스테이지 설정 (불변 객체)
     */
    public static class StageData {
        private final String stageId;
        private final String name;
        private final double baseTimeScale;
        private final double timeScaleIncrement;
        private final long obstacleIntervalMin;
        private final long obstacleIntervalMax;
        private final long monsterIntervalMin;
        private final long monsterIntervalMax;
        private final Map<ObstacleType, Integer> monsterWeights;
        private final double timeLimit;
        
        private StageData(Builder builder) {
            this.stageId = builder.stageId;
            this.name = builder.name;
            this.baseTimeScale = builder.baseTimeScale;
            this.timeScaleIncrement = builder.timeScaleIncrement;
            this.obstacleIntervalMin = builder.obstacleIntervalMin;
            this.obstacleIntervalMax = builder.obstacleIntervalMax;
            this.monsterIntervalMin = builder.monsterIntervalMin;
            this.monsterIntervalMax = builder.monsterIntervalMax;
            this.monsterWeights = builder.monsterWeights != null 
                ? new HashMap<>(builder.monsterWeights) 
                : null;
            this.timeLimit = builder.timeLimit;
        }
        
        public static Builder builder(String stageId) {
            return new Builder(stageId);
        }
        
        /**
         * Stage 객체로 변환
         */
        public Stage toStage() {
            // monsterWeights Map에서 가중치 추출
            SpawnConfig spawnConfig = new SpawnConfig(
                obstacleIntervalMin, 
                obstacleIntervalMax,
                monsterIntervalMin, 
                monsterIntervalMax,
                monsterWeights  // Map 그대로 전달
            );
            
            return new Stage(
                stageId,
                baseTimeScale,
                timeScaleIncrement,
                spawnConfig,
                timeLimit
            );
        }
        
        // Getters
        public String getStageId() { return stageId; }
        public String getName() { return name; }
        public double getTimeLimit() { return timeLimit; }
        
        @Override
        public String toString() {
            return String.format("Stage '%s': %s (%.0fs)", stageId, name, timeLimit);
        }
        
        /**
         * Builder Pattern
         */
        public static class Builder {
            private final String stageId;
            private String name = "Unnamed Stage";
            private double baseTimeScale = 1.0;
            private double timeScaleIncrement = 0.01;
            private long obstacleIntervalMin;
            private long obstacleIntervalMax;
            private long monsterIntervalMin = 0;
            private long monsterIntervalMax = 0;
            private Map<ObstacleType, Integer> monsterWeights;
            private double timeLimit;
            
            private Builder(String stageId) {
                this.stageId = stageId;
            }
            
            public Builder name(String name) {
                this.name = name;
                return this;
            }
            
            public Builder timeScale(double base, double increment) {
                this.baseTimeScale = base;
                this.timeScaleIncrement = increment;
                return this;
            }
            
            public Builder obstacleInterval(long min, long max) {
                if (min <= 0 || max < min) {
                    throw new IllegalArgumentException(
                        "Invalid obstacle interval: [" + min + ", " + max + "]"
                    );
                }
                this.obstacleIntervalMin = min;
                this.obstacleIntervalMax = max;
                return this;
            }
            
            public Builder monsterInterval(long min, long max) {
                if (min < 0 || max < min) {
                    throw new IllegalArgumentException(
                        "Invalid monster interval: [" + min + ", " + max + "]"
                    );
                }
                this.monsterIntervalMin = min;
                this.monsterIntervalMax = max;
                return this;
            }
            
            public Builder monsterWeights(Map<ObstacleType, Integer> weights) {
                if (weights != null && !weights.isEmpty()) {
                    this.monsterWeights = new HashMap<>(weights);
                }
                return this;
            }
            
            public Builder noMonsters() {
                this.monsterIntervalMin = 0;
                this.monsterIntervalMax = 0;
                this.monsterWeights = null;
                return this;
            }
            
            public Builder timeLimit(double seconds) {
                if (seconds <= 0) {
                    throw new IllegalArgumentException(
                        "Time limit must be positive: " + seconds
                    );
                }
                this.timeLimit = seconds;
                return this;
            }
            
            public StageData build() {
                if (obstacleIntervalMax == 0) {
                    throw new IllegalStateException("Obstacle interval must be set");
                }
                if (timeLimit == 0) {
                    throw new IllegalStateException("Time limit must be set");
                }
                return new StageData(this);
            }
        }
    }
    
    // 스테이지 데이터 저장소
    private static final Map<String, StageData> STAGES = new HashMap<>();
    
    /**
     * 가중치 맵 생성 헬퍼
     */
    private static Map<ObstacleType, Integer> createWeights(int obstacle, int monster) {
        Map<ObstacleType, Integer> w = new HashMap<>();
        w.put(ObstacleType.GROUND_OBSTACLE, obstacle);
        w.put(ObstacleType.AIR_OBSTACLE, obstacle);
        w.put(ObstacleType.SOFT_MONSTER, monster);
        w.put(ObstacleType.HARD_MONSTER, monster);
        return w;
    }
    
    static {
        // Infinite: 균등 (1:1:1:1)
        STAGES.put("infinite", StageData.builder("infinite")
            .name("Infinite Mode")
            .timeScale(1.0, 0.01)
            .obstacleInterval(1000, 1500)
            .monsterInterval(1000, 1500)
            .monsterWeights(createWeights(1, 1))
            .timeLimit(Double.MAX_VALUE)
            .build()
        );
        
        // Stage 1: 균등 (1:1:1:1)
        STAGES.put("stage1", StageData.builder("stage1")
            .name("Stage 1")
            .timeScale(1.0, 0.0)
            .obstacleInterval(1000, 1500)
            .monsterInterval(1000, 1500)
            .monsterWeights(createWeights(1, 1))
            .timeLimit(20)
            .build()
        );
        
        // Stage 2: 몬스터만 (0:0:1:1)
        STAGES.put("stage2", StageData.builder("stage2")
            .name("Stage 2 - Monsters")
            .timeScale(1.0, 0.0)
            .obstacleInterval(1000, 1500)
            .monsterInterval(1000, 1500)
            .monsterWeights(createWeights(0, 1))
            .timeLimit(20)
            .build()
        );
        
        // Stage 3: 장애물만 (1:1:0:0)
        STAGES.put("stage3", StageData.builder("stage3")
            .name("Stage 3 - Obstacles")
            .timeScale(1.0, 0.0)
            .obstacleInterval(1000, 1500)
            .monsterInterval(1000, 1500)
            .monsterWeights(createWeights(1, 0))
            .timeLimit(20)
            .build()
        );
        
        // Stage 4: 균등, 더 빠름 (1:1:1:1)
        STAGES.put("stage4", StageData.builder("stage4")
            .name("Stage 4 - Faster")
            .timeScale(1.2, 0.0)
            .obstacleInterval(800, 1200)
            .monsterInterval(800, 1200)
            .monsterWeights(createWeights(1, 1))
            .timeLimit(20)
            .build()
        );
        
        // Stage 5: 몬스터 많음 (3:7)
        STAGES.put("stage5", StageData.builder("stage5")
            .name("Stage 5 - More Monsters")
            .timeScale(1.2, 0.0)
            .obstacleInterval(800, 1200)
            .monsterInterval(800, 1200)
            .monsterWeights(createWeights(3, 7))
            .timeLimit(20)
            .build()
        );
        
        // Stage 6: 장애물 많음 (7:3)
        STAGES.put("stage6", StageData.builder("stage6")
            .name("Stage 6 - More Obstacles")
            .timeScale(1.2, 0.0)
            .obstacleInterval(800, 1200)
            .monsterInterval(800, 1200)
            .monsterWeights(createWeights(7, 3))
            .timeLimit(20)
            .build()
        );
        
        // Stage 7: 균등, 점진적 배속 (1:1:1:1)
        STAGES.put("stage7", StageData.builder("stage7")
            .name("Stage 7 - Progressive")
            .timeScale(1.0, 0.015)
            .obstacleInterval(1000, 1500)
            .monsterInterval(1000, 1500)
            .monsterWeights(createWeights(1, 1))
            .timeLimit(60)
            .build()
        );
    }
    
    /**
     * 스테이지 데이터 조회
     */
    public static StageData get(String stageId) {
        StageData data = STAGES.get(stageId);
        if (data == null) {
            throw new IllegalArgumentException(
                "Stage not found: " + stageId
            );
        }
        return data;
    }
    
    /**
     * 스테이지 존재 여부 확인
     */
    public static boolean exists(String stageId) {
        return STAGES.containsKey(stageId);
    }
    
    /**
     * 모든 스테이지 ID 반환
     */
    public static Set<String> getAllStageIds() {
        return STAGES.keySet();
    }
}