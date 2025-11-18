package marchoffools.common.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class RoomInfo implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String roomId;
    private String roomName;
    private String hostId;
    private List<PlayerInfo> players;
    private int maxPlayers;
    private boolean isPrivate;
    private boolean isPlaying;
    
    public RoomInfo() {
        this.players = new ArrayList<>();
        this.maxPlayers = 2; // 2인 게임
    }
    
    public RoomInfo(String roomId, String roomName, String hostId) {
        this.roomId = roomId;
        this.roomName = roomName;
        this.hostId = hostId;
        this.players = new ArrayList<>();
        this.maxPlayers = 2;
        this.isPrivate = false;
        this.isPlaying = false;
    }
    
    public boolean isFull() {
        return players.size() >= maxPlayers;
    }
    
    // Getters and Setters
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
    
    public int getMaxPlayers() {
        return maxPlayers;
    }
    
    public void setMaxPlayers(int maxPlayers) {
        this.maxPlayers = maxPlayers;
    }
    
    public boolean isPrivate() {
        return isPrivate;
    }
    
    public void setPrivate(boolean isPrivate) {
        this.isPrivate = isPrivate;
    }
    
    public boolean isPlaying() {
        return isPlaying;
    }
    
    public void setPlaying(boolean isPlaying) {
        this.isPlaying = isPlaying;
    }
}