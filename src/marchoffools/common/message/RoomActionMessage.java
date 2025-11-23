package marchoffools.common.message;

import marchoffools.common.protocol.Message;

public class RoomActionMessage extends Message {
    private static final long serialVersionUID = 1L;
    
    // 액션 타입 상수
    public static final int ACTION_NONE = 0;
    public static final int CONNECT = 1;          // 서버 연결
    public static final int DISCONNECT = 2;       // 서버 연결 종료
    public static final int CREATE_ROOM = 3;
    public static final int JOIN_ROOM = 4;
    public static final int LEAVE_ROOM = 5;
    public static final int QUICK_MATCH = 6;
    public static final int REQUEST_RANDOM_ROLE = 7;
    public static final int SELECT_CHARACTER = 8;
    public static final int PLAYER_READY = 9;
    public static final int START_GAME = 10;
    
    // 캐릭터 타입 상수
    public static final int ROLE_NONE = 0;
    public static final int ROLE_KNIGHT = 1;  // 기사
    public static final int ROLE_HORSE = 2;   // 기마
    
    private int action;           // 액션 타입
    private String roomId;        // 방 ID (입장/퇴장 시)
    private String playerName;    // 플레이어 이름 (연결 시)
    private int roleType;    // 캐릭터 타입 (선택 시)
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
    
    public String getPlayerName() {
        return playerName;
    }
    
    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }
    
    public int getRoleType() {
        return roleType;
    }
    
    public void setRoleType(int characterType) {
        this.roleType = characterType;
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
               ", playerName=" + playerName +
               ", roleType=" + roleType + 
               ", ready=" + ready + "}";
    }
}