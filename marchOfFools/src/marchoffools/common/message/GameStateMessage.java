package marchoffools.common.message;

import java.util.List;
import marchoffools.common.model.PlayerInfo;
import marchoffools.common.protocol.Message;

public class GameStateMessage extends Message {
    private static final long serialVersionUID = 1L;
    
    private List<PlayerInfo> players;
    private List<ObstacleData> obstacles;
    private double gameSpeed;
    private double distance;
    private int score;
    
    public GameStateMessage() {
        super();
    }
    
    // Getters and Setters
    public List<PlayerInfo> getPlayers() {
        return players;
    }
    
    public void setPlayers(List<PlayerInfo> players) {
        this.players = players;
    }
    
    public List<ObstacleData> getObstacles() {
        return obstacles;
    }
    
    public void setObstacles(List<ObstacleData> obstacles) {
        this.obstacles = obstacles;
    }
    
    public double getGameSpeed() {
        return gameSpeed;
    }
    
    public void setGameSpeed(double gameSpeed) {
        this.gameSpeed = gameSpeed;
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
    
    // 방해물 데이터 내부 클래스
    public static class ObstacleData implements java.io.Serializable {
        private static final long serialVersionUID = 1L;
        
        private String obstacleId;
        private double x;
        private double y;
        private String type;
        private boolean destroyed;
        
        public ObstacleData() {}
        
        public ObstacleData(String obstacleId, double x, double y, String type) {
            this.obstacleId = obstacleId;
            this.x = x;
            this.y = y;
            this.type = type;
            this.destroyed = false;
        }
        
        // Getters and Setters
        public String getObstacleId() {
            return obstacleId;
        }
        
        public void setObstacleId(String obstacleId) {
            this.obstacleId = obstacleId;
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
        
        public String getType() {
            return type;
        }
        
        public void setType(String type) {
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