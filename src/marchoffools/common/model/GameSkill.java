package marchoffools.common.model;

/**
 * 게임 스킬 정의
 */
public class GameSkill {
    
    // ========== 스킬 ID 상수 ==========
    public static final int SKILL_SHOUT = 0;    // 기사 - 외침
    public static final int SKILL_THRUST = 1;   // 기사 - 찌르기
    public static final int SKILL_SLASH = 2;    // 기사 - 베기
    public static final int SKILL_JUMP = 3;     // 말 - 점프
    public static final int SKILL_SLIDE = 4;    // 말 - 슬라이드
    public static final int SKILL_DASH = 5;     // 말 - 돌진
    
    // ========== 스킬 쿨타임 (밀리초) ==========
    public static final long[] SKILL_COOLDOWNS = {
        30000,  // 외침: 30초
        500,   	// 찌르기: 0.5초
        500,   	// 베기: 0.5초
        0,      // 점프: 쿨타임 없음
        0,      // 슬라이드: 쿨타임 없음
        10000   // 돌진: 10초
    };
    
    // ========== 스킬 지속 시간 (밀리초) ==========
    public static final long[] SKILL_DURATIONS = {
        500,    // 외침: 0.5초
        100,    // 찌르기: 0.1초
        100,    // 베기: 0.1초
        0,      // 점프: 포물선 계산
        0,      // 슬라이드: 키 입력 지속
        1000    // 돌진: 1초
    };
    
    private GameSkill() {
        // 유틸리티 클래스 - 인스턴스화 방지
    }
}