package marchoffools.server.game.mode;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import marchoffools.common.model.GameMode;
import marchoffools.server.game.spawn.SpawnConfig;

/**
 * 무한 모드
 * 지정된 스테이지들이 순환 반복
 */
public class InfiniteMode implements GameMode {
    private final Map<String, Stage> stages;  // 순서 보장
    private final String[] stageSequence;     // 순환 순서
    
    private double gameTime = 0;
    private int sequenceIndex = 0;
    private double stageStartTime = 0;
    
    public InfiniteMode(Map<String, Stage> stages) {
        if (stages == null || stages.isEmpty()) {
            throw new IllegalArgumentException("Stages cannot be empty");
        }
        
        this.stages = new LinkedHashMap<>(stages);
        this.stageSequence = stages.keySet().toArray(new String[0]);
        
        System.out.println("InfiniteMode created with stages: " 
            + Arrays.toString(stageSequence));
    }
    
    @Override
    public void update(double deltaTime) {
        gameTime += deltaTime;
        
        // 현재 스테이지
        Stage currentStage = getCurrentStage();
        double stageTime = gameTime - stageStartTime;
        
        // 스테이지 시간 초과 시 다음 스테이지로 순환
        if (currentStage.isTimeUp(stageTime)) {
            sequenceIndex = (sequenceIndex + 1) % stageSequence.length;
            stageStartTime = gameTime;
            
            System.out.println("InfiniteMode: Cycled to stage '" 
                + getCurrentStageId() + "'");
        }
    }
    
    @Override
    public double getTimeScale() {
        Stage currentStage = getCurrentStage();
        double stageTime = gameTime - stageStartTime;
        return currentStage.getTimeScale(stageTime);
    }
    
    @Override
    public SpawnConfig getSpawnConfig() {
        return getCurrentStage().getSpawnConfig();
    }
    
    @Override
    public boolean isGameOver(int score, double playTime) {
        return false;  // 무한 모드는 게임 오버 없음
    }
    
    @Override
    public String getCurrentStageInfo() {
        return String.format("INFINITE MODE");
    }
    
    @Override
    public void reset() {
        gameTime = 0;
        sequenceIndex = 0;
        stageStartTime = 0;
    }
    
    private Stage getCurrentStage() {
        return stages.get(stageSequence[sequenceIndex]);
    }
    
    private String getCurrentStageId() {
        return stageSequence[sequenceIndex];
    }
}