package marchoffools.server.game;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class RoomManager {
    
    private Map<String, Room> rooms;
    private Random random;
    
    public RoomManager() {
        this.rooms = new ConcurrentHashMap<>();
        this.random = new Random();
    }
    
    // 방 생성
    public Room createRoom(String hostId, String hostName) {
        String roomId = generateRoomId();
        
        Room room = new Room(roomId, hostId);
        rooms.put(roomId, room);
        
        System.out.println("방 생성: " + roomId + " (방장: " + hostName + ")");
        System.out.println("현재 방 개수: " + rooms.size());
        
        return room;
    }
    
    // 방 ID로 방 찾기
    public Room getRoom(String roomId) {
        return rooms.get(roomId);
    }
    
    // 빠른 매칭 - 입장 가능한 방 찾기
    public Room findAvailableRoom() {
        for (Room room : rooms.values()) {
            if (!room.isFull() && !room.isPlaying()) {
                System.out.println("매칭된 방: " + room.getRoomId());
                return room;
            }
        }
        
        System.out.println("사용 가능한 방 없음");
        return null;
    }
    
    // 방 제거
    public void removeRoom(String roomId) {
        Room removed = rooms.remove(roomId);
        if (removed != null) {
            System.out.println("방 삭제: " + roomId);
            System.out.println("현재 방 개수: " + rooms.size());
        }
    }
    
    // 플레이어가 속한 방 찾기
    public Room findRoomByPlayer(String playerId) {
        for (Room room : rooms.values()) {
            if (room.hasPlayer(playerId)) {
                return room;
            }
        }
        return null;
    }
    
    // 방 ID 생성 (6자리 숫자)
    private String generateRoomId() {
        String roomId;
        do {
            int number = random.nextInt(900000) + 100000;  // 100000 ~ 999999
            roomId = String.valueOf(number);
        } while (rooms.containsKey(roomId));
        
        return roomId;
    }
    
    // 전체 방 개수
    public int getRoomCount() {
        return rooms.size();
    }
    
    // 전체 플레이어 수
    public int getTotalPlayerCount() {
        int count = 0;
        for (Room room : rooms.values()) {
            count += room.getPlayerCount();
        }
        return count;
    }
}