package marchoffools.common.protocol;

import java.io.Serializable;

public class Packet implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private MessageType type;
    private Object data;
    
    public Packet(){};
    
    public Packet(MessageType type, Object data) {
        this.type = type;
        this.data = data;
    }
    
    // Getter & Setter
    public MessageType getType() {
        return type;
    }
    
    public void setType(MessageType type) {
        this.type = type;
    }
    
    public Object getData() {
        return data;
    }
    
    public void setData(Object data) {
        this.data = data;
    }
    
    @Override
    public String toString() {
        return "Packet{type=" + type + ", data=" + data + "}";
    }
}