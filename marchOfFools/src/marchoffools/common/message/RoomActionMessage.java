package marchoffools.common.message;

import marchoffools.common.protocol.Message;

public class RoomActionMessage extends Message {
    private static final long serialVersionUID = 1L;
    
    // 액션 타입 상수
    public static final int CREATE_ROOM = 1;
    public static final int JOIN_ROOM = 2;
    public static final int LEAVE_ROOM = 3;
    public static final int QUICK_MATCH = 4;
    public static final int SELECT_CHARACTER = 5;
    public static final int PLAYER_READY = 6;
    public static final int START_GAME = 7;
    
    // 캐릭터 타입 상수
    public static final int CHARACTER_KNIGHT = 1;  // 기사
    public static final int CHARACTER_HORSE = 2;   // 기마
    
    private int action;           // 액션 타입
    private String roomId;        // 방 ID (입장/퇴장 시)
    private String roomName;      // 방 이름 (생성 시)
    private int characterType;    // 캐릭터 타입 (선택 시)
    private boolean ready;        // 준비 상태
    
    public RoomActionMessage() {
        super();
    }
    
    public RoomActionMessage(String playerId, int action) {
        super(playerId);
        this.action = action;
    }
    
    // Getters and Setters
    public int getAction() {
        return action;
    }
    
    public void setAction(int action) {
        this.action = action;
    }
    
    public String getRoomId() {
        return roomId;
    }
    
    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }
    
    public String getRoomName() {
        return roomName;
    }
    
    public void setRoomName(String roomName) {
        this.roomName = roomName;
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
    
    @Override
    public String toString() {
        return "RoomActionMessage{action=" + action + 
               ", playerId=" + playerId + 
               ", roomId=" + roomId + 
               ", characterType=" + characterType + 
               ", ready=" + ready + "}";
    }
}