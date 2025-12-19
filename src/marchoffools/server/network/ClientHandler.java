package marchoffools.server.network;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;

import marchoffools.common.protocol.MessageType;
import marchoffools.common.protocol.Packet;
import marchoffools.server.game.GameSession;
import marchoffools.server.game.Room;
import marchoffools.server.game.RoomManager;
import marchoffools.server.ui.ServerLogger;
import marchoffools.common.message.*; 

public class ClientHandler extends Thread {
    
    private static final ServerLogger logger = ServerLogger.getInstance();
    
    private Socket socket;
    private GameServer server;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    
    private String playerId;
    private String playerName;
    private String currentRoomId;
    
    public ClientHandler(Socket socket, GameServer server) {
        this.socket = socket;
        this.server = server;
    }
    
    @Override
    public void run() {
        try {
            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(socket.getInputStream());
            
            logger.info("ClientHandler 스트림 초기화 완료");
            
            // 메시지 수신 루프
            receiveMessages();
            
        } catch (IOException e) {
            logger.warn("클라이언트 연결 종료: " + playerName + " (원인: " + e.getMessage() + ")");
        } finally {
            cleanup();
        }
    }
    
    private void receiveMessages() {
        try {
            while (true) {
                Packet packet = (Packet) in.readObject();
                
                logger.debug("Received packet: " + packet.getType() + " from " + playerName);
                
                handlePacket(packet);
            }
        } catch (IOException | ClassNotFoundException e) {
            logger.info("Connection closed: " + playerName + " (" + e.getMessage() + ")");
        }
    }
    
    private void handlePacket(Packet packet) {
        MessageType type = packet.getType();
        
        switch (type) {
            case ROOM_ACTION:
                handleRoomAction((RoomActionMessage) packet.getData());
                break;
                
            case CHAT:
                handleChat((ChatMessage) packet.getData());
                break;
                
            case GAME_INPUT:
                handleGameInput((GameInputMessage) packet.getData());
                break;
                
            default:
                logger.warn("알 수 없는 메시지 타입: " + type);
        }
    }
    
    private void handleRoomAction(RoomActionMessage msg) {
        int action = msg.getAction();
        
        switch (action) {
            case RoomActionMessage.CONNECT:
                handleConnect(msg);
                break;
                
            case RoomActionMessage.DISCONNECT:
                handleDisconnect(msg);
                break;
                
            case RoomActionMessage.CREATE_ROOM:
                handleCreateRoom(msg);
                break;
                
            case RoomActionMessage.JOIN_ROOM:
                handleJoinRoom(msg);
                break;
                
            case RoomActionMessage.QUICK_MATCH: 
                handleQuickMatch(msg);
                break;
                
            case RoomActionMessage.LEAVE_ROOM:  
                handleLeaveRoom(msg);
                break;
                
            case RoomActionMessage.LIST_ROOMS:
                handleListRooms(msg);
                break;
                
            case RoomActionMessage.CANCEL_MATCH:  
                handleCancelMatch(msg);
                break;
              
            case RoomActionMessage.SELECT_CHARACTER:
                handleSelectCharacter(msg);
                break;
                
            case RoomActionMessage.PLAYER_READY:
                handlePlayerReady(msg);
                break;
                
            case RoomActionMessage.START_GAME:
                handleStartGame(msg);
                break;
                
            case RoomActionMessage.BACK_TO_LOBBY:
                handleBackToLobby(msg);
                break;
                
            default:
                sendResponse(ResponseMessage.error(
                    ResponseMessage.INVALID_INPUT,
                    "지원하지 않는 액션입니다: " + action
                ));
        }
    }
    
    private void handleConnect(RoomActionMessage msg) {
        if (this.playerId != null) {
            sendResponse(ResponseMessage.error(
                ResponseMessage.ALREADY_CONNECTED,
                "이미 서버에 연결되어 있습니다"
            ));
            return;
        }
        
        this.playerId = msg.getPlayerId();
        this.playerName = msg.getPlayerName();
        
        String clientIp = socket.getInetAddress().getHostAddress();
        logger.info("플레이어 연결: " + playerName + " (ID: " + playerId + ", IP: " + clientIp + ")");
        
        // 서버에 이벤트 알림
        server.notifyClientConnected(playerId, playerName, clientIp);
        
        sendResponse(ResponseMessage.success("서버 연결 성공", playerName));
    }
    
