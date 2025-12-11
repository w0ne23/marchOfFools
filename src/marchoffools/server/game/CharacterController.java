package marchoffools.server.game;

/**
 * 캐릭터 상태 및 스킬 관리 컨트롤러
 */
public class CharacterController {

    // ========== 캐릭터 상태 상수 ==========
    public static final int STATE_IDLE = 0;
    public static final int STATE_JUMPING = 1;
    public static final int STATE_SLIDING = 2;
    public static final int STATE_ATTACKING = 3;

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
        500,    // 외침: 0.1초
        100,    // 찌르기: 0.1초
        100,    // 베기: 0.1초
        0,      // 점프: 포물선 계산
        0,      // 슬라이드: 키 입력 지속
        1000    // 돌진: 1초
    };

    // ========== 점프 관련 상수 ==========
    private static final double JUMP_INITIAL_VELOCITY = 500.0; // 초기 속도 (픽셀/초)
    private static final double GRAVITY = 1200.0;              // 중력 (픽셀/초²)
    private static final double GROUND_Y = 300.0;              // 지면 Y좌표

    // ========== 캐릭터 상태 ==========
    private int charState = STATE_IDLE;
    private long stateStartTime = 0;

    // ========== 점프 상태 ==========
    private double playerY = GROUND_Y;
    private double verticalVelocity = 0.0;

    // ========== 무적 상태 (돌진) ==========
    private boolean isInvincible = false;
    private long dashEndTime = 0;

    // ========== 스킬 쿨타임 관리 ==========
    private long[] lastSkillUsedTime = new long[6];
    private long[] skillActiveUntil = new long[6];

    // ========== 캐릭터 상태 관리 ==========

    /**
     * 캐릭터 상태 변경
     */
    public void setCharState(int state) {
        this.charState = state;
        this.stateStartTime = System.currentTimeMillis();
    }

    /**
     * 캐릭터 상태 자동 업데이트
     */
    public void updateCharacterState(double deltaTime) {
        long now = System.currentTimeMillis();

        // 점프 상태 처리
        if (charState == STATE_JUMPING) {
            updateJump(deltaTime);
        }

        // 슬라이드 자동 복귀 (예외적으로 0.5초 후)
        if (charState == STATE_SLIDING) {
            long elapsed = now - stateStartTime;
            if (elapsed > 500) {
                charState = STATE_IDLE;
            }
        }

        // 공격 상태 자동 복귀
        if (charState == STATE_ATTACKING) {
            long elapsed = now - stateStartTime;
            if (elapsed > 300) { // 0.3초
                charState = STATE_IDLE;
            }
        }

        // 무적 상태 체크
        if (isInvincible && now >= dashEndTime) {
            isInvincible = false;
        }
    }

    /**
     * 점프 물리 계산
     */
    private void updateJump(double deltaTime) {
        // 중력 적용
        verticalVelocity -= GRAVITY * deltaTime;
        playerY += verticalVelocity * deltaTime;

        // 착지 체크
        if (playerY >= GROUND_Y) {
            playerY = GROUND_Y;
            verticalVelocity = 0.0;
            charState = STATE_IDLE;
        }
    }

    /**
     * 점프 시작
     */
    public void startJump() {
        if (charState == STATE_JUMPING) return; // 이미 점프 중
        
        charState = STATE_JUMPING;
        verticalVelocity = JUMP_INITIAL_VELOCITY;
        stateStartTime = System.currentTimeMillis();
    }

    // ========== 스킬 관리 ==========

    /**
     * 특정 스킬을 사용할 수 있는지 확인
     */
    public boolean canUseSkill(int skillId) {
        if (skillId < 0 || skillId >= 6) return false;

        // 쿨타임이 없는 스킬
        if (SKILL_COOLDOWNS[skillId] == 0) return true;

        long now = System.currentTimeMillis();
        return (now - lastSkillUsedTime[skillId]) >= SKILL_COOLDOWNS[skillId];
    }

    /**
     * 스킬 사용 (쿨타임 시작)
     */
    public boolean useSkill(int skillId) {
        if (!canUseSkill(skillId)) return false;

        long now = System.currentTimeMillis();
        lastSkillUsedTime[skillId] = now;

        // 찌르기/베기는 쿨타임 묶기
        if (skillId == SKILL_THRUST) {
            lastSkillUsedTime[SKILL_SLASH] = now;
        } else if (skillId == SKILL_SLASH) {
            lastSkillUsedTime[SKILL_THRUST] = now;
        }

        return true;
    }

    /**
     * 스킬 활성화 (효과 시작)
     */
    public void activateSkill(int skillId) {
        if (skillId < 0 || skillId >= 6) return;

        long now = System.currentTimeMillis();
        skillActiveUntil[skillId] = now + SKILL_DURATIONS[skillId];

        // 특수 처리
        switch (skillId) {
            case SKILL_DASH:
                isInvincible = true;
                dashEndTime = now + 1000; // 1초 무적
                break;
            case SKILL_SHOUT:
            case SKILL_THRUST:
            case SKILL_SLASH:
                charState = STATE_ATTACKING;
                stateStartTime = now;
                break;
        }
    }

    /**
     * 특정 스킬이 활성화 상태인지 확인
     */
    public boolean hasActiveSkill(int skillId) {
        if (skillId < 0 || skillId >= 6) return false;

        long now = System.currentTimeMillis();
        return now < skillActiveUntil[skillId];
    }

    /**
     * 활성화된 스킬이 있는지 확인
     */
    public boolean hasAnyActiveSkill() {
        long now = System.currentTimeMillis();
        for (long activeUntil : skillActiveUntil) {
            if (now < activeUntil) return true;
        }
        return false;
    }

    /**
     * 만료된 스킬 정리
     */
    public void updateSkills() {
        long now = System.currentTimeMillis();
        for (int i = 0; i < skillActiveUntil.length; i++) {
            if (skillActiveUntil[i] > 0 && now >= skillActiveUntil[i]) {
                skillActiveUntil[i] = 0;
            }
        }
    }

    /**
     * 남은 쿨타임 반환
     */
    public long getRemainingCooldown(int skillId) {
        if (skillId < 0 || skillId >= 6) return 0;

        long now = System.currentTimeMillis();
        long elapsed = now - lastSkillUsedTime[skillId];

        return Math.max(0, SKILL_COOLDOWNS[skillId] - elapsed);
    }

    /**
     * 모든 스킬의 남은 쿨타임 반환
     */
    public long[] getAllCooldowns() {
        long[] cooldowns = new long[6];
        for (int i = 0; i < 6; i++) {
            cooldowns[i] = getRemainingCooldown(i);
        }
        return cooldowns;
    }

    /**
     * 활성화된 스킬 배열 반환
     */
    public boolean[] getActiveSkills() {
        boolean[] active = new boolean[6];
        for (int i = 0; i < 6; i++) {
            active[i] = hasActiveSkill(i);
        }
        return active;
    }

    // ========== Getters ==========

    public int getCharState() {
        return charState;
    }

    public double getPlayerY() {
        return playerY;
    }

    public boolean isInvincible() {
        return isInvincible;
    }

    public long[] getLastSkillUsedTime() {
        return lastSkillUsedTime;
    }

    public long[] getSkillActiveUntil() {
        return skillActiveUntil;
    }

    // ========== 초기화 ==========

    /**
     * 상태 초기화
     */
    public void reset() {
        charState = STATE_IDLE;
        stateStartTime = 0;
        playerY = GROUND_Y;
        verticalVelocity = 0.0;
        isInvincible = false;
        dashEndTime = 0;
        lastSkillUsedTime = new long[6];
        skillActiveUntil = new long[6];
    }
}