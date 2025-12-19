package marchoffools.server.game;

import java.util.*;
import marchoffools.common.model.GameModeType;
import marchoffools.common.model.PlayerInfo;
import marchoffools.common.message.RoomActionMessage;
import marchoffools.common.message.RoomInfoMessage;
import marchoffools.common.protocol.Message;
import marchoffools.common.protocol.MessageType;
import marchoffools.common.protocol.Packet;
import marchoffools.server.network.ClientHandler;

public class Room {
	
	public static final int STATUS_WAITING = 0;
    public static final int STATUS_PLAYING = 1;
    public static final int STATUS_FINISHED = 2;
    
    private String roomId;
    private String hostId;
    private Map<String, PlayerInfo> players;
	private Map<String, ClientHandler> handlers;
    private boolean playing;
    private boolean full;
    private int status;
    
    private static final int MAX_PLAYERS = 2;
    
    private GameSession gameSession;
    private GameModeType gameMode = GameModeType.INFINITE; // 기본값: 무한 모드
    
    public Room(String roomId, String hostId) {
        this.roomId = roomId;
        this.hostId = hostId;
        this.players = new HashMap<>();
        this.handlers = new HashMap<>();
        this.playing = false;
        this.full = false;
        this.status = STATUS_WAITING;
    }
    
    // 플레이어 추가
    public synchronized boolean addPlayer(String playerId, String playerName, ClientHandler handler) {
        if (players.size() >= MAX_PLAYERS) {
            return false;
        }
        
        PlayerInfo playerInfo = new PlayerInfo(playerId, playerName);
        playerInfo.setRole(RoomActionMessage.ROLE_NONE);
        playerInfo.setReady(false);
        
        players.put(playerId, playerInfo);
        handlers.put(playerId, handler);
        
        if (players.size() >= MAX_PLAYERS) {
            full = true;
        }
        
        System.out.println("방 " + roomId + "에 플레이어 추가: " + playerName + " (" + players.size() + "/" + MAX_PLAYERS + ")");
        
        return true;
    }
    
    // 플레이어 제거
    public synchronized void removePlayer(String playerId) {
        PlayerInfo removed = players.remove(playerId);
        handlers.remove(playerId);
        
        if (removed != null) {
            System.out.println("방 " + roomId + "에서 플레이어 제거: " + removed.getPlayerName());
        }
        
        // 방장이 나갔고 방에 다른 플레이어가 남아있으면 방장 위임
        if (playerId.equals(hostId) && !players.isEmpty()) {
            String newHostId = players.keySet().iterator().next();
            PlayerInfo newHost = players.get(newHostId);
            this.hostId = newHostId;
            
            System.out.println("방장 위임: " + newHost.getPlayerName() + " (ID: " + newHostId + ")");
        }
        
        if (players.size() < MAX_PLAYERS) {
            full = false;
        }
    }
    
    // 역할 설정
    public synchronized void setPlayerRole(String playerId, int role) {
        PlayerInfo player = players.get(playerId);
        if (player != null) {
            player.setRole(role);
        }
    }
    
    // 역할 가져오기
    public int getPlayerRole(String playerId) {
        PlayerInfo player = players.get(playerId);
        return (player != null) ? player.getRole() : RoomActionMessage.ROLE_NONE;
    }
    
    // 역할이 이미 선택되었는지 확인
    public synchronized boolean isRoleTaken(int role, String requestingPlayerId) {
        for (Map.Entry<String, PlayerInfo> entry : players.entrySet()) {
            String playerId = entry.getKey();
            PlayerInfo player = entry.getValue();
            
            if (!playerId.equals(requestingPlayerId) && player.getRole() == role) {
                return true;
            }
        }
        return false;
    }
    
    // 준비 상태 설정
    public synchronized void setPlayerReady(String playerId, boolean ready) {
        PlayerInfo player = players.get(playerId);
        if (player != null) {
            player.setReady(ready);
        }
    }
    
    // 모든 플레이어가 준비되었는지
    public boolean allPlayersReady() {
        if (players.size() < MAX_PLAYERS) {
            return false;
        }
        
        for (PlayerInfo player : players.values()) {
            if (!player.isReady()) {
                return false;
            }
        }
        return true;
    }
    
    // 모든 플레이어가 역할을 선택했는지
    public boolean allPlayersHaveRole() {
        for (PlayerInfo player : players.values()) {
            if (player.getRole() == RoomActionMessage.ROLE_NONE) {
                return false;
            }
        }
        return true;
    }
    
