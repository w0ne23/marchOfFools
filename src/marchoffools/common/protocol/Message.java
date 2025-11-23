package marchoffools.common.protocol;

import java.io.Serializable;

// 추상 기본 클래스
public abstract class Message implements Serializable {
    private static final long serialVersionUID = 1L;
    
    protected String playerId;
    protected long timestamp;
    
    public Message() {
        this.timestamp = System.currentTimeMillis();
    }
    
    public Message(String playerId) {
        this.playerId = playerId;
        this.timestamp = System.currentTimeMillis();
    }
    
    // Getters and Setters
    public String getPlayerId() {
        return playerId;
    }
    
    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}