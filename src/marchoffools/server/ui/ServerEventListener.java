package marchoffools.server.ui;

import marchoffools.server.game.Room;

/**
 * 서버 이벤트 리스너 인터페이스
 * GameServer에서 발생하는 이벤트를 UI에 전달
 */
public interface ServerEventListener {
    
    /**
     * 클라이언트 연결 시
     */
    void onClientConnected(String clientId, String clientName, String ip);
    
    /**
     * 클라이언트 연결 해제 시
     */
    void onClientDisconnected(String clientId, String clientName);
    
    /**
     * 방 생성 시
     */
    void onRoomCreated(String roomId, String hostName);
    
    /**
     * 방 삭제 시
     */
    void onRoomRemoved(String roomId);
    
    /**
     * 방 정보 변경 시 (플레이어 입장/퇴장, 준비 상태 변경 등)
     */
    void onRoomUpdated(Room room);
    
    /**
     * 게임 시작 시
     */
    void onGameStarted(String roomId);
    
    /**
     * 게임 종료 시
     */
    void onGameEnded(String roomId, int score);
    
    /**
     * 채팅 메시지 수신 시
     */
    void onChatMessage(String roomId, String sender, String message);
    
    /**
     * 게임 로그 발생 시
     */
    void onGameLog(String roomId, String log);
    
    /**
     * 서버 로그 발생 시
     */
    void onServerLog(String level, String message);
}