package marchoffools.common.model;

import marchoffools.server.game.spawn.SpawnConfig;

/**
 * 게임 모드 인터페이스 (Strategy Pattern)
 * 무한 모드, 스테이지 모드 등 다양한 게임 모드를 구현
 */
public interface GameMode {
    void update(double deltaTime);
    double getTimeScale();
    SpawnConfig getSpawnConfig();
    boolean isGameOver(int score, double playTime);
    
    /**
     * UI 표시용 상태 텍스트
     */
    String getCurrentStageInfo();
    
    void reset();
}