    // 두 플레이어가 같은 역할인지 확인
    public boolean hasSameRoles() {
    	Set<Integer> selectedRoles = new HashSet<>();
        
        for (PlayerInfo player : players.values()) {
            int role = player.getRole();
            
            // 역할이 NONE인 경우는 중복 검사에서 제외
            if (role != RoomActionMessage.ROLE_NONE) {
                if (selectedRoles.contains(role)) {
                    return true; 
                }
                selectedRoles.add(role);
            }
        }
        // 중복이 없음
        return false;
    }
    
    // 방 정보 브로드캐스트
    public void broadcastRoomInfo(int status) {
        RoomInfoMessage msg = new RoomInfoMessage(status, roomId);
        msg.setHostId(hostId);
        msg.setPlayers(new ArrayList<>(players.values()));
        msg.setCanStart(canStartGame());
        msg.setStatus(this.status);
        
        Packet packet = new Packet(MessageType.ROOM_INFO, msg);
        
        for (ClientHandler handler : handlers.values()) {
            handler.sendPacket(packet);
        }
    }
    
    // 같은 방의 모든 플레이어에게 패킷 전송
    public void broadcastPacket(Packet packet) {
        for (ClientHandler handler : handlers.values()) {
            handler.sendPacket(packet);
        }
    }
    
    public void broadcast(MessageType type, Message msg) {
        Packet packet = new Packet(type, msg);
        broadcastPacket(packet);
    }
    
    // 게임 시작 가능 여부
    public boolean canStartGame() {
        return players.size() == MAX_PLAYERS && 
               allPlayersHaveRole() && 
               !hasSameRoles() && 
               allPlayersReady();
    }
    
    // 게임 세션 시작
    public void startGame() {
        System.out.println("========================================");
        System.out.println("🎮 Room.startGame() CALLED");
        System.out.println("   Room ID: " + roomId);
        System.out.println("   Players: " + players.size());
        System.out.println("========================================");
        
        this.playing = true;
        this.status = STATUS_PLAYING;
        System.out.println("✅ Room " + roomId + " set to Playing (Mode: " + gameMode.getDisplayName() + ")");
        
        broadcastRoomInfo(STATUS_PLAYING);
        System.out.println("✅ RoomInfo broadcasted");
        
        // GameMode를 GameSession에 전달
        this.gameSession = new GameSession(this, gameMode);
        System.out.println("✅ GameSession created");
        
        this.gameSession.startGame();
        System.out.println("✅ GameSession.startGame() called");
        System.out.println("========================================");
    }
    
    /**
     * ⭐ 새 게임을 위한 방 상태 초기화
     * 게임 종료 후 같은 방에서 다시 게임을 시작할 수 있도록 함
     */
    public synchronized void resetForNewGame() {
        System.out.println("========================================");
        System.out.println("🔄 Room.resetForNewGame() CALLED");
        System.out.println("   Room ID: " + roomId);
        System.out.println("========================================");
        
        // 기존 게임 세션 정리
        if (gameSession != null) {
            gameSession.stopGame();
            gameSession = null;
        }
        
        // 방 상태 초기화
        this.playing = false;
        this.status = STATUS_WAITING;
        
        // 모든 플레이어의 준비 상태 초기화 (역할은 유지)
        for (PlayerInfo player : players.values()) {
            player.setReady(false);
            System.out.println("   - " + player.getPlayerName() + " ready 상태 초기화");
        }
        
        System.out.println("✅ Room reset complete - status: WAITING");
        System.out.println("========================================");
        
        // 클라이언트에 업데이트된 방 정보 전송
        broadcastRoomInfo(STATUS_WAITING);
    }
    
    // Getters and Setters
    
    public GameSession getGameSession() {
        return gameSession;
    }
    
    public int getStatus() {
        return status;
    }
    
    public void setStatus(int status) {
        this.status = status;
    }
    
    public String getRoomId() {
        return roomId;
    }
    
    public String getHostId() {
        return hostId;
    }

    public Map<String, PlayerInfo> getPlayers() {
		return players;
	}
    
    public int getPlayerCount() {
        return players.size();
    }
    
    public PlayerInfo getPlayerInfo(String playerId) {
        return players.get(playerId);
    }
    
    public boolean isFull() {
        return full;
    }
    
    public boolean isPlaying() {
        return playing;
    }
    
    public boolean isEmpty() {
        return players.isEmpty();
    }
    
    public boolean hasPlayer(String playerId) {
        return players.containsKey(playerId);
    }
    
    public Collection<ClientHandler> getHandlers() {
        return handlers.values();
    }
    
    // 게임 모드 설정
    public void setGameMode(GameModeType gameMode) {
        if (gameMode != null) {
            this.gameMode = gameMode;
            System.out.println("방 " + roomId + " 게임 모드 설정: " + gameMode.getDisplayName());
        }
    }
    
    public GameModeType getGameMode() {
        return gameMode;
    }
}