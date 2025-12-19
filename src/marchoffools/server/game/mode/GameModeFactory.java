package marchoffools.server.game.mode;

import java.util.LinkedHashMap;
import java.util.Map;

import marchoffools.common.model.GameMode;
import marchoffools.common.model.GameModeType;

/**
 * 게임 모드 팩토리 (Factory Pattern)
 * GameModeType에 따라 적절한 GameMode 인스턴스 생성
 */
public class GameModeFactory {
    
    /**
     * GameMode 생성
     */
    public static GameMode create(GameModeType type) {
        if (type == null) {
            throw new IllegalArgumentException("GameModeType cannot be null");
        }
        
        switch (type) {
            case INFINITE:
                return createInfiniteMode();
            
            case STAGE:
                return createStageMode();
                
            default:
                throw new IllegalArgumentException(
                    "Unsupported game mode: " + type
                );
        }
    }
    
    /**
     * 무한 모드 생성
     * 단일 스테이지: infinite (점진적 배속, 무제한)
     */
    private static GameMode createInfiniteMode() {
        String[] stageIds = { "infinite" };
        
        Map<String, Stage> stages = createStagesFromIds(stageIds);
        return new InfiniteMode(stages);
    }
    
    /**
     * 스테이지 모드 생성
     * 스테이지 순서: stage1 → stage2 → stage3 → stage4 → stage5 → stage6 → stage7
     */
    private static GameMode createStageMode() {
        String[] stageIds = {
            "stage1",
            "stage2",
            "stage3",
            "stage4",
            "stage5",
            "stage6",
            "stage7"
        };
        
        Map<String, Stage> stages = createStagesFromIds(stageIds);
        return new StageMode(stages);
    }
    
    /**
     * Stage ID 배열로부터 Stage Map 생성 (순서 보장)
     */
    private static Map<String, Stage> createStagesFromIds(String[] stageIds) {
        Map<String, Stage> stages = new LinkedHashMap<>();
        
        for (String stageId : stageIds) {
            if (!StageRepository.exists(stageId)) {
                throw new IllegalArgumentException(
                    "Stage not found: " + stageId
                );
            }
            
            Stage stage = StageRepository.get(stageId).toStage();
            stages.put(stageId, stage);
        }
        
        return stages;
    }
    
    /**
     * 사용 가능한 모든 모드 반환
     */
    public static GameModeType[] getAvailableModes() {
        return GameModeType.values();
    }
}