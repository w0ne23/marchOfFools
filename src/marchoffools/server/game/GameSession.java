package marchoffools.server.game;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import marchoffools.common.message.GameStateMessage;
import marchoffools.common.protocol.MessageType;

public class GameSession {

    private String roomId;
    private int playTime = 0;
    private boolean isGameRunning = false;
    
    private ScheduledExecutorService gameLoop;
    private Room room;

    public GameSession(Room room) {
        this.room = room;
        this.roomId = room.getRoomId();
    }

    public void startGame() {
        isGameRunning = true;
        playTime = 0;
        
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
        	playTime++;

            // 2. 시간 정보 전송
            broadcastGameState();

        } catch (Exception e) {
            e.printStackTrace();
            stopGame();
        }
    }

    private void broadcastGameState() {
        
        GameStateMessage msg = new GameStateMessage(roomId, playTime);
        
        // Room에 있는 broadcast 메서드 활용
        room.broadcast(MessageType.GAME_STATE, msg); 
    }

    private void finishGame() {
        System.out.println("[GameSession] Time Over: " + roomId);
        isGameRunning = false;
        stopGame();
        
        // Room 클래스의 상태 변경 및 알림 메서드 호출
        room.setStatus(Room.STATUS_FINISHED);
        room.broadcastRoomInfo(Room.STATUS_FINISHED);
        
        // TODO: 여기서 최종 playTime과 파괴한 장애물 수를 가지고 랭킹 처리
    }

    public void stopGame() {
        if (gameLoop != null && !gameLoop.isShutdown()) {
            gameLoop.shutdown();
        }
    }
    
    // 현재까지 생존한 시간을 반환
    public int getPlayTime() {
        return playTime;
    }
}