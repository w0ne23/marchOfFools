package marchoffools.server.game;

import marchoffools.common.model.GameMode;
import marchoffools.common.model.GameModeType;
import marchoffools.common.model.PlayerInfo;
import marchoffools.server.game.mode.GameModeFactory;

/**
 * 게임의 전체 상태를 관리하는 클래스
 */
public class GameState {
    
    private GameMode gameMode;
    private CharacterController charController;
    private TrackController trackController;
    private ObstacleSpawner spawner;

    private PlayerInfo knightPlayer;
    private PlayerInfo horsePlayer;

    private long gameStartTimeNanos = 0;
    private long lastSpawnConfigUpdate = 0;
    private static final long CONFIG_UPDATE_INTERVAL = 1000; // 1초마다 체크

    public GameState(Room room, GameModeType modeType) {
        // GameMode 생성 (Factory Pattern)
        this.gameMode = GameModeFactory.create(modeType);
        
        this.charController = new CharacterController();
        this.trackController = new TrackController(charController);
        this.spawner = new ObstacleSpawner();
        
        // 역할별 플레이어 찾기
        for (PlayerInfo player : room.getPlayers().values()) {
            if (player.getRole() == 1) { // ROLE_KNIGHT
                this.knightPlayer = player;
            } else if (player.getRole() == 2) { // ROLE_HORSE
                this.horsePlayer = player;
            }
        }
        
        System.out.println("[GameState] Initialized with mode: " + modeType);
    }

    /**
     * 게임 상태 업데이트 (매 프레임마다 호출)
     */
    public void update(double deltaTime) {
        double timeScale = gameMode.getTimeScale();
        double scaledDelta = deltaTime * timeScale;
        
        String prevStageInfo = gameMode.getCurrentStageInfo();
        
        // 게임 모드 업데이트
        gameMode.update(deltaTime);
        
        String currentStageInfo = gameMode.getCurrentStageInfo();
        if (!prevStageInfo.equals(currentStageInfo)) {
            spawner.updateStrategies(gameMode);
            System.out.println("[GameState] Stage changed: " + prevStageInfo + " → " + currentStageInfo);
        }
        
        // 게임 상태 업데이트
        charController.updateCharacterState(scaledDelta);
        charController.updateSkills();
        trackController.update(scaledDelta);
    }

    // ========== 캐릭터 제어 위임 ==========

    public void setCharState(int state) {
        charController.setCharState(state);
    }

    public int getCharState() {
        return charController.getCharState();
    }

    public void startJump() {
        charController.startJump();
    }

    public double getPlayerY() {
        return charController.getPlayerY();
    }

    public boolean isInvincible() {
        return charController.isInvincible();
    }

    // ========== 스킬 관리 위임 ==========

    public boolean canUseSkill(int skillId) {
        return charController.canUseSkill(skillId);
    }

    public boolean useSkill(int skillId) {
        return charController.useSkill(skillId);
    }

    public void activateSkill(int skillId) {
        charController.activateSkill(skillId);
    }

    public long getRemainingCooldown(int skillId) {
        return charController.getRemainingCooldown(skillId);
    }

    public long[] getAllCooldowns() {
        return charController.getAllCooldowns();
    }

    public boolean[] getActiveSkills() {
        return charController.getActiveSkills();
    }

    // ========== 트랙 접근 위임 ==========

    public double getDistance() {
        return trackController.getDistance();
    }

    public int getScore() {
        return trackController.getScore();
    }

    public TrackController getTrackController() {
        return trackController;
    }
    
    public ObstacleSpawner getSpawner() {
        return spawner;
    }

    // ========== 게임 상태 관리 ==========

    /**
     * 게임이 종료되었는지 확인
     */
    public boolean isGameOver() {
        return trackController.isGameOver() || 
               gameMode.isGameOver(getScore(), getPlayTime());
    }

    /**
     * 현재 게임 진행 시간 반환 (밀리초)
     */
    public long getPlayTimeMs() {
        return (System.nanoTime() - gameStartTimeNanos) / 1_000_000;
    }

    /**
     * 현재 게임 진행 시간 반환 (초)
     */
    public int getPlayTime() {
        return (int) (getPlayTimeMs() / 1000);
    }

    /**
     * 게임 상태 초기화
     */
    public void reset() {
        gameStartTimeNanos = System.nanoTime();
        gameMode.reset();
        spawner.reset();
        spawner.updateStrategies(gameMode);
        charController.reset();
        trackController.reset();
        lastSpawnConfigUpdate = System.currentTimeMillis();
        
        System.out.println("[GameState] Reset complete");
    }

    // ========== Getters ==========

    public PlayerInfo getKnightPlayer() {
        return knightPlayer;
    }

    public PlayerInfo getHorsePlayer() {
        return horsePlayer;
    }

    public CharacterController getCharController() {
        return charController;
    }
    
    public GameMode getGameMode() {
        return gameMode;
    }
    
    public String getGameModeName() {
        return gameMode.getClass().getSimpleName()
            .replace("Mode", "")
            .toUpperCase(); // "INFINITE" 또는 "STAGE"
    }

    public String getCurrentStageInfo() {
        return gameMode.getCurrentStageInfo();
    }
}