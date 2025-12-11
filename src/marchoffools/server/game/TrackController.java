package marchoffools.server.game;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import marchoffools.common.message.GameStateMessage.ObstacleData;
import marchoffools.common.model.ObstacleType;
import marchoffools.common.model.PlayerInfo;

/**
 * 게임 트랙 및 장애물 관리 컨트롤러
 */
public class TrackController {

    private static final double SCROLL_SPEED = 200.0; // 초당 픽셀
    private static final int PLAYER_X = 100; // 플레이어 고정 X 좌표
    private static final int PLAYER_WIDTH = 50;
    private static final int OBSTACLE_WIDTH = 50;

    private Room room;
    private CharacterController charController;
    private List<ObstacleData> obstacles;
    
    private double distance = 0.0;
    private double scrollSpeed = SCROLL_SPEED;
    private int score = 0;
    private boolean gameOver = false;

    public TrackController(Room room, CharacterController charController) {
        this.room = room;
        this.charController = charController;
        this.obstacles = new CopyOnWriteArrayList<>();
    }

    /**
     * 트랙 업데이트
     */
    public void update(double deltaTime) {
        // 거리 증가
        distance += scrollSpeed * deltaTime;
        
        // 장애물 이동
        updateObstacles(deltaTime);
        
        // 충돌 체크
        checkCollisions();
        
        // 화면 밖 장애물 제거
        removeOffscreenObstacles();
    }

    /**
     * 장애물 이동
     */
    private void updateObstacles(double deltaTime) {
        for (ObstacleData obs : obstacles) {
            obs.setX(obs.getX() - scrollSpeed * deltaTime);
        }
    }

    /**
     * 충돌 체크 (역할별 분리)
     */
    private void checkCollisions() {
        // TODO: 충돌 체크 로직 구현
        // 1. 말 담당: GROUND_OBSTACLE, AIR_OBSTACLE
        // 2. 기사 담당: WEAK_MONSTER, STRONG_MONSTER, BOSS_MONSTER
        
        // 현재는 껍데기만 제공
    }

    /**
     * 말 충돌 체크
     */
    private boolean isCollidingWithHorse(ObstacleData obs) {
        // X축 체크
        if (!isXOverlap(obs)) return false;
        
        // 무적 체크
        if (charController.isInvincible()) return false;
        
        // TODO: 타입별 회피 체크
        // GROUND_OBSTACLE: 점프 중이면 회피
        // AIR_OBSTACLE: 슬라이드 중이면 회피
        
        return false; // 임시
    }

    /**
     * 기사 충돌 체크
     */
    private boolean isCollidingWithKnight(ObstacleData obs) {
        // X축 체크
        if (!isXOverlap(obs)) return false;
        
        // 무적 체크
        if (charController.isInvincible()) return false;
        
        // TODO: 스킬별 처치 체크
        // 외침: 모든 몬스터 처치
        // 찌르기: STRONG_MONSTER
        // 베기: WEAK_MONSTER
        
        return false; // 임시
    }

    /**
     * X축 겹침 체크
     */
    private boolean isXOverlap(ObstacleData obs) {
        double obsRight = obs.getX() + OBSTACLE_WIDTH;
        double obsLeft = obs.getX();
        
        return (obsRight > PLAYER_X && obsLeft < PLAYER_X + PLAYER_WIDTH);
    }

    /**
     * 말 충돌 해결
     */
    private void resolveHorseCollision(ObstacleData obs) {
        // TODO: 게임오버 처리
        System.out.println("[TrackController] Horse collision - Game Over");
        gameOver = true;
    }

    /**
     * 기사 충돌 해결
     */
    private void resolveKnightCollision(ObstacleData obs) {
        // TODO: 몬스터 처치 또는 게임오버
        System.out.println("[TrackController] Knight collision");
    }

    /**
     * 화면 밖 장애물 제거
     */
    private void removeOffscreenObstacles() {
        obstacles.removeIf(obs -> obs.getX() < -100);
    }

    /**
     * 장애물 추가
     */
    public void addObstacle(ObstacleData obstacle) {
        obstacles.add(obstacle);
    }

    // ========== Getters and Setters ==========

    public double getDistance() {
        return distance;
    }

    public int getScore() {
        return score;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public List<ObstacleData> getObstacles() {
        return obstacles;
    }

    public double getScrollSpeed() {
        return scrollSpeed;
    }

    public void setScrollSpeed(double scrollSpeed) {
        this.scrollSpeed = scrollSpeed;
    }

    /**
     * 트랙 상태 초기화
     */
    public void reset() {
        distance = 0.0;
        scrollSpeed = SCROLL_SPEED;
        score = 0;
        gameOver = false;
        obstacles.clear();
    }
}