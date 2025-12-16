package marchoffools.common.model;

import java.util.Random;

public enum ObstacleType {
	GROUND_OBSTACLE(0, "하방 장애물", 
        GameConstants.GROUND_OBSTACLE_WIDTH, 
        GameConstants.GROUND_OBSTACLE_HEIGHT, 
        GameConstants.GROUND_OBSTACLE_Y,
        100),
    
    AIR_OBSTACLE(1, "상방 장애물", 
        GameConstants.AIR_OBSTACLE_WIDTH, 
        GameConstants.AIR_OBSTACLE_HEIGHT, 
        GameConstants.AIR_OBSTACLE_Y,
        100),
    
    SOFT_MONSTER(2, "연질 몬스터", 
        GameConstants.MONSTER_WIDTH, 
        GameConstants.MONSTER_HEIGHT, 
        GameConstants.MONSTER_Y,
        100),
    
    HARD_MONSTER(3, "경질 몬스터", 
        GameConstants.MONSTER_WIDTH, 
        GameConstants.MONSTER_HEIGHT, 
        GameConstants.MONSTER_Y,
        100),
    
    BOSS_MONSTER(4, "보스 몬스터", 
        GameConstants.BOSS_WIDTH, 
        GameConstants.BOSS_HEIGHT, 
        GameConstants.BOSS_Y,
        10000);
    
    private final int id;
    private final String displayName;
    private final int width;
    private final int height;
    private final int y;
    private final int score;
    
    ObstacleType(int id, String displayName, int width, int height, int y, int score) {
        this.id = id;
        this.displayName = displayName;
        this.width = width;
        this.height = height;
        this.y = y;
        this.score = score;
    }


	public int getId() {
        return id;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public int getY() {
		return y;
	}

	public int getScore() {
		return score;
	}
	
    public static ObstacleType getById(int id) {
        for (ObstacleType type : values()) {
            if (type.id == id) {
                return type;
            }
        }
        return GROUND_OBSTACLE;
    }
    
    // 타입 확인
    public boolean isMonster() {
        return this == SOFT_MONSTER || this == HARD_MONSTER || this == BOSS_MONSTER;
    }
    
    public boolean isObstacle() {
        return this == GROUND_OBSTACLE || this == AIR_OBSTACLE;
    }
    
}