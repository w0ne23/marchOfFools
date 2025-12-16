package marchoffools.common.model;

public class GameConstants {
    
    // ========== 게임 영역 크기 ==========
    public static final int GAME_WIDTH = 1366;
    public static final int GAME_HEIGHT = 768;
    
    // ========== 기준 좌표 ==========
    public static final int GROUND_Y = 600;
//    public static final double JUMP_HEIGHT = 150.0; // 점프 시 최대 높이 증가치
    public static final double SLIDE_HEIGHT = 50.0; // 슬라이드 시 높이 감소치
    
    // ========== 캐릭터 ==========
    public static final int CHARACTER_WIDTH = 200;
    public static final int CHARACTER_HEIGHT = 200;
    public static final int CHARACTER_X = 100;
    public static final int CHARACTER_Y = GROUND_Y - CHARACTER_HEIGHT;
    
    // ========== 하방 장애물 (지상) ==========
    public static final int GROUND_OBSTACLE_WIDTH = 100;
    public static final int GROUND_OBSTACLE_HEIGHT = 100;
    public static final int GROUND_OBSTACLE_Y = GROUND_Y - GROUND_OBSTACLE_HEIGHT;
    
    // ========== 상방 장애물 (공중) ==========
    public static final int AIR_OBSTACLE_WIDTH = 100;
    public static final int AIR_OBSTACLE_HEIGHT = 200;
    public static final int AIR_OBSTACLE_Y = CHARACTER_Y - AIR_OBSTACLE_HEIGHT + 10;
    
    // ========== 일반 몬스터 (지상) ==========
    public static final int MONSTER_WIDTH = 150;
    public static final int MONSTER_HEIGHT = 150;
    public static final int MONSTER_Y = GROUND_Y - MONSTER_HEIGHT;

    // ========== 보스 몬스터 (지상) ==========
    public static final int BOSS_WIDTH = 300;
    public static final int BOSS_HEIGHT = 300;
    public static final int BOSS_Y = GROUND_Y - BOSS_HEIGHT;
    
    // ========== 스킬 범위 ==========
    public static final int SHOUT_RANGE = GAME_WIDTH - (CHARACTER_X + CHARACTER_WIDTH);
    public static final int THRUST_RANGE = 300;
    public static final int SLASH_RANGE = 250;
    
}