    private void handleDisconnect(RoomActionMessage msg) {
        logger.info("플레이어 연결 종료 요청: " + playerName);
        
        sendResponse(ResponseMessage.success("연결을 종료합니다"));
        cleanup();
    }
    
    private void handleCreateRoom(RoomActionMessage msg) {
        if (currentRoomId != null) {
            sendResponse(ResponseMessage.error(
                ResponseMessage.ALREADY_IN_ROOM,
                "이미 방에 입장해 있습니다"
            ));
            return;
        }
        
        try {
            RoomManager roomManager = server.getRoomManager();
            
            Room room = roomManager.createRoom(playerId, playerName);
            currentRoomId = room.getRoomId();
            
            room.addPlayer(playerId, playerName, this);
            
            logger.info("방 생성: " + room.getRoomId() + " (방장: " + playerName + ")");
            
            // 서버에 이벤트 알림
            server.notifyRoomCreated(room.getRoomId(), playerName);
            
            sendResponse(ResponseMessage.success("방 생성 성공", room.getRoomId()));
            room.broadcastRoomInfo(RoomInfoMessage.ROOM_CREATED);
            
        } catch (Exception e) {
            logger.error("방 생성 중 오류: " + e.getMessage());
            sendResponse(ResponseMessage.serverError("방 생성 중 오류 발생: " + e.getMessage()));
        }
    }
    
    private void handleJoinRoom(RoomActionMessage msg) {
        String roomId = msg.getRoomId();
        
        if (roomId == null || roomId.trim().isEmpty()) {
            sendResponse(ResponseMessage.error(
                ResponseMessage.ROOM_NOT_FOUND,
                "방 ID를 입력해주세요"
            ));
            return;
        }
        
        if (currentRoomId != null) {
            sendResponse(ResponseMessage.error(
                ResponseMessage.ALREADY_IN_ROOM,
                "이미 다른 방에 입장해 있습니다"
            ));
            return;
        }
        
        RoomManager roomManager = server.getRoomManager();
        Room room = roomManager.getRoom(roomId);
        
        if (room == null) {
            sendResponse(ResponseMessage.error(
                ResponseMessage.ROOM_NOT_FOUND,
                "방을 찾을 수 없습니다: " + roomId
            ));
            return;
        }
        
        if (room.isFull()) {
            sendResponse(ResponseMessage.error(
                ResponseMessage.ROOM_FULL,
                "방이 가득 찼습니다 (2/2)"
            ));
            return;
        }
        
        if (room.isPlaying()) {
            sendResponse(ResponseMessage.error(
                ResponseMessage.GAME_ALREADY_STARTED,
                "게임이 이미 시작되었습니다"
            ));
            return;
        }
        
        if (room.addPlayer(playerId, playerName, this)) {
            currentRoomId = roomId;
            
            logger.info("방 입장: " + playerName + " → " + roomId);
            
            // 서버에 이벤트 알림
            server.notifyRoomUpdated(room);
            
            sendResponse(ResponseMessage.success("방 입장 성공", roomId));
            room.broadcastRoomInfo(RoomInfoMessage.PLAYER_JOINED);
        } else {
            sendResponse(ResponseMessage.error(
                ResponseMessage.ROOM_FULL,
                "방 입장 실패"
            ));
        }
    }

