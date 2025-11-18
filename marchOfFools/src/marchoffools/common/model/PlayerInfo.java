package marchoffools.common.model;

import java.io.Serializable;
import marchoffools.common.message.SelectCharacterMessage.CharacterType;

public class PlayerInfo implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String playerId;
    private String playerName;
    private CharacterType character;
    private boolean ready;
    private double x;
    private double y;
    private int health;
    
    public PlayerInfo() {}
    
    public PlayerInfo(String playerId, String playerName) {
        this.playerId = playerId;
        this.playerName = playerName;
        this.ready = false;
        this.health = 100;
    }
    
    // Getter & Setter
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
    
    public CharacterType getCharacter() {
        return character;
    }
    
    public void setCharacter(CharacterType character) {
        this.character = character;
    }
    
    public boolean isReady() {
        return ready;
    }
    
    public void setReady(boolean ready) {
        this.ready = ready;
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
    
    public int getHealth() {
        return health;
    }
    
    public void setHealth(int health) {
        this.health = health;
    }
}