package marchoffools.server.game;

import marchoffools.common.model.PlayerInfo;

/**
 * 게임의 전체 상태를 관리하는 클래스
 */
public class GameState {

    private CharacterController charController;
    private TrackController trackController;

    private PlayerInfo knightPlayer;
    private PlayerInfo horsePlayer;

    private long gameStartTimeNanos = 0;
    private static final long MAX_PLAY_TIME_MS = 180000; // 3분

    public GameState(Room room) {
        this.charController = new CharacterController();
        this.trackController = new TrackController(room, charController);
        
        // 역할별 플레이어 찾기
        for (PlayerInfo player : room.getPlayers().values()) {
            if (player.getRole() == 1) { // ROLE_KNIGHT
                this.knightPlayer = player;
            } else if (player.getRole() == 2) { // ROLE_HORSE
                this.horsePlayer = player;
            }
        }
    }

    /**
     * 게임 상태 업데이트
     */
    public void update(double deltaTime) {
        charController.updateCharacterState(deltaTime);
        charController.updateSkills();
        trackController.update(deltaTime);
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

    // ========== 게임 상태 관리 ==========

    /**
     * 게임이 종료되었는지 확인
     */
    public boolean isGameOver() {
        return trackController.isGameOver() || getPlayTimeMs() >= MAX_PLAY_TIME_MS;
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
        charController.reset();
        trackController.reset();
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
}