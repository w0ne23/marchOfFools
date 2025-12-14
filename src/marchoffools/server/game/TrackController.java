package marchoffools.server.game;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import marchoffools.common.message.GameStateMessage.ObstacleData;
import marchoffools.common.model.ObstacleType;

/**
 * 게임 트랙 및 장애물 관리 컨트롤러
 */
public class TrackController {

    private static final double SCROLL_SPEED = 200.0; // 초당 픽셀
    private static final int PLAYER_X = 100; // 플레이어 고정 X 좌표
    private static final int PLAYER_WIDTH = 50;
    private static final int PLAYER_HEIGHT = 50;
    private static final int OBSTACLE_WIDTH = 50;
    private static final int OBSTACLE_HEIGHT = 50;

    // ========== 스킬 범위 상수 ==========
    private static final int SHOUT_RANGE = 1366;     // 외침: 화면 전체
    private static final int THRUST_RANGE = 150;     // 찌르기: 150px
    private static final int SLASH_RANGE = 100;      // 베기: 100px

    private Room room;
    private CharacterController charController;
    private List<ObstacleData> obstacles;
    
    private double distance = 0.0;
    private double scrollSpeed = SCROLL_SPEED;
    private int score = 0;
    private boolean gameOver = false;
    
    // 통계
    private int obstaclesDestroyed = 0;
    private int obstaclesAvoided = 0;

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
     * 충돌 체크
     */
    private void checkCollisions() {
        for (ObstacleData obs : obstacles) {
            if (obs.isDestroyed()) continue;
            
            ObstacleType type = ObstacleType.getById(obs.getType());
            
            if (type.isObstacle()) {
                // 장애물: 말 담당 (점프/슬라이드로 회피)
                if (isCollidingWithPlayer(obs)) {
                    gameOver = true;
                    System.out.println("[TrackController] Collision with " + type.name() + " - Game Over");
                }
            } else if (type.isMonster()) {
                // 몬스터: 기사 담당 (스킬로 처치)
                if (isMonsterInRange(obs)) {
                    resolveMonsterCollision(obs);
                }
            }
        }
    }

    /**
     * 플레이어와 장애물 AABB 충돌 체크
     */
    private boolean isCollidingWithPlayer(ObstacleData obs) {
        // 무적 상태면 충돌 안 함
        if (charController.isInvincible()) return false;
        
        double playerY = charController.getPlayerY();
        
        // 플레이어 바운딩 박스
        double playerLeft = PLAYER_X;
        double playerRight = PLAYER_X + PLAYER_WIDTH;
        double playerTop = playerY;
        double playerBottom = playerY + PLAYER_HEIGHT;
        
        // 장애물 바운딩 박스
        double obsLeft = obs.getX();
        double obsRight = obs.getX() + OBSTACLE_WIDTH;
        double obsTop = obs.getY();
        double obsBottom = obs.getY() + OBSTACLE_HEIGHT;
        
        // AABB 충돌 검사
        boolean xOverlap = playerRight > obsLeft && playerLeft < obsRight;
        boolean yOverlap = playerBottom > obsTop && playerTop < obsBottom;
        
        return xOverlap && yOverlap;
    }

    /**
     * 몬스터가 스킬 범위 내에 있는지 체크
     */
    private boolean isMonsterInRange(ObstacleData obs) {
        // 무적 상태면 충돌 안 함
        if (charController.isInvincible()) return false;
        
        ObstacleType type = ObstacleType.getById(obs.getType());
        if (!type.isMonster()) return false;
        
        // Y축 체크 (몬스터가 플레이어와 같은 높이에 있는지)
        double playerY = charController.getPlayerY();
        double playerTop = playerY;
        double playerBottom = playerY + PLAYER_HEIGHT;
        double monsterTop = obs.getY();
        double monsterBottom = obs.getY() + OBSTACLE_HEIGHT;
        
        boolean yOverlap = playerBottom > monsterTop && playerTop < monsterBottom;
        if (!yOverlap) return false;
        
        // 플레이어 오른쪽 끝
        double playerRight = PLAYER_X + PLAYER_WIDTH;
        
        // 몬스터 왼쪽 끝
        double monsterLeft = obs.getX();
        
        // 1. 외침 스킬: 화면 전체 (Y축만 체크하면 됨)
        if (charController.hasActiveSkill(CharacterController.SKILL_SHOUT)) {
            // 화면 내에 있는 모든 몬스터
            if (obs.getX() >= -OBSTACLE_WIDTH && obs.getX() <= SHOUT_RANGE) {
                return true;
            }
        }
        
        // 2. SOFT_MONSTER: 베기 스킬
        if (type == ObstacleType.SOFT_MONSTER) {
            if (charController.hasActiveSkill(CharacterController.SKILL_SLASH)) {
                // 베기 범위: 플레이어 오른쪽 끝에서 100px
                double slashRight = playerRight + SLASH_RANGE;
                
                // 몬스터가 베기 범위 내에 있는지
                if (monsterLeft >= playerRight && monsterLeft <= slashRight) {
                    return true;
                }
            }
        }
        
        // 3. HARD_MONSTER: 찌르기 스킬
        if (type == ObstacleType.HARD_MONSTER) {
            if (charController.hasActiveSkill(CharacterController.SKILL_THRUST)) {
                // 찌르기 범위: 플레이어 오른쪽 끝에서 150px
                double thrustRight = playerRight + THRUST_RANGE;
                
                // 몬스터가 찌르기 범위 내에 있는지
                if (monsterLeft >= playerRight && monsterLeft <= thrustRight) {
                    return true;
                }
            }
        }
        
        // 4. 스킬 없이 플레이어와 충돌했는지 (AABB)
        double playerLeft = PLAYER_X;
        double monsterRight = obs.getX() + OBSTACLE_WIDTH;
        
        boolean xOverlap = playerRight > monsterLeft && playerLeft < monsterRight;
        
        return xOverlap;
    }

