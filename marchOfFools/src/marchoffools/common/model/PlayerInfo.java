package marchoffools.common.model;

import java.io.Serializable;

public class PlayerInfo implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String playerId;
    private String playerName;
    private int characterType;    // RoomActionMessage의 캐릭터 상수 사용
    private boolean ready;
    private int health;
    
    public PlayerInfo() {}
    
    public PlayerInfo(String playerId, String playerName) {
        this.playerId = playerId;
        this.playerName = playerName;
        this.ready = false;
        this.health = 100;
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
    
    public int getCharacterType() {
        return characterType;
    }
    
    public void setCharacterType(int characterType) {
        this.characterType = characterType;
    }
    
    public boolean isReady() {
        return ready;
    }
    
    public void setReady(boolean ready) {
        this.ready = ready;
    }
    
    public int getHealth() {
        return health;
    }
    
    public void setHealth(int health) {
        this.health = health;
    }
}