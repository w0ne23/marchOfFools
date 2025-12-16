package marchoffools.server.game;

import marchoffools.common.model.GameConstants;
import marchoffools.common.model.GameSkill;

/**
 * 캐릭터 상태 및 스킬 관리 컨트롤러
 */
public class CharacterController {

    // ========== 캐릭터 상태 상수 ==========
    public static final int STATE_IDLE = 0;
    public static final int STATE_JUMPING = 1;
    public static final int STATE_SLIDING = 2;

    // ========== 점프 관련 상수 ==========
    private static final double GRAVITY = 2000.0; // 중력 (픽셀/초²)
    private static final double JUMP_INITIAL_VELOCITY = 1000;

    // ========== 슬라이드 관련 상수 ==========
    private static final double SLIDE_TRANSITION_SPEED = 1000.0; // 슬라이드 전환 속도 (픽셀/초)

    // ========== 캐릭터 상태 ==========
    private int charState = STATE_IDLE;
    private long stateStartTime = 0;
    private boolean isAttacking = false;
    private long attackEndTime = 0;

    // ========== 점프 상태 ==========
    private double playerY = GameConstants.CHARACTER_Y;
    private double verticalVelocity = 0.0;

    // ========== 슬라이드 상태 ==========
    private boolean isSliding = false;
    private double targetY = GameConstants.CHARACTER_Y;

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

        // 슬라이드 상태 처리
        if (charState == STATE_SLIDING || isSliding) {
            updateSlide(deltaTime);
        }

        // 공격 상태 복귀
        if (isAttacking && now >= attackEndTime) {
            isAttacking = false;
        }

