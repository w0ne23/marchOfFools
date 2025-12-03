package marchoffools.common.model;

import java.io.Serializable;

public class PlayerInfo implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String playerId;
    private String playerName;
    private int role;
    private boolean ready;
    private int score; 
    
    public PlayerInfo() {}
    
    public PlayerInfo(String playerId, String playerName) {
        this.playerId = playerId;
        this.playerName = playerName;
        this.role = 0; // ROLE_NONE
        this.ready = false;
        this.score = 0;
    }
    
    // Getters and Setters
    public String getPlayerId() {
        return playerId;
    }
    
    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }
    
    public String getPlayerName() {
        return playerName;
    }
    
    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }
    
    public int getRole() {
        return role;
    }
    
    public void setRole(int role) {
        this.role = role;
    }
    
    public boolean isReady() {
        return ready;
    }
    
    public void setReady(boolean ready) {
        this.ready = ready;
    }
    
    public int getScore() {
        return score;
    }
    
    public void setScore(int score) {
        this.score = score;
    }
    
    @Override
    public String toString() {
        return "PlayerInfo{" +
               "playerId='" + playerId + '\'' +
               ", playerName='" + playerName + '\'' +
               ", role=" + role +
               ", ready=" + ready +
               ", score=" + score +
               '}';
    }
}