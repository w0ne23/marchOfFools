package marchoffools.common.protocol;

import java.io.Serializable;

public class Packet implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private MessageType type;
    private String senderId;
    private long timestamp;
    private Object data;
    
    public Packet() {
        this.timestamp = System.currentTimeMillis();
    }
    
    public Packet(MessageType type, String senderId, Object data) {
        this.type = type;
        this.senderId = senderId;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }
    
    // Getter & Setter
    public MessageType getType() {
        return type;
    }
    
    public void setType(MessageType type) {
        this.type = type;
    }
    
    public String getSenderId() {
        return senderId;
    }
    
    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    
    public Object getData() {
        return data;
    }
    
    public void setData(Object data) {
        this.data = data;
    }
    
    @Override
    public String toString() {
        return "Packet{type=" + type + ", senderId=" + senderId + 
               ", timestamp=" + timestamp + ", data=" + data + "}";
    }
}