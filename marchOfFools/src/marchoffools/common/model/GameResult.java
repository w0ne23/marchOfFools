package marchoffools.common.model;

import java.io.Serializable;
import java.util.Date;

public class GameResult implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String roomId;
    private String player1Id;
    private String player2Id;
    private double distance;
    private int totalScore;
    private int obstaclesDestroyed;
    private int obstaclesAvoided;
    private long playTime; // milliseconds
    private Date gameDate;
    
    public GameResult() {
        this.gameDate = new Date();
    }
    
    // Getter & Setter
    public String getRoomId() {
        return roomId;
    }
    
    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }
    
    public String getPlayer1Id() {
        return player1Id;
    }
    
    public void setPlayer1Id(String player1Id) {
        this.player1Id = player1Id;
    }
    
    public String getPlayer2Id() {
        return player2Id;
    }
    
    public void setPlayer2Id(String player2Id) {
        this.player2Id = player2Id;
    }
    
    public double getDistance() {
        return distance;
    }
    
    public void setDistance(double distance) {
        this.distance = distance;
    }
    
    public int getTotalScore() {
        return totalScore;
    }
    
    public void setTotalScore(int totalScore) {
        this.totalScore = totalScore;
    }
    
    public int getObstaclesDestroyed() {
        return obstaclesDestroyed;
    }
    
    public void setObstaclesDestroyed(int obstaclesDestroyed) {
        this.obstaclesDestroyed = obstaclesDestroyed;
    }
    
    public int getObstaclesAvoided() {
        return obstaclesAvoided;
    }
    
    public void setObstaclesAvoided(int obstaclesAvoided) {
        this.obstaclesAvoided = obstaclesAvoided;
    }
    
    public long getPlayTime() {
        return playTime;
    }
    
    public void setPlayTime(long playTime) {
        this.playTime = playTime;
    }
    
    public Date getGameDate() {
        return gameDate;
    }
    
    public void setGameDate(Date gameDate) {
        this.gameDate = gameDate;
    }
}