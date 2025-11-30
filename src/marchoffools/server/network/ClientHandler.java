package marchoffools.server.network;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import marchoffools.common.protocol.MessageType;
import marchoffools.common.protocol.Packet;
import marchoffools.server.game.Room;
import marchoffools.server.game.RoomManager;
import marchoffools.common.message.*; 

public class ClientHandler extends Thread {
    
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
            
            System.out.println("ClientHandler 스트림 초기화 완료");
            
            // 메시지 수신 루프
            receiveMessages();
            
        } catch (IOException e) {
            System.out.println("클라이언트 연결 종료: " + playerName + " (원인: " + e.getMessage() + ")");
            e.printStackTrace(); 
        } finally {
            cleanup();
        }
    }
    
    private void receiveMessages() {
        try {
            while (true) {
                Packet packet = (Packet) in.readObject();
                
                System.out.println("Received packet: " + packet.getType() + " from " + playerName);
                
                handlePacket(packet);
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Connection closed due to error: " + e.getMessage());
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
                System.out.println("알 수 없는 메시지 타입: " + type);
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
                
            case RoomActionMessage.CANCEL_MATCH:  
                handleCancelMatch(msg);
                break;
              
            case RoomActionMessage.SELECT_CHARACTER:  // TODO: Phase 3
            	handleSelectCharacter(msg);
                break;
                
            case RoomActionMessage.PLAYER_READY:  // TODO: Phase 3
            	handlePlayerReady(msg);
                break;
                
            case RoomActionMessage.START_GAME:  // TODO: Phase 4
            	handleStartGame(msg);
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
        
        System.out.println("플레이어 연결: " + playerName + " (ID: " + playerId + ")");
        
        sendResponse(ResponseMessage.success("서버 연결 성공", playerName));
    }
    
    private void handleDisconnect(RoomActionMessage msg) {
        System.out.println("플레이어 연결 종료 요청: " + playerName);
        
        // 연결 종료 요청 시 응답 전송 후 정리
        sendResponse(ResponseMessage.success("연결을 종료합니다"));
        cleanup();
    }
    
    private void handleCreateRoom(RoomActionMessage msg) {
        // 이미 방에 있는지 확인
        if (currentRoomId != null) {
            sendResponse(ResponseMessage.error(
                ResponseMessage.ALREADY_IN_ROOM,
                "이미 방에 입장해 있습니다"
            ));
            return;
        }
        
        try {
            RoomManager roomManager = server.getRoomManager();
            
            // 방 생성
            Room room = roomManager.createRoom(playerId, playerName);
            currentRoomId = room.getRoomId();
            
            // 방에 플레이어 추가
            room.addPlayer(playerId, playerName, this);
            
            System.out.println("방 생성 완료: " + room.getRoomId() + " by " + playerName);
            
            // 성공 응답
            sendResponse(ResponseMessage.success("방 생성 성공", room.getRoomId()));
            
            // 방 정보 전송
            room.broadcastRoomInfo(RoomInfoMessage.ROOM_CREATED);
            
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(ResponseMessage.serverError("방 생성 중 오류 발생: " + e.getMessage()));
        }
    }
    
    private void handleJoinRoom(RoomActionMessage msg) {
        String roomId = msg.getRoomId();
        
        // 방 ID 검증
        if (roomId == null || roomId.trim().isEmpty()) {
            sendResponse(ResponseMessage.error(
                ResponseMessage.ROOM_NOT_FOUND,
                "방 ID를 입력해주세요"
            ));
            return;
        }
        
        // 이미 방에 있는지 확인
        if (currentRoomId != null) {
            sendResponse(ResponseMessage.error(
                ResponseMessage.ALREADY_IN_ROOM,
                "이미 다른 방에 입장해 있습니다"
            ));
            return;
        }
        
        RoomManager roomManager = server.getRoomManager();
        
        // 방 찾기
        Room room = roomManager.getRoom(roomId);
        if (room == null) {
            sendResponse(ResponseMessage.error(
                ResponseMessage.ROOM_NOT_FOUND,
                "방을 찾을 수 없습니다: " + roomId
            ));
            return;
        }
        
     // 방이 가득 찼는지 확인
        if (room.isFull()) {
            sendResponse(ResponseMessage.error(
                ResponseMessage.ROOM_FULL,
                "방이 가득 찼습니다 (2/2)"
            ));
            return;
        }
        
        // 게임이 이미 시작됐는지 확인
        if (room.isPlaying()) {
            sendResponse(ResponseMessage.error(
                ResponseMessage.GAME_ALREADY_STARTED,
                "게임이 이미 시작되었습니다"
            ));
            return;
        }
        
        // 방 입장
        if (room.addPlayer(playerId, playerName, this)) {
            currentRoomId = roomId;
            
            System.out.println("방 입장 완료: " + playerName + " → " + roomId);
            
            // 성공 응답
            sendResponse(ResponseMessage.success("방 입장 성공", roomId));
            
            // 모든 플레이어에게 방 정보 브로드캐스트
            room.broadcastRoomInfo(RoomInfoMessage.PLAYER_JOINED);
        } else {
            sendResponse(ResponseMessage.error(
                ResponseMessage.ROOM_FULL,
                "방 입장 실패"
            ));
        }
    }

    // 빠른 매칭 처리
    private void handleQuickMatch(RoomActionMessage msg) {
        // 이미 방에 있는지 확인
        if (currentRoomId != null) {
            sendResponse(ResponseMessage.error(
                ResponseMessage.ALREADY_IN_ROOM,
                "이미 방에 입장해 있습니다"
            ));
            return;
        }
        
        RoomManager roomManager = server.getRoomManager();
        
        // 사용 가능한 방 찾기
        Room room = roomManager.findAvailableRoom();
        
        if (room == null) {
            // 방이 없으면 새로 생성
            System.out.println("빠른 매칭: 새 방 생성 - " + playerName);
            handleCreateRoom(msg);
            return;
        }
        
        // 방 입장
        if (room.addPlayer(playerId, playerName, this)) {
            currentRoomId = room.getRoomId();
            
            System.out.println("빠른 매칭 완료: " + playerName + " → " + room.getRoomId());
            
            // 성공 응답
            sendResponse(ResponseMessage.success("매칭 성공", room.getRoomId()));
            
            // 모든 플레이어에게 방 정보 브로드캐스트
            room.broadcastRoomInfo(RoomInfoMessage.PLAYER_JOINED);
        } else {
            sendResponse(ResponseMessage.error(
                ResponseMessage.NO_AVAILABLE_ROOM,
                "매칭 실패"
            ));
        }
    }

    // 방 나가기 처리
    private void handleLeaveRoom(RoomActionMessage msg) {
        // 방에 있는지 확인
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
            currentRoomId = null;
            sendResponse(ResponseMessage.error(
                ResponseMessage.ROOM_NOT_FOUND,
                "방을 찾을 수 없습니다"
            ));
            return;
        }
        
        // 방에서 나가기
        room.removePlayer(playerId);
        String roomId = currentRoomId;
        currentRoomId = null;
        
        System.out.println("방 나가기 완료: " + playerName + " ← " + roomId);
        
        // 성공 응답
        sendResponse(ResponseMessage.success("방 나가기 성공"));
        
        // 방이 비었으면 삭제
        if (room.isEmpty()) {
            roomManager.removeRoom(roomId);
        } else {
            // 남은 플레이어들에게 알림
            room.broadcastRoomInfo(RoomInfoMessage.PLAYER_LEFT);
        }
    }
    
    private void handleCancelMatch(RoomActionMessage msg) {
        // 빠른 매칭 중 취소는 방 나가기와 동일
        handleLeaveRoom(msg);
    }
    
    private void handleChat(ChatMessage msg) {
        System.out.println("채팅: " + msg.getSenderName() + ": " + msg.getContent());
        
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
        
        Packet packet = new Packet(MessageType.CHAT, msg);
        room.broadcastPacket(packet);
    }
    
    private void handleSelectCharacter(RoomActionMessage msg) {
        // 1. 방 정보 가져오기
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

        // 2. 역할 중복 체크 
        int requestedRole = msg.getRoleType();
        if (room.isRoleTaken(requestedRole, playerId)) {
            sendResponse(ResponseMessage.error(
                ResponseMessage.ROLE_ALREADY_TAKEN, 
                "이미 선택된 역할입니다"
            ));
            return;
        }

        // 3. 방 객체의 데이터 업데이트 (동기화됨)
        room.setPlayerRole(playerId, requestedRole);
        System.out.println("플레이어 역할 변경: " + playerName + " -> " + requestedRole);

        // 4. 변경된 정보를 방 안의 '모든' 클라이언트에게 전송
        room.broadcastRoomInfo(RoomInfoMessage.ROLE_CHANGED);
    }
    
    private void handlePlayerReady(RoomActionMessage msg) {
        // 1. 현재 방 정보 가져오기
        if (currentRoomId == null) return;
        RoomManager roomManager = server.getRoomManager();
        Room room = roomManager.getRoom(currentRoomId);
        
        if (room == null) return;

        // 2. 해당 플레이어의 준비 상태 업데이트
        room.setPlayerReady(playerId, msg.isReady());
        System.out.println("플레이어 준비 상태 변경: " + playerName + " -> " + msg.isReady());

        // 3. 방 안의 모든 사람에게 최신 상태 브로드캐스트
        room.broadcastRoomInfo(RoomInfoMessage.READY_CHANGED);
    }
    
    private void handleStartGame(RoomActionMessage msg) {
        System.out.println("게임 시작 요청: " + playerName);
        
        // 1. 방에 있는지 확인
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
        
        // 2. 방장인지 확인
        if (!playerId.equals(room.getHostId())) {
            System.out.println("✗ 방장이 아님: " + playerName);
            sendResponse(ResponseMessage.error(
                ResponseMessage.NOT_HOST,
                "방장만 게임을 시작할 수 있습니다"
            ));
            return;
        }
        
        // 3. 게임 시작 가능 여부 확인
        if (!room.canStartGame()) {
            System.out.println("✗ 게임 시작 불가");
            System.out.println("  - 플레이어 수: " + room.getPlayerCount());
            
            sendResponse(ResponseMessage.error(
                ResponseMessage.PLAYERS_NOT_READY,
                "모든 플레이어가 준비되지 않았습니다"
            ));
            return;
        }
        
        // 게임 시작
        System.out.println("게임 시작 조건 충족");
        
        // 4. 방 상태를 PLAYING으로 변경
        room.startGame();  // playing = true, status = "PLAYING"
        System.out.println("✓ 방 상태 변경: WAITING → PLAYING");
        
        // 5. 변경된 RoomInfo를 모든 플레이어에게 브로드캐스트
        room.broadcastRoomInfo(RoomInfoMessage.GAME_STARTING);
        System.out.println("✓ 모든 플레이어에게 게임 시작 신호 전송 완료");
    }
    
    private void handleGameInput(GameInputMessage msg) {
        System.out.println("게임 입력: " + msg.getInputType() + " from " + playerName);
        
        // TODO: Phase 4에서 게임 세션에 전달
    }
    
    public void sendPacket(Packet packet) {
        try {
            if (out != null) {
            	out.reset();
            	
                out.writeObject(packet);
                out.flush();
            }
        } catch (IOException e) {
            System.err.println("패킷 전송 실패: " + e.getMessage());
            // 패킷 전송 실패는 소켓이 닫혔거나 네트워크 문제이므로 연결을 끊습니다.
            disconnect(); 
        }
    }
    
    private void sendResponse(ResponseMessage response) {
        response.setPlayerId("server");
        Packet packet = new Packet(MessageType.RESPONSE, response);
        sendPacket(packet);
    }
    
    private void broadcasting(Packet packet) {
        // GameServer의 clients 목록 접근 시 동기화 유지
        synchronized (server.getClients()) {
            for (ClientHandler client : server.getClients()) {
                if (client != this) {  // 자기 자신 제외
                    client.sendPacket(packet);
                }
            }
        }
    }
    
    public void disconnect() {
        cleanup();
    }
    
    private void cleanup() {
    	// 방에 있었다면 방에서 나가기 처리
    	if (currentRoomId != null) {
            RoomManager roomManager = server.getRoomManager();
            Room room = roomManager.getRoom(currentRoomId);
            
            if (room != null) {
                room.removePlayer(playerId);
                
                System.out.println("비정상 종료로 방 나가기: " + playerName + " ← " + currentRoomId);
                
                // 방이 비었으면 삭제
                if (room.isEmpty()) {
                    roomManager.removeRoom(currentRoomId);
                } else {
                    // 남은 플레이어들에게 알림
                    room.broadcastRoomInfo(RoomInfoMessage.PLAYER_LEFT);
                }
            }
            currentRoomId = null;
        }
    	
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) {
            System.err.println("연결 종료 오류: " + e.getMessage());
        }
        
        server.removeClient(this);
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