package marchoffools.server.game.mode;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import marchoffools.common.model.GameMode;
import marchoffools.server.game.spawn.SpawnConfig;

/**
 * 스테이지 모드
 * 지정된 스테이지를 순차적으로 진행하고 마지막 스테이지 완료 시 게임 종료
 */
public class StageMode implements GameMode {
    private final Map<String, Stage> stages;  // 순서 보장
    private final String[] stageSequence;     // 진행 순서
    
    private double gameTime = 0;
    private int sequenceIndex = 0;
    private double stageStartTime = 0;
    private boolean allStagesCompleted = false;
    
    public StageMode(Map<String, Stage> stages) {
        if (stages == null || stages.isEmpty()) {
            throw new IllegalArgumentException("Stages cannot be empty");
        }
        
        this.stages = new LinkedHashMap<>(stages);
        this.stageSequence = stages.keySet().toArray(new String[0]);
        
        System.out.println("StageMode created with stages: " 
            + Arrays.toString(stageSequence));
    }
    
    @Override
    public void update(double deltaTime) {
        if (allStagesCompleted) {
            return;
        }
        
        gameTime += deltaTime;
        
        // 현재 스테이지
        Stage currentStage = getCurrentStage();
        double stageTime = gameTime - stageStartTime;
        
        // 스테이지 시간 초과 체크
        if (currentStage.isTimeUp(stageTime)) {
            if (sequenceIndex < stageSequence.length - 1) {
                // 다음 스테이지로 전환
                sequenceIndex++;
                stageStartTime = gameTime;
                
                System.out.println("StageMode: Advanced to stage '" 
                    + getCurrentStageId() + "'");
            } else {
                // 마지막 스테이지 완료
                allStagesCompleted = true;
                
                System.out.println("StageMode: All stages completed!");
            }
        }
    }
    
    @Override
    public double getTimeScale() {
        if (allStagesCompleted) {
            return 1.0;
        }
        
        Stage currentStage = getCurrentStage();
        double stageTime = gameTime - stageStartTime;
        return currentStage.getTimeScale(stageTime);
    }
    
    @Override
    public SpawnConfig getSpawnConfig() {
        if (allStagesCompleted) {
            return null;
        }
        
        return getCurrentStage().getSpawnConfig();
    }
    
    @Override
    public boolean isGameOver(int score, double playTime) {
        return allStagesCompleted;
    }
    
    @Override
    public String getCurrentStageInfo() {
        if (allStagesCompleted) {
            return String.format("STAGE MODE - COMPLETED", gameTime);
        }
        
        return String.format("Stage %d", sequenceIndex + 1);
    }
    
    @Override
    public void reset() {
        gameTime = 0;
        sequenceIndex = 0;
        stageStartTime = 0;
        allStagesCompleted = false;
    }
    
    private Stage getCurrentStage() {
        return stages.get(stageSequence[sequenceIndex]);
    }
    
    private String getCurrentStageId() {
        return stageSequence[sequenceIndex];
    }
}