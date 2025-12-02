package marchoffools.server.game;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import marchoffools.common.message.GameStateMessage;
import marchoffools.common.protocol.MessageType;
import marchoffools.server.network.ClientHandler;

public class GameSession {

    private String roomId;
    private int remainingTime = 180; // 기본 3분
    private boolean isGameRunning = false;
    
    private ScheduledExecutorService gameLoop;
    private Room room;

    public GameSession(Room room) {
        this.room = room;
        this.roomId = room.getRoomId();
    }

    public void startGame() {
        isGameRunning = true;
        // 1초마다 실행
        gameLoop = Executors.newSingleThreadScheduledExecutor();
        gameLoop.scheduleAtFixedRate(this::gameTick, 0, 1, TimeUnit.SECONDS);
        
        System.out.println("[GameSession] Timer Started: " + roomId);
    }

    private void gameTick() {
        if (!isGameRunning) {
            stopGame();
            return;
        }

        try {
            // 1. 시간 감소
            remainingTime--;

            // 2. 시간 정보 전송 (GameStateMessage 활용)
            broadcastGameState();

            // 3. 종료 체크
            if (remainingTime <= 0) {
                finishGame();
            }
        } catch (Exception e) {
            e.printStackTrace();
            stopGame();
        }
    }

    private void broadcastGameState() {
        // 기존 GameStateMessage 생성자 활용 (roomId, remainingTime)
        GameStateMessage msg = new GameStateMessage(roomId, remainingTime);
        
        // Room에 있는 broadcast 메서드 활용
        room.broadcast(MessageType.GAME_STATE, msg); 
    }

    private void finishGame() {
        System.out.println("[GameSession] Time Over: " + roomId);
        isGameRunning = false;
        stopGame();
        
        // [수정] 게임 종료 알림도 RoomInfoMessage 활용
        // Room 클래스의 상태 변경 및 알림 메서드 호출
        room.setStatus(Room.STATUS_FINISHED);
        room.broadcastRoomInfo(Room.STATUS_FINISHED);
    }

    public void stopGame() {
        if (gameLoop != null && !gameLoop.isShutdown()) {
            gameLoop.shutdown();
        }
    }
}