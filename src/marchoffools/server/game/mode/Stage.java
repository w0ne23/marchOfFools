package marchoffools.server.game.mode;

import marchoffools.server.game.spawn.SpawnConfig;

/**
 * 스테이지 정의 클래스
 * 각 스테이지의 timeScale, 스폰 설정, 시간 제한
 */
public class Stage {
    private final String stageId;
    private final double baseTimeScale;
    private final double timeScaleIncrement;
    private final SpawnConfig spawnConfig;
    private final double timeLimit; // 초 단위
    
    public Stage(String stageId, double baseTimeScale, double timeScaleIncrement,
                 SpawnConfig spawnConfig, double timeLimit) {
        this.stageId = stageId;
        this.baseTimeScale = baseTimeScale;
        this.timeScaleIncrement = timeScaleIncrement;
        this.spawnConfig = spawnConfig;
        this.timeLimit = timeLimit;
    }
    
    /**
     * 스테이지 내 시간에 따른 타임 스케일
     * @param stageTime 스테이지 경과 시간 (초)
     * @return 타임 스케일 배율
     */
    public double getTimeScale(double stageTime) {
        return baseTimeScale + (stageTime * timeScaleIncrement);
    }
    
    public SpawnConfig getSpawnConfig() {
        return spawnConfig;
    }
    
    /**
     * 스테이지 시간 초과 체크
     * @param stageTime 스테이지 경과 시간 (초)
     * @return 제한 시간 초과 여부
     */
    public boolean isTimeUp(double stageTime) {
        return stageTime >= timeLimit;
    }
    
    public String getStageId() {
        return stageId;
    }
    
    public double getTimeLimit() {
        return timeLimit;
    }
    
    @Override
    public String toString() {
        return String.format("Stage{id='%s', timeScale=%.2f+%.3f/s, time=%.0fs}", 
            stageId, baseTimeScale, timeScaleIncrement, timeLimit);
    }
}