    private void handleQuickMatch(RoomActionMessage msg) {
        if (currentRoomId != null) {
            sendResponse(ResponseMessage.error(
                ResponseMessage.ALREADY_IN_ROOM,
                "이미 방에 입장해 있습니다"
            ));
            return;
        }
        
        RoomManager roomManager = server.getRoomManager();
        Room room = roomManager.findAvailableRoom();
        
        if (room == null) {
            logger.info("빠른 매칭: 새 방 생성 - " + playerName);
            handleCreateRoom(msg);
            return;
        }
        
        if (room.addPlayer(playerId, playerName, this)) {
            currentRoomId = room.getRoomId();
            
            logger.info("빠른 매칭 성공: " + playerName + " → " + room.getRoomId());
            
            server.notifyRoomUpdated(room);
            
            sendResponse(ResponseMessage.success("매칭 성공", room.getRoomId()));
            room.broadcastRoomInfo(RoomInfoMessage.PLAYER_JOINED);
        } else {
            sendResponse(ResponseMessage.error(
                ResponseMessage.NO_AVAILABLE_ROOM,
                "매칭 실패"
            ));
        }
    }

    private void handleLeaveRoom(RoomActionMessage msg) {
        if (currentRoomId == null) {
            sendResponse(ResponseMessage.error(ResponseMessage.NOT_IN_ROOM, "방에 입장해 있지 않습니다"));
            return;
        }
        
        RoomManager roomManager = server.getRoomManager();
        Room room = roomManager.getRoom(currentRoomId);
        
        if (room == null) {
            currentRoomId = null;
            sendResponse(ResponseMessage.error(ResponseMessage.ROOM_NOT_FOUND, "방을 찾을 수 없습니다"));
            return;
        }
        
        room.removePlayer(playerId);
        String roomId = currentRoomId;
        currentRoomId = null;
        
        logger.info("방 나가기: " + playerName + " ← " + roomId);
        sendResponse(ResponseMessage.success("방 나가기 성공"));
        
        if (room.isEmpty()) {
            roomManager.removeRoom(roomId);
            server.notifyRoomRemoved(roomId);
        } else {
            if (room.isPlaying() || room.getStatus() == Room.STATUS_FINISHED) {
                room.resetForNewGame();
                logger.info("남은 플레이어를 위해 방 상태 초기화 (WAITING)");
            }
            server.notifyRoomUpdated(room);
            room.broadcastRoomInfo(RoomInfoMessage.PLAYER_LEFT);
        }
    }
    
    private void handleListRooms(RoomActionMessage msg) {
        logger.debug("방 목록 요청: " + playerName);
        
        RoomManager roomManager = server.getRoomManager();
        List<RoomListMessage.RoomSummary> roomList = roomManager.getRoomList();
        
        RoomListMessage response = new RoomListMessage(roomList);
        Packet packet = new Packet(MessageType.ROOM_LIST, response);
        sendPacket(packet);
        
        logger.debug("방 목록 전송: " + roomList.size() + "개");
    }
    
    private void handleCancelMatch(RoomActionMessage msg) {
        handleLeaveRoom(msg);
    }
    
    private void handleChat(ChatMessage msg) {
        if (currentRoomId == null) {
            sendResponse(ResponseMessage.error(
                ResponseMessage.NOT_IN_ROOM,
                "Not in a room"
            ));
            return;
        }
        
        RoomManager roomManager = server.getRoomManager();
        Room room = roomManager.getRoom(currentRoomId);
        
        if (room == null) {
            sendResponse(ResponseMessage.error(
                ResponseMessage.ROOM_NOT_FOUND,
                "Room not found"
            ));
            return;
        }
        
        // 채팅 로그 (방별)
        logger.chat(currentRoomId, msg.getSenderName(), msg.getContent());
        
        Packet packet = new Packet(MessageType.CHAT, msg);
        room.broadcastPacket(packet);
    }
    
    private void handleSelectCharacter(RoomActionMessage msg) {
        if (currentRoomId == null) {
            sendResponse(ResponseMessage.error(
                ResponseMessage.NOT_IN_ROOM,
                "방에 입장해 있지 않습니다"
            ));
            return;
        }
        
        RoomManager roomManager = server.getRoomManager();
        Room room = roomManager.getRoom(currentRoomId);
        
        if (room == null) {
            sendResponse(ResponseMessage.error(
                ResponseMessage.ROOM_NOT_FOUND,
                "방을 찾을 수 없습니다"
            ));
            return;
        }

        int requestedRole = msg.getRoleType();
        if (requestedRole != RoomActionMessage.ROLE_NONE && room.isRoleTaken(requestedRole, playerId)) {
            sendResponse(ResponseMessage.error(
                ResponseMessage.ROLE_ALREADY_TAKEN, 
                "이미 선택된 역할입니다"
            ));
            return;
        }

        room.setPlayerRole(playerId, requestedRole);
        String roleName = getRoleName(requestedRole);
        logger.info("역할 선택: " + playerName + " → " + roleName);
        logger.game(currentRoomId, "역할 선택: " + playerName + " → " + roleName);
        
        server.notifyRoomUpdated(room);
        room.broadcastRoomInfo(RoomInfoMessage.ROLE_CHANGED);
    }
    