        // 무적 상태 체크
        if (isInvincible && now >= dashEndTime) {
            isInvincible = false;
        }
    }

    /**
     * 점프 시작
     */
    public void startJump() {
        System.out.println("=== startJump() CALLED ===");
        System.out.println("  Current state: " + charState);
        System.out.println("  Is sliding: " + isSliding);
        System.out.println("  Current Y: " + playerY);
//        System.out.println("  JUMP_HEIGHT constant: " + GameConstants.JUMP_HEIGHT);
        System.out.println("  Calculated initial velocity: " + JUMP_INITIAL_VELOCITY);
        
        // 이미 점프 중이면 무시
        if (charState == STATE_JUMPING) {
            System.out.println("  ❌ Already jumping - ignored");
        }
        
        // 점프 실행
        charState = STATE_JUMPING;
        verticalVelocity = JUMP_INITIAL_VELOCITY;
        stateStartTime = System.currentTimeMillis();
        
        System.out.println("  ✅ Jump started!");
        System.out.println("  New state: " + charState);
        System.out.println("  Velocity: " + verticalVelocity + " px/s");
        System.out.println("========================");
    }
    
    /**
     * 점프 물리 계산
     */
    private void updateJump(double deltaTime) {
        // 중력 적용
        verticalVelocity -= GRAVITY * deltaTime;
        
        // Y 좌표 업데이트 (위로 올라갈 때 Y는 감소)
        playerY -= verticalVelocity * deltaTime;

        // 착지 체크
        if (playerY >= GameConstants.CHARACTER_Y) {
            playerY = GameConstants.CHARACTER_Y;
            verticalVelocity = 0.0;
            charState = STATE_IDLE;
            System.out.println("[Jump] Landed at Y=" + playerY);
        }
    }

    /**
     * 슬라이드 시작/종료
     */
    public void setSliding(boolean sliding) {
        // 점프 중에는 슬라이드 불가
        if (sliding && charState == STATE_JUMPING) {
            System.out.println("[CharController] Cannot slide - currently jumping");
            return;
        }

        if (this.isSliding != sliding) {
            this.isSliding = sliding;
            
            if (sliding) {
                charState = STATE_SLIDING;
                System.out.println("[CharController] Slide started");
            } else {
                System.out.println("[CharController] Slide ended");
            }
        }
    }

    /**
     * 슬라이드 처리
     */
    private void updateSlide(double deltaTime) {
        if (isSliding) {
            targetY = GameConstants.CHARACTER_Y + GameConstants.SLIDE_HEIGHT;
        } else {
            targetY = GameConstants.CHARACTER_Y;
        }

        // 부드럽게 Y좌표 전환
        double diff = targetY - playerY;
        if (Math.abs(diff) > 0.1) {
            double move = Math.signum(diff) * SLIDE_TRANSITION_SPEED * deltaTime;
            
            if (Math.abs(move) > Math.abs(diff)) {
                playerY = targetY;
            } else {
                playerY += move;
            }
        } else {
            playerY = targetY;
        }

        // 슬라이드 상태 동기화
        if (isSliding && charState != STATE_SLIDING) {
            charState = STATE_SLIDING;
        } else if (!isSliding && charState == STATE_SLIDING && Math.abs(playerY - GameConstants.CHARACTER_Y) < 0.1) {
            charState = STATE_IDLE;
        }
    }

    // ========== 스킬 관리 ==========

    public boolean canUseSkill(int skillId) {
        if (skillId < 0 || skillId >= 6) return false;

        // 쿨타임이 없는 스킬
        if (GameSkill.SKILL_COOLDOWNS[skillId] == 0) return true;

        long now = System.currentTimeMillis();
        return (now - lastSkillUsedTime[skillId]) >= GameSkill.SKILL_COOLDOWNS[skillId];
    }

    public boolean useSkill(int skillId) {
        if (!canUseSkill(skillId)) return false;

        long now = System.currentTimeMillis();
        lastSkillUsedTime[skillId] = now;

        // 찌르기/베기는 쿨타임 묶기
        if (skillId == GameSkill.SKILL_THRUST) {
            lastSkillUsedTime[GameSkill.SKILL_SLASH] = now;
        } else if (skillId == GameSkill.SKILL_SLASH) {
            lastSkillUsedTime[GameSkill.SKILL_THRUST] = now;
        }

        return true;
    }

    public void activateSkill(int skillId) {
        if (skillId < 0 || skillId >= 6) return;

        long now = System.currentTimeMillis();
        skillActiveUntil[skillId] = now + GameSkill.SKILL_DURATIONS[skillId];

        // 스킬별 로직... 추후 분기 없이 구현
        switch (skillId) {
            case GameSkill.SKILL_DASH:
                isInvincible = true;
                dashEndTime = now + 1000;
                System.out.println("[CharController] Dash activated - invincible for 1s");
                break;
            case GameSkill.SKILL_SHOUT:
            case GameSkill.SKILL_THRUST:
            case GameSkill.SKILL_SLASH:
                isAttacking = true;
                attackEndTime = now + 300;
                System.out.println("[CharController] Attack skill activated: " + skillId);
                break;
        }
    }

    /** 
     * 활성화 스킬 확인
     */
    public boolean hasActiveSkill(int skillId) {
        if (skillId < 0 || skillId >= 6) return false;

        long now = System.currentTimeMillis();
        return now < skillActiveUntil[skillId];
    }

    public boolean[] getActiveSkills() {
        boolean[] active = new boolean[6];
        for (int i = 0; i < 6; i++) {
            active[i] = hasActiveSkill(i);
        }
        return active;
    }

    /**
     * 스킬 쿨타임 확인
     */
    public long getRemainingCooldown(int skillId) {
        if (skillId < 0 || skillId >= 6) return 0;

        long now = System.currentTimeMillis();
        long elapsed = now - lastSkillUsedTime[skillId];

        return Math.max(0, GameSkill.SKILL_COOLDOWNS[skillId] - elapsed);
    }

    public long[] getAllCooldowns() {
        long[] cooldowns = new long[6];
        for (int i = 0; i < 6; i++) {
            cooldowns[i] = getRemainingCooldown(i);
        }
        return cooldowns;
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

	public boolean isAttacking() {
	    return isAttacking;
	}

    public long[] getLastSkillUsedTime() {
        return lastSkillUsedTime;
    }

    public long[] getSkillActiveUntil() {
        return skillActiveUntil;
    }

    public boolean isSliding() {
        return isSliding;
    }

    // ========== 초기화 ==========

    /**
     * 상태 초기화
     */
    public void reset() {
        charState = STATE_IDLE;
        stateStartTime = 0;
        playerY = GameConstants.CHARACTER_Y;
        verticalVelocity = 0.0;
        isSliding = false;
        targetY = GameConstants.CHARACTER_Y;
        isInvincible = false;
        dashEndTime = 0;
        isAttacking = false;
        attackEndTime = 0;
        lastSkillUsedTime = new long[6];
        skillActiveUntil = new long[6];
        
        System.out.println("[CharController] Reset complete");
        System.out.println("  Initial Y: " + playerY);
        System.out.println("  Jump velocity: " + JUMP_INITIAL_VELOCITY);
    }
}