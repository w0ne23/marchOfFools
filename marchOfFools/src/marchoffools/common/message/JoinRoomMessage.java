package marchoffools.common.message;

import marchoffools.common.protocol.Message;

public class JoinRoomMessage extends Message {
    private static final long serialVersionUID = 1L;
    
    private String roomId;
    
    public JoinRoomMessage() {
        super();
    }
    
    public JoinRoomMessage(String playerId, String roomId) {
        super(playerId);
        this.roomId = roomId;
    }
    
    public String getRoomId() {
        return roomId;
    }
    
    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }
}