    private void handlePlayerReady(RoomActionMessage msg) {
        if (currentRoomId == null) return;
        
        RoomManager roomManager = server.getRoomManager();
        Room room = roomManager.getRoom(currentRoomId);
        
        if (room == null) return;

        room.setPlayerReady(playerId, msg.isReady());
        String readyStatus = msg.isReady() ? "준비 완료" : "준비 해제";
        logger.info("준비 상태: " + playerName + " → " + readyStatus);
        logger.game(currentRoomId, "준비 상태: " + playerName + " → " + readyStatus);
        
        server.notifyRoomUpdated(room);
        room.broadcastRoomInfo(RoomInfoMessage.READY_CHANGED);
    }
    
    private void handleStartGame(RoomActionMessage msg) {
        logger.info("게임 시작 요청: " + playerName);
        
        if (currentRoomId == null) {
            sendResponse(ResponseMessage.error(
                ResponseMessage.NOT_IN_ROOM,
                "방에 입장해 있지 않습니다"
            ));
            return;
        }
        
        RoomManager roomManager = server.getRoomManager();
        Room room = roomManager.getRoom(currentRoomId);
        
        if (room == null) {
            sendResponse(ResponseMessage.error(
                ResponseMessage.ROOM_NOT_FOUND,
                "방을 찾을 수 없습니다"
            ));
            return;
        }
        
        if (!playerId.equals(room.getHostId())) {
            logger.warn("게임 시작 거부: " + playerName + " (방장 아님)");
            sendResponse(ResponseMessage.error(
                ResponseMessage.NOT_HOST,
                "방장만 게임을 시작할 수 있습니다"
            ));
            return;
        }
        
        if (!room.canStartGame()) {
            logger.warn("게임 시작 불가: 플레이어 수 " + room.getPlayerCount());
            sendResponse(ResponseMessage.error(
                ResponseMessage.PLAYERS_NOT_READY,
                "모든 플레이어가 준비되지 않았습니다"
            ));
            return;
        }
        
        logger.info("========================================");
        logger.info("🎮 게임 시작: 방 " + currentRoomId);
        logger.game(currentRoomId, "🎮 게임 시작!");
        
        room.startGame();
        
        server.notifyGameStarted(currentRoomId);
        server.notifyRoomUpdated(room);
        
        room.broadcastRoomInfo(RoomInfoMessage.GAME_STARTING);
        logger.info("========================================");
    }
    
    private void handleBackToLobby(RoomActionMessage msg) {
        logger.info("대기실 복귀 요청: " + playerName);
        
        if (currentRoomId == null) {
            sendResponse(ResponseMessage.error(
                ResponseMessage.NOT_IN_ROOM,
                "방에 입장해 있지 않습니다"
            ));
            return;
        }
        
        RoomManager roomManager = server.getRoomManager();
        Room room = roomManager.getRoom(currentRoomId);
        
        if (room == null) {
            sendResponse(ResponseMessage.error(
                ResponseMessage.ROOM_NOT_FOUND,
                "방을 찾을 수 없습니다"
            ));
            return;
        }
        
        if (room.getStatus() != Room.STATUS_WAITING) {
            room.resetForNewGame();
            logger.info("방 상태 초기화 완료 (WAITING)");
            logger.game(currentRoomId, "대기실로 복귀");
        }
        
        server.notifyRoomUpdated(room);
        room.broadcastRoomInfo(RoomInfoMessage.READY_CHANGED);
        
        sendResponse(ResponseMessage.success("대기실로 복귀했습니다"));
    }
    
