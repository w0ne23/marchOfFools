package marchoffools.common.message;

import marchoffools.common.protocol.Message;

public class LeaveRoomMessage extends Message {
    private static final long serialVersionUID = 1L;
    
    private String roomId;
    
    public LeaveRoomMessage() {
        super();
    }
    
    public LeaveRoomMessage(String playerId, String roomId) {
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