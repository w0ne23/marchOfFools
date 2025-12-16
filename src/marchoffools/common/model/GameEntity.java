package marchoffools.common.model;

import java.awt.Color;

/**
 * 장애물 개별 데이터
 */
public enum GameEntity {
    
    // ========== 하방 장애물 ==========
    GROUND_ROCK(
        ObstacleType.GROUND_OBSTACLE,
        "바위",
        new Color(139, 69, 19),
        "/assets/ground_rock.png"
    ),
    
    // ========== 상방 장애물 ==========
    AIR_ROCK(
        ObstacleType.AIR_OBSTACLE,
        "돌",
        new Color(135, 206, 250),
        "/assets/air_rock.png"
    ),
    
    // ========== 연질 몬스터 ==========
    SOFT_SLIME(
        ObstacleType.SOFT_MONSTER,
        "슬라임",
        new Color(144, 238, 144),
        "/assets/soft_slime.png"
    ),
    
    // ========== 경질 몬스터 ==========
    HARD_GOLEM(
        ObstacleType.HARD_MONSTER,
        "골렘",
        new Color(255, 69, 0),
        "/assets/hard_golem.png"
    ),
    
    // ========== 보스 몬스터 ==========
    BOSS_DRAGON(
        ObstacleType.BOSS_MONSTER,
        "드래곤",
        new Color(128, 0, 128),
        "/assets/boss_dragon.png"
    );
    
    // ========== 필드 ==========
    private final ObstacleType obstacleType;
    private final String name;
    private final Color defaultColor;
    private final String imagePath;
    
    // ========== 생성자 ==========
    GameEntity(ObstacleType obstacleType, String name, Color defaultColor, String imagePath) {
        this.obstacleType = obstacleType;
        this.name = name;
        this.defaultColor = defaultColor;
        this.imagePath = imagePath;
    }
    
    // ========== Getters ==========
    
    public ObstacleType getObstacleType() {
        return obstacleType;
    }

    public String getName() {
        return name;
    }

    public Color getDefaultColor() {
        return defaultColor;
    }

    public String getImagePath() {
        return imagePath;
    }
    
    public int getWidth() {
        return obstacleType.getWidth();
    }
    
    public int getHeight() {
        return obstacleType.getHeight();
    }

    public int getScore() {
        return obstacleType.getScore();
    }
    
    // 현재 모든 엔티티가 타입당 단일하므로 단순 구현...
    public static GameEntity getByType(ObstacleType type) {
        for (GameEntity entity : values()) {
            if (entity.obstacleType == type) {
                return entity;
            }
        }
        return null;
    }
}