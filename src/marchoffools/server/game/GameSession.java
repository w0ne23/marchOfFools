package marchoffools.server.game;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import marchoffools.common.message.GameStateMessage;
import marchoffools.common.protocol.MessageType;

/**
 * 한 판의 게임 진행을 관리하는 클래스
 */
public class GameSession {

    private static final int FPS = 60;
    private static final long FRAME_TIME = 1000 / FPS; // 약 16ms
    private static final int BROADCAST_INTERVAL = 2; // 매 2프레임마다 (30hz)

    private String roomId;
    private Room room;
    private GameState state;
    private ObstacleSpawner spawner;

    private int frameCount = 0;
    private boolean isRunning = false;

    private ScheduledExecutorService gameLoop;

    public GameSession(Room room) {
        this.room = room;
        this.roomId = room.getRoomId();
        this.state = new GameState(room);
        this.spawner = new ObstacleSpawner();
    }

    /**
     * 게임 시작
     */
    public void startGame() {
        isRunning = true;
        frameCount = 0;
        state.reset();

        gameLoop = Executors.newSingleThreadScheduledExecutor();
        gameLoop.scheduleAtFixedRate(this::update, 0, FRAME_TIME, TimeUnit.MILLISECONDS);

        System.out.println("========================================");
        System.out.println("게임 시작: " + roomId);
        System.out.println("========================================");
    }

    /**
     * 매 프레임마다 호출되는 업데이트 메서드
     */
    private void update() {
        if (!isRunning) {
            return;
        }

        if (room == null) {
            finishGame();
            isRunning = false;
        }

        // 게임 상태 업데이트
        state.update(FRAME_TIME / 1000.0);

        // 장애물 생성 시도
        spawner.trySpawn(state.getTrackController());

        // 상태 브로드캐스트 (30hz)
        if (frameCount % BROADCAST_INTERVAL == 0) {
            broadcastGameState();
        }

        // 게임 종료 조건 체크
        if (state.isGameOver()) {
            finishGame();
            isRunning = false;
        }

        frameCount++;
    }

    /**
     * 게임 상태를 클라이언트에 브로드캐스트
     */
    private void broadcastGameState() {
        GameStateMessage msg = new GameStateMessage();
        
        // 기본 정보
        msg.setRoomId(roomId);
        msg.setDistance(state.getDistance());
        msg.setScore(state.getScore());
        msg.setRemainingTime(state.getPlayTime());
        
        // 캐릭터 상태
        msg.setPlayerY(state.getPlayerY());
        msg.setCharState(state.getCharState());
        msg.setInvincible(state.isInvincible());
        
        // 장애물 리스트
        msg.setObstacles(state.getTrackController().getObstacles());
        
        // 스킬 쿨타임
        long[] cooldowns = state.getAllCooldowns();
        msg.setKnightCooldowns(new long[]{cooldowns[0], cooldowns[1], cooldowns[2]});
        msg.setHorseCooldowns(new long[]{cooldowns[3], cooldowns[4], cooldowns[5]});
        
        // 활성 스킬
        msg.setActiveSkills(state.getActiveSkills());
        
        room.broadcast(MessageType.GAME_STATE, msg);
        
        // 10초마다 한 번씩만 상태 로그
        if (frameCount % 600 == 0) { // 60fps * 10초 = 600프레임
            System.out.println(String.format(
                "📊 [%ds] 거리: %.0fm | 점수: %d | 장애물: %d개 | Y: %.0f",
                state.getPlayTime(),
                state.getDistance(),
                state.getScore(),
                msg.getObstacles().size(),
                state.getPlayerY()
            ));
        }
    }

    /**
     * 게임 종료 처리
     */
    private void finishGame() {
        System.out.println("========================================");
        System.out.println("🏁 게임 종료: " + roomId);
        System.out.println("   최종 점수: " + state.getScore());
        System.out.println("   플레이 시간: " + state.getPlayTime() + "초");
        System.out.println("========================================");
        
        isRunning = false;
        stopGame();

        // TODO: GameResultMessage 생성 및 전송

        room.setStatus(Room.STATUS_FINISHED);
        room.broadcastRoomInfo(Room.STATUS_FINISHED);
    }

    /**
     * 게임 루프 중지
     */
    public void stopGame() {
        if (gameLoop != null && !gameLoop.isShutdown()) {
            gameLoop.shutdown();
        }
    }

    /**
     * 현재 게임 진행 시간 반환
     */
    public int getPlayTime() {
        return state.getPlayTime();
    }

    /**
     * 게임 상태 객체 반환
     */
    public GameState getState() {
        return state;
    }
}