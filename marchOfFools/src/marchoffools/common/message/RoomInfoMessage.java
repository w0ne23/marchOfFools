package marchoffools.common.message;

import java.util.ArrayList;
import java.util.List;
import marchoffools.common.model.PlayerInfo;
import marchoffools.common.protocol.Message;

public class RoomInfoMessage extends Message {
    private static final long serialVersionUID = 1L;
    
    // 방 상태 상수
    public static final int STATUS_NONE = 0;
    public static final int ROOM_CREATED = 1;
    public static final int ROOM_UPDATED = 2;
    public static final int PLAYER_JOINED = 3;
    public static final int PLAYER_LEFT = 4;
    public static final int PLAYER_READY_CHANGED = 5;
    public static final int CHARACTER_SELECTED = 6;
    public static final int GAME_STARTING = 7;
    
    private int status;               // 방 상태
    private String roomId;            // 방 ID
    private String hostId;            // 방장 ID
    private List<PlayerInfo> players; // 플레이어 목록
    private boolean canStart;         // 게임 시작 가능 여부
    
    public RoomInfoMessage() {
        super();
        this.players = new ArrayList<>();
    }
    
    public RoomInfoMessage(int status, String roomId) {
        super();
        this.status = status;
        this.roomId = roomId;
        this.players = new ArrayList<>();
    }
    
    // Getters and Setters
    public int getStatus() {
        return status;
    }
    
    public void setStatus(int status) {
        this.status = status;
    }
    
    public String getRoomId() {
        return roomId;
    }
    
    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }
    
    public String getHostId() {
        return hostId;
    }
    
    public void setHostId(String hostId) {
        this.hostId = hostId;
    }
    
    public List<PlayerInfo> getPlayers() {
        return players;
    }
    
    public void setPlayers(List<PlayerInfo> players) {
        this.players = players;
    }
    
    public boolean isCanStart() {
        return canStart;
    }
    
    public void setCanStart(boolean canStart) {
        this.canStart = canStart;
    }
    
    @Override
    public String toString() {
        return "RoomInfoMessage{status=" + status + 
               ", roomId=" + roomId + 
               ", players=" + players.size() + 
               ", canStart=" + canStart + "}";
    }
}