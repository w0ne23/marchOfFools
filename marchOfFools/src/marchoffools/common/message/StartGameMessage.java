package marchoffools.common.message;

import marchoffools.common.protocol.Message;

public class StartGameMessage extends Message {
    private static final long serialVersionUID = 1L;
    
    private String roomId;
    
    public StartGameMessage() {
        super();
    }
    
    public StartGameMessage(String playerId, String roomId) {
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