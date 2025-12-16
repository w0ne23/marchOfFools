package marchoffools.server.game;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import marchoffools.common.message.GameStateMessage.ObstacleData;
import marchoffools.common.model.ObstacleType;
import marchoffools.common.model.GameConstants;
import marchoffools.common.model.GameSkill;

/**
 * 게임 트랙 및 장애물 관리 컨트롤러
 */
public class TrackController {

    private static final double SCROLL_SPEED = 500.0; // 초당 픽셀

    private Room room;
    private CharacterController charController;
    private List<ObstacleData> obstacles;
    
    private double distance = 0.0;
    private double scrollSpeed = SCROLL_SPEED; // TODO: 시간 기반 배속 구현...
    private int score = 0;
    private boolean gameOver = false;
    
    // 점수 계산용
    private double lastScoredDistance = 0.0;
    
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
        
        // 거리 기반 점수 추가
        updateDistanceScore();
        
        // 장애물 이동
        updateObstacles(deltaTime);
        
        // 스킬과 몬스터 충돌 체크
        checkSkillCollisions();
        
        // 캐릭터와 장애물 충돌 체크
        if (checkCharacterCollision()) {
            gameOver = true;
            System.out.println("[TrackController] Game Over - Character collision");
        }
        
        // 화면 밖 장애물 제거
        removeOffscreenObstacles();
    }
    
    /**
     * 거리 기반 점수 업데이트
     * 100px(1m)마다 10점 추가
     */
    private void updateDistanceScore() {
        double distanceTraveled = distance - lastScoredDistance;
        
        if (distanceTraveled >= 100.0) {
            int meters = (int)(distanceTraveled / 100.0);
            score += meters * 10;
            lastScoredDistance += meters * 100.0;
            System.out.println("[Score] Distance bonus: +" + meters + " (" + (int)(distance/100) + "m traveled)");
        }
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
     * 캐릭터와 모든 장애물의 충돌 체크
     * 역할 구분 없이 충돌 시 게임 오버
     */
    private boolean checkCharacterCollision() {
        // 캐릭터가 무적 상태면 충돌 무시
        if (charController.isInvincible()) {
            return false;
        }
        
        // 캐릭터 히트박스
        double charX = GameConstants.CHARACTER_X;
        double charY = charController.getPlayerY();
        int charW = GameConstants.CHARACTER_WIDTH;
        int charH = GameConstants.CHARACTER_HEIGHT;
        
        // 슬라이드 중일 때 높이 감소
        if (charController.getCharState() == CharacterController.STATE_SLIDING) {
            charH = (int)(GameConstants.CHARACTER_HEIGHT - GameConstants.SLIDE_HEIGHT);
            charY += GameConstants.SLIDE_HEIGHT;
        }
        
        // 모든 장애물과 충돌 체크
        for (ObstacleData obs : obstacles) {
            if (obs.isDestroyed()) continue;
            
            ObstacleType type = ObstacleType.getById(obs.getType());
            
            // AABB 충돌 체크
            if (isColliding(charX, charY, charW, charH,
                           obs.getX(), obs.getY(), type.getWidth(), type.getHeight())) {
                System.out.println("[Collision] Character hit obstacle: " + type.getDisplayName());
                return true; // 게임 오버
            }
        }
        
        return false;
    }

    /**
     * 활성화된 스킬과 몬스터 충돌 체크
     * y축은 우선 무시하도록 구현(아직 밸런싱하지 않았음)
     */
    private void checkSkillCollisions() {
        boolean[] activeSkills = charController.getActiveSkills();
        
        double charX = GameConstants.CHARACTER_X;
        int charW = GameConstants.CHARACTER_WIDTH;
        
        for (ObstacleData obs : obstacles) {
            if (obs.isDestroyed()) continue;
            
            ObstacleType type = ObstacleType.getById(obs.getType());
            
            // 몬스터가 아니면 스킬로 처치 불가
            if (!type.isMonster()) continue;
            
            boolean destroyed = false;
            int points = 0;
            
            // 외침(0) - 화면 전체, 모든 몬스터
            if (activeSkills[GameSkill.SKILL_SHOUT]) {
                if (obs.getX() >= 0 && obs.getX() <= GameConstants.GAME_WIDTH) {
                    destroyed = true;
                    points = type.getScore();
                    System.out.println("[Skill] Shout destroyed " + type.getDisplayName() + " (+" + points + ")");
                }
            }
            
            // 찌르기(1) - HARD
            if (!destroyed && activeSkills[GameSkill.SKILL_THRUST]) {
                double thrustX = charX + charW;
                double thrustY = 0;
                int thrustW = GameConstants.THRUST_RANGE;
                int thrustH = GameConstants.GAME_HEIGHT;  // 화면 전체 높이
                
                if (isColliding(thrustX, thrustY, thrustW, thrustH,
                              obs.getX(), obs.getY(), type.getWidth(), type.getHeight())) {
                    if (type == ObstacleType.HARD_MONSTER) {
                        destroyed = true;
                        points = type.getScore();
                        System.out.println("[Skill] Thrust destroyed " + type.getDisplayName() + " (+" + points + ")");
                    }
                }
            }
            
            // 베기(2) - SOFT
            if (!destroyed && activeSkills[GameSkill.SKILL_SLASH]) {
                double slashX = charX + charW;
                double slashY = 0;
                int slashW = GameConstants.SLASH_RANGE;
                int slashH = GameConstants.GAME_HEIGHT;  // 화면 전체 높이
                
                if (isColliding(slashX, slashY, slashW, slashH,
                              obs.getX(), obs.getY(), type.getWidth(), type.getHeight())) {
                    if (type == ObstacleType.SOFT_MONSTER) {
                        destroyed = true;
                        points = type.getScore();
                        System.out.println("[Skill] Slash destroyed " + type.getDisplayName() + " (+" + points + ")");
                    }
                }
            }
            
            // 처치 성공 처리
            if (destroyed) {
                obs.setDestroyed(true);
                obstaclesDestroyed++;
                score += points;
            }
        }
    }

    /**
     * AABB 충돌 체크 유틸리티
     */
    private boolean isColliding(double x1, double y1, int w1, int h1,
                               double x2, double y2, int w2, int h2) {
        return x1 < x2 + w2 &&
               x1 + w1 > x2 &&
               y1 < y2 + h2 &&
               y1 + h1 > y2;
    }

    /**
     * 화면 밖 장애물 제거 (회피 성공 처리 포함)
     */
    private void removeOffscreenObstacles() {
        obstacles.removeIf(obs -> {
            ObstacleType type = ObstacleType.getById(obs.getType());
            
            // 화면 왼쪽 밖으로 나갔고, 아직 파괴되지 않았다면 회피 성공
            if (obs.getX() + type.getWidth() < 0 && !obs.isDestroyed()) {
                // 장애물(말 담당): 회피 성공
                if (type.isObstacle()) {
                    obstaclesAvoided++;
                    score += 100; // 성공적으로 회피
                    System.out.println("[TrackController] Avoided " + type.getDisplayName() + " (+100)");
                }
                // 몬스터(기사 담당): 그냥 지나침 (처치 실패는 점수 없음)
            }
            
            return obs.getX() + type.getWidth() < 0;
        });
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
        lastScoredDistance = 0.0;
        
        System.out.println("[TrackController] Reset complete - score system initialized");
    }
}