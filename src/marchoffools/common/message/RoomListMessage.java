package marchoffools.common.message;

import java.io.Serializable;
import java.util.List;

public class RoomListMessage implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private List<RoomSummary> rooms;
    
    public RoomListMessage(List<RoomSummary> rooms) {
        this.rooms = rooms;
    }
    
    public List<RoomSummary> getRooms() {
        return rooms;
    }
    
    // 방 요약 정보 (내부 클래스)
    public static class RoomSummary implements Serializable {
        private static final long serialVersionUID = 1L;
        
        private String roomId;
        private String hostName;
        private int playerCount;
        private int maxPlayers;
        private boolean playing;
        
        public RoomSummary(String roomId, String hostName, int playerCount, int maxPlayers, boolean playing) {
            this.roomId = roomId;
            this.hostName = hostName;
            this.playerCount = playerCount;
            this.maxPlayers = maxPlayers;
            this.playing = playing;
        }
        
        // Getters
        public String getRoomId() { return roomId; }
        public String getHostName() { return hostName; }
        public int getPlayerCount() { return playerCount; }
        public int getMaxPlayers() { return maxPlayers; }
        public boolean isPlaying() { return playing; }
        public boolean isFull() { return playerCount >= maxPlayers; }
    }
}