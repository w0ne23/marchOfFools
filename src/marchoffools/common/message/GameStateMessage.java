package marchoffools.common.message;

import java.util.ArrayList;
import java.util.List;
import marchoffools.common.protocol.Message;

public class GameStateMessage extends Message {
    private static final long serialVersionUID = 1L;
    
    // ========== 기본 게임 정보 ==========
    private String roomId;
    private double distance;
    private int score;
    private int remainingTime;
    
    // ========== 캐릭터 상태 ==========
    private double playerY;                 // Y좌표
    private int charState;                  // 상태 (IDLE/JUMPING/SLIDING/ATTACKING)
    private boolean isInvincible;           // 무적 여부
    
    // ========== 스킬 쿨타임 ==========
    private long[] knightCooldowns;         // [외침, 찌르기, 베기]
    private long[] horseCooldowns;          // [점프, 슬라이드, 돌진]
    
    // ========== 활성 스킬 ==========
    private boolean[] activeSkills;         // 각 스킬 활성화 여부
    
    // ========== 장애물 목록 ==========
    private List<ObstacleData> obstacles;
    
    public GameStateMessage() {
        super();
        this.obstacles = new ArrayList<>();
        this.knightCooldowns = new long[3];
        this.horseCooldowns = new long[3];
        this.activeSkills = new boolean[6];
    }
    
    // ========== Getters and Setters ==========
    
    public String getRoomId() {
        return roomId;
    }
    
    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }
    
    public double getDistance() {
        return distance;
    }
    
    public void setDistance(double distance) {
        this.distance = distance;
    }
    
    public int getScore() {
        return score;
    }
    
    public void setScore(int score) {
        this.score = score;
    }
    
    public int getRemainingTime() {
        return remainingTime;
    }
    
    public void setRemainingTime(int remainingTime) {
        this.remainingTime = remainingTime;
    }
    
    public double getPlayerY() {
        return playerY;
    }
    
    public void setPlayerY(double playerY) {
        this.playerY = playerY;
    }
    
    public int getCharState() {
        return charState;
    }
    
    public void setCharState(int charState) {
        this.charState = charState;
    }
    
    public boolean isInvincible() {
        return isInvincible;
    }
    
    public void setInvincible(boolean invincible) {
        isInvincible = invincible;
    }
    
    public long[] getKnightCooldowns() {
        return knightCooldowns;
    }
    
    public void setKnightCooldowns(long[] knightCooldowns) {
        this.knightCooldowns = knightCooldowns;
    }
    
    public long[] getHorseCooldowns() {
        return horseCooldowns;
    }
    
    public void setHorseCooldowns(long[] horseCooldowns) {
        this.horseCooldowns = horseCooldowns;
    }
    
    public boolean[] getActiveSkills() {
        return activeSkills;
    }
    
    public void setActiveSkills(boolean[] activeSkills) {
        this.activeSkills = activeSkills;
    }
    
    public List<ObstacleData> getObstacles() {
        return obstacles;
    }
    
    public void setObstacles(List<ObstacleData> obstacles) {
        this.obstacles = obstacles;
    }
    
    // ========== 장애물 데이터 내부 클래스 ==========
    
    public static class ObstacleData implements java.io.Serializable {
        private static final long serialVersionUID = 1L;
        
        private String id;
        private double x;
        private double y;
        private int type;
        private boolean destroyed;
        
        public ObstacleData() {}
        
        public ObstacleData(String id, double x, double y, int type) {
            this.id = id;
            this.x = x;
            this.y = y;
            this.type = type;
            this.destroyed = false;
        }
        
        // Getters and Setters
        
        public String getId() {
            return id;
        }
        
        public void setId(String id) {
            this.id = id;
        }
        
        public double getX() {
            return x;
        }
        
        public void setX(double x) {
            this.x = x;
        }
        
        public double getY() {
            return y;
        }
        
        public void setY(double y) {
            this.y = y;
        }
        
        public int getType() {
            return type;
        }
        
        public void setType(int type) {
            this.type = type;
        }
        
        public boolean isDestroyed() {
            return destroyed;
        }
        
        public void setDestroyed(boolean destroyed) {
            this.destroyed = destroyed;
        }
    }
}