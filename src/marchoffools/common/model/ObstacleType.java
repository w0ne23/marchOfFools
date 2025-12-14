package marchoffools.common.model;

import java.util.Random;

public enum ObstacleType {
    GROUND_OBSTACLE(0, 470),   // 하방 장애물, 점프로 회피
    AIR_OBSTACLE(1, 130),      // 상방 장애물, 슬라이드로 회피
    SOFT_MONSTER(2, 300),      // 연질 몬스터, 베기로 처치
    HARD_MONSTER(3, 300),    // 경질 몬스터, 찌르기로 처치
    BOSS_MONSTER(4, 300);      // 보스 몬스터
    
    private final int id;
    private final double y;
    private static final Random random = new Random();
    
    ObstacleType(int id, double y) {
        this.id = id;
        this.y = y;
    }
    
    public int getId() {
        return id;
    }
    
    public double getY() {
        return y;
    }
    
    public static ObstacleType getById(int id) {
        for (ObstacleType type : values()) {
            if (type.id == id) {
                return type;
            }
        }
        return GROUND_OBSTACLE;
    }
    
    public static ObstacleType random() {
        ObstacleType[] types = values();
        return types[random.nextInt(types.length)];
    }
    
    /**
     * 몬스터 타입인지 확인
     */
    public boolean isMonster() {
        return this == SOFT_MONSTER || this == HARD_MONSTER || this == BOSS_MONSTER;
    }
    
    /**
     * 장애물 타입인지 확인
     */
    public boolean isObstacle() {
        return this == GROUND_OBSTACLE || this == AIR_OBSTACLE;
    }
    
    /**
     * 필요한 액션 타입 반환
     * @return 0: 점프, 1: 슬라이드, 2: 베기, 3: 찌르기, 4: 외침
     */
    public int getRequiredAction() {
        switch (this) {
            case GROUND_OBSTACLE: return 0; // 점프
            case AIR_OBSTACLE: return 1;    // 슬라이드
            case SOFT_MONSTER: return 2;    // 베기
            case HARD_MONSTER: return 3;  // 찌르기
            default: return -1;
        }
    }
}