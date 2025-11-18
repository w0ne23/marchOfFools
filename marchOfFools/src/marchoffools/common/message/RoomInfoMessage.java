package marchoffools.common.message;

import marchoffools.common.model.RoomInfo;
import marchoffools.common.protocol.Message;

public class RoomInfoMessage extends Message {
    private static final long serialVersionUID = 1L;
    
    private RoomInfo roomInfo;
    
    public RoomInfoMessage() {
        super();
    }
    
    public RoomInfoMessage(RoomInfo roomInfo) {
        super();
        this.roomInfo = roomInfo;
    }
    
    public RoomInfo getRoomInfo() {
        return roomInfo;
    }
    
    public void setRoomInfo(RoomInfo roomInfo) {
        this.roomInfo = roomInfo;
    }
}