    /**
     * 몬스터 충돌 해결
     */
    private void resolveMonsterCollision(ObstacleData obs) {
        ObstacleType type = ObstacleType.getById(obs.getType());
        boolean destroyed = false;
        int points = 0;
        
        double playerRight = PLAYER_X + PLAYER_WIDTH;
        double monsterLeft = obs.getX();
        
        // 외침 스킬: 모든 몬스터 처치
        if (charController.hasActiveSkill(CharacterController.SKILL_SHOUT)) {
            if (obs.getX() >= -OBSTACLE_WIDTH && obs.getX() <= SHOUT_RANGE) {
                destroyed = true;
                points = (type == ObstacleType.BOSS_MONSTER) ? 500 : 200;
            }
        }
        // SOFT_MONSTER: 베기로 처치
        else if (type == ObstacleType.SOFT_MONSTER && 
            charController.hasActiveSkill(CharacterController.SKILL_SLASH)) {
            double slashRight = playerRight + SLASH_RANGE;
            if (monsterLeft >= playerRight && monsterLeft <= slashRight) {
                destroyed = true;
                points = 200;
            }
        }
        // HARD_MONSTER: 찌르기로 처치
        else if (type == ObstacleType.HARD_MONSTER && 
            charController.hasActiveSkill(CharacterController.SKILL_THRUST)) {
            double thrustRight = playerRight + THRUST_RANGE;
            if (monsterLeft >= playerRight && monsterLeft <= thrustRight) {
                destroyed = true;
                points = 200;
            }
        }
        
        if (destroyed) {
            obs.setDestroyed(true);
            obstaclesDestroyed++;
            score += points;
            System.out.println("[TrackController] Destroyed " + type.name() + " (+" + points + ")");
        } else {
            // 스킬 없이 충돌 - 게임 오버
            gameOver = true;
            System.out.println("[TrackController] Collision with " + type.name() + " without skill - Game Over");
        }
    }

    /**
     * 화면 밖 장애물 제거 (회피 성공 처리 포함)
     */
    private void removeOffscreenObstacles() {
        obstacles.removeIf(obs -> {
            // 화면 왼쪽 밖으로 나갔고, 아직 파괴되지 않았다면 회피 성공
            if (obs.getX() + OBSTACLE_WIDTH < 0 && !obs.isDestroyed()) {
                ObstacleType type = ObstacleType.getById(obs.getType());
                
                // 장애물: 회피 성공
                if (type.isObstacle()) {
                    obstaclesAvoided++;
                    score += 100;
                    System.out.println("[TrackController] Avoided " + type.name() + " (+100)");
                }
                // 몬스터: 그냥 지나침 (점수 없음)
            }
            return obs.getX() + OBSTACLE_WIDTH < 0;
        });
    }

    /**
     * 장애물 추가
     */
    public void addObstacle(ObstacleData obstacle) {
        obstacles.add(obstacle);
    }

    // ========== Getters ==========

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

    public int getObstaclesDestroyed() {
        return obstaclesDestroyed;
    }

    public int getObstaclesAvoided() {
        return obstaclesAvoided;
    }

    /**
     * 트랙 상태 초기화
     */
    public void reset() {
        distance = 0.0;
        scrollSpeed = SCROLL_SPEED;
        score = 0;
        gameOver = false;
        obstaclesDestroyed = 0;
        obstaclesAvoided = 0;
        obstacles.clear();
    }
}