    private void handleGameInput(GameInputMessage msg) {
        int inputType = msg.getInputType();
        
        if (currentRoomId == null) {
            return;
        }
        
        RoomManager roomManager = server.getRoomManager();
        Room room = roomManager.getRoom(currentRoomId);
        
        if (room == null) {
            return;
        }
        
        GameSession session = room.getGameSession();
        if (session == null) {
            return;
        }
        
        switch (inputType) {
            case GameInputMessage.JUMP:
                session.getState().startJump();
                logger.game(currentRoomId, "점프: " + playerName);
                break;
                
            case GameInputMessage.SLIDE:
                boolean isSliding = msg.getValue() == 1;
                session.getState().getCharController().setSliding(isSliding);
                logger.game(currentRoomId, "슬라이드 " + (isSliding ? "ON" : "OFF") + ": " + playerName);
                break;
                
            case GameInputMessage.ATTACK:
                int skillId = msg.getValue();
                
                if (!session.getState().canUseSkill(skillId)) {
                    long remainingCooldown = session.getState().getRemainingCooldown(skillId);
                    logger.game(currentRoomId, "스킬 쿨타임: " + getSkillName(skillId) 
                        + " (" + playerName + ") - " + String.format("%.1f", remainingCooldown / 1000.0) + "s");
                    return;
                }
                
                session.getState().useSkill(skillId);
                session.getState().activateSkill(skillId);
                logger.game(currentRoomId, "스킬 사용: " + getSkillName(skillId) + " (" + playerName + ")");
                break;
                
            case GameInputMessage.EMOTION:
                break;
        }
        
        room.broadcast(MessageType.GAME_INPUT, msg);
    }

    private String getSkillName(int skillId) {
        switch (skillId) {
            case 0: return "외침";
            case 1: return "찌르기";
            case 2: return "베기";
            case 5: return "돌진";
            default: return "Unknown(" + skillId + ")";
        }
    }
    
    private String getRoleName(int role) {
        switch (role) {
            case 1: return "기사";
            case 2: return "말";
            default: return "미선택";
        }
    }
    
    public void sendPacket(Packet packet) {
        synchronized (out) { 
            try {
                if (out != null) {
                    out.reset(); 
                    out.writeObject(packet);
                    out.flush();
                }
            } catch (IOException e) {
                logger.error("패킷 전송 실패: " + e.getMessage());
                disconnect();
            }
        }
    }
    
    private void sendResponse(ResponseMessage response) {
        response.setPlayerId("server");
        Packet packet = new Packet(MessageType.RESPONSE, response);
        sendPacket(packet);
    }
    
    public void disconnect() {
        cleanup();
    }
    
    private void cleanup() {
        if (currentRoomId != null) {
            RoomManager roomManager = server.getRoomManager();
            Room room = roomManager.getRoom(currentRoomId);
            
            if (room != null) {
                room.removePlayer(playerId);
                logger.warn("비정상 종료로 방 나가기: " + playerName + " ← " + currentRoomId);
                
                if (room.isEmpty()) {
                    roomManager.removeRoom(currentRoomId);
                    server.notifyRoomRemoved(currentRoomId);
                } else {
                    if (room.isPlaying() || room.getStatus() == Room.STATUS_FINISHED) {
                        room.resetForNewGame();
                    }
                    server.notifyRoomUpdated(room);
                    room.broadcastRoomInfo(RoomInfoMessage.PLAYER_LEFT);
                }
            }
            currentRoomId = null;
        }
        
        // 서버에 연결 종료 알림
        if (playerId != null) {
            server.notifyClientDisconnected(playerId, playerName);
        }
        
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) {
            logger.error("연결 종료 오류: " + e.getMessage());
        }
        
        server.removeClient(this);
    }
    
    // 강제 퇴장 시 방 ID 초기화용
    public void clearCurrentRoom() {
        this.currentRoomId = null;
    }
    
    public String getPlayerId() {
        return playerId;
    }
    
    public String getPlayerName() {
        return playerName;
    }
    
    public String getCurrentRoomId() {
        return currentRoomId;
    }
}