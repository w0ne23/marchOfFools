package marchoffools.common.message;

import marchoffools.common.protocol.Message;

public class CreateRoomMessage extends Message {
    private static final long serialVersionUID = 1L;
    
    private String roomName;
    private boolean isPrivate;
    
    public CreateRoomMessage() {
        super();
    }
    
    public CreateRoomMessage(String playerId, String roomName, boolean isPrivate) {
        super(playerId);
        this.roomName = roomName;
        this.isPrivate = isPrivate;
    }
    
    public String getRoomName() {
        return roomName;
    }
    
    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }
    
    public boolean isPrivate() {
        return isPrivate;
    }
    
    public void setPrivate(boolean isPrivate) {
        this.isPrivate = isPrivate;
    }
}