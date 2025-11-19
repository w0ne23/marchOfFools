package marchoffools.common.message;

import java.util.ArrayList;
import java.util.List;
import marchoffools.common.protocol.Message;

public class GameStateMessage extends Message {
    private static final long serialVersionUID = 1L;
    
    // 플레이어 상태
    private double player1X;
    private double player1Y;
    private int player1Health;
    private double player2X;
    private double player2Y;
    private int player2Health;
    
    // 게임 진행 상태
    private double distance;          // 진행 거리
    private double gameSpeed;         // 게임 속도
    private int score;                // 현재 점수
    
    // 방해물 목록
    private List<ObstacleData> obstacles;
    
    // 아이템 효과 상태
    private boolean player1HasShield;
    private boolean player2HasShield;
    private long speedBoostEndTime;
    
    public GameStateMessage() {
        super();
        this.obstacles = new ArrayList<>();
    }
    
    // Getters and Setters
    public double getPlayer1X() {
        return player1X;
    }
    
    public void setPlayer1X(double player1X) {
        this.player1X = player1X;
    }
    
    public double getPlayer1Y() {
        return player1Y;
    }
    
    public void setPlayer1Y(double player1Y) {
        this.player1Y = player1Y;
    }
    
    public int getPlayer1Health() {
        return player1Health;
    }
    
    public void setPlayer1Health(int player1Health) {
        this.player1Health = player1Health;
    }
    
    public double getPlayer2X() {
        return player2X;
    }
    
    public void setPlayer2X(double player2X) {
        this.player2X = player2X;
    }
    
    public double getPlayer2Y() {
        return player2Y;
    }
    
    public void setPlayer2Y(double player2Y) {
        this.player2Y = player2Y;
    }
    
    public int getPlayer2Health() {
        return player2Health;
    }
    
    public void setPlayer2Health(int player2Health) {
        this.player2Health = player2Health;
    }
    
    public double getDistance() {
        return distance;
    }
    
    public void setDistance(double distance) {
        this.distance = distance;
    }
    
    public double getGameSpeed() {
        return gameSpeed;
    }
    
    public void setGameSpeed(double gameSpeed) {
        this.gameSpeed = gameSpeed;
    }
    
    public int getScore() {
        return score;
    }
    
    public void setScore(int score) {
        this.score = score;
    }
    
    public List<ObstacleData> getObstacles() {
        return obstacles;
    }
    
    public void setObstacles(List<ObstacleData> obstacles) {
        this.obstacles = obstacles;
    }
    
    public boolean isPlayer1HasShield() {
        return player1HasShield;
    }
    
    public void setPlayer1HasShield(boolean player1HasShield) {
        this.player1HasShield = player1HasShield;
    }
    
    public boolean isPlayer2HasShield() {
        return player2HasShield;
    }
    
    public void setPlayer2HasShield(boolean player2HasShield) {
        this.player2HasShield = player2HasShield;
    }
    
    public long getSpeedBoostEndTime() {
        return speedBoostEndTime;
    }
    
    public void setSpeedBoostEndTime(long speedBoostEndTime) {
        this.speedBoostEndTime = speedBoostEndTime;
    }
    
    // 방해물 데이터 내부 클래스
    public static class ObstacleData implements java.io.Serializable {
        private static final long serialVersionUID = 1L;
        
        private String id;
        private double x;
        private double y;
        private int type;       // 방해물 타입 (플래그)
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