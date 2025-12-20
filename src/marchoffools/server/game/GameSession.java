package marchoffools.server.game;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import marchoffools.common.message.GameResultMessage;
import marchoffools.common.message.GameStateMessage;
import marchoffools.common.model.GameModeType;
import marchoffools.common.model.PlayerInfo;
import marchoffools.common.protocol.MessageType;
import marchoffools.server.ui.ServerLogger;

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

    private int frameCount = 0;
    private boolean isRunning = false;
    
    private long gameStartTime = 0; // 게임 시작 시각 (밀리초)

    private ScheduledExecutorService gameLoop;
    
    private static final ServerLogger logger = ServerLogger.getInstance();
    private String lastStageInfo = "";

    public GameSession(Room room, GameModeType gameModeType) {
        this.room = room;
        this.roomId = room.getRoomId();
        this.state = new GameState(room, gameModeType);
    }
    
    public void startGame() {
        System.out.println("========================================");
        System.out.println("🎮 GameSession.startGame() CALLED");
        System.out.println("   Room ID: " + roomId);
        System.out.println("========================================");
        
        isRunning = true;
        frameCount = 0;
        state.reset();
        gameStartTime = System.currentTimeMillis();
        state.getSpawner().startSpawn();
        
        lastStageInfo = state.getCurrentStageInfo();
        logger.game(roomId, "게임 시작 - 모드: " + room.getGameMode().getDisplayName() + ", " + lastStageInfo);
        
        System.out.println("✅ Game started at: " + gameStartTime);
        
        System.out.println("✅ Creating game loop...");
        gameLoop = Executors.newSingleThreadScheduledExecutor();
        gameLoop.scheduleAtFixedRate(this::update, 0, FRAME_TIME, TimeUnit.MILLISECONDS);
        
        System.out.println("✅ Game loop started!");
        System.out.println("   FPS: " + FPS);
        System.out.println("   Frame time: " + FRAME_TIME + "ms");
        System.out.println("   Mode: " + room.getGameMode().getDisplayName());
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
            return;
        }
        
        String prevStageInfo = state.getCurrentStageInfo();

        // 게임 상태 업데이트
        state.update(FRAME_TIME / 1000.0);

        // 스테이지 변경 감지 및 로깅
        String currentStageInfo = state.getCurrentStageInfo();
        if (!prevStageInfo.equals(currentStageInfo)) {
            logger.game(roomId, "스테이지 변경: " + prevStageInfo + " → " + currentStageInfo);
            lastStageInfo = currentStageInfo;
        }
        
        // 장애물 스폰
        state.getSpawner().trySpawn(state.getTrackController());

        // 상태 브로드캐스트 (30hz)
        if (frameCount % BROADCAST_INTERVAL == 0) {
            broadcastGameState();
        }

        // 게임 종료 조건 체크
        if (state.isGameOver()) {
            finishGame();
            isRunning = false;
            return;
        }

        frameCount++;
    }

    /**
     * 게임 상태를 클라이언트에 브로드캐스트
     */
    private void broadcastGameState() {
        GameStateMessage msg = new GameStateMessage();
        
        TrackController track = state.getTrackController();
        
        // 기본 정보
        msg.setRoomId(roomId);
        msg.setDistance(track.getDistance());
        msg.setScore(track.getScore());
        msg.setPlayTime(state.getPlayTime());
        
        // 캐릭터 상태
        msg.setPlayerY(state.getPlayerY());
        msg.setCharState(state.getCharState());
        msg.setInvincible(state.isInvincible());
        
        // 장애물 리스트
        msg.setObstacles(track.getObstacles());
        
        // 스킬 쿨타임
        long[] cooldowns = state.getAllCooldowns();
        msg.setKnightCooldowns(new long[]{cooldowns[0], cooldowns[1], cooldowns[2]});
        msg.setHorseCooldowns(new long[]{cooldowns[3], cooldowns[4], cooldowns[5]});
        
        // 활성 스킬
        msg.setActiveSkills(state.getActiveSkills());
        
        // 게임 모드 상태 텍스트
        msg.setGameMode(state.getGameModeName());
        msg.setStageInfo(state.getCurrentStageInfo());
        
        room.broadcast(MessageType.GAME_STATE, msg);
        
        // 10초마다 한 번씩만 상태 로그
        if (frameCount % 600 == 0) {
            System.out.println(String.format(
                "📊 [%ds] 거리: %.0fm | 점수: %d | 장애물: %d개 | Y: %.0f | Mode: %s",
                state.getPlayTime(),
                track.getDistance(),
                track.getScore(),
                msg.getObstacles().size(),
                state.getPlayerY(),
                state.getCurrentStageInfo()
            ));
        }
    }

    /**
     * 게임 종료 처리
     */
    private void finishGame() {
        TrackController track = state.getTrackController();
        
        logger.game(roomId, "게임 종료 - 점수: " + track.getScore() + ", 시간: " + state.getPlayTime() + "초");
        
        System.out.println("========================================");
        System.out.println("🏁 게임 종료: " + roomId);
        System.out.println("   최종 점수: " + track.getScore());
        System.out.println("   플레이 시간: " + state.getPlayTime() + "초");
        System.out.println("   모드: " + room.getGameMode().getDisplayName());
        System.out.println("========================================");
        
        isRunning = false;
        stopGame();

        // GameResultMessage 생성 및 전송
        sendGameResult();

        // 방 상태 변경
        room.setStatus(Room.STATUS_FINISHED);
        room.broadcastRoomInfo(Room.STATUS_FINISHED);
    }

    /**
     * 게임 결과 생성 및 전송
     */
    private void sendGameResult() {
        GameResultMessage result = new GameResultMessage();
        
        TrackController track = state.getTrackController();
        
        // 플레이어 정보 설정
        PlayerInfo knightPlayer = state.getKnightPlayer();
        PlayerInfo horsePlayer = state.getHorsePlayer();
        
        if (knightPlayer != null) {
            result.setPlayer1Id(knightPlayer.getPlayerId());
            result.setPlayer1Name(knightPlayer.getPlayerName());
        }
        
        if (horsePlayer != null) {
            result.setPlayer2Id(horsePlayer.getPlayerId());
            result.setPlayer2Name(horsePlayer.getPlayerName());
        }
        
        // 게임 결과 통계
        result.setFinalDistance(track.getDistance());
        result.setTotalScore(track.getScore());
        result.setPlayTime(System.currentTimeMillis() - gameStartTime);
        
        // 장애물 통계
        result.setObstaclesDestroyed(track.getObstaclesDestroyed());
        result.setObstaclesAvoided(track.getObstaclesAvoided());
        
        // 개별 기여도 (기사가 파괴, 말이 회피)
        result.setPlayer1Destroyed(track.getObstaclesDestroyed());
        result.setPlayer2Avoided(track.getObstaclesAvoided());
        
        // 결과 브로드캐스트
        room.broadcast(MessageType.GAME_RESULT, result);
        
        System.out.println("📈 게임 결과 전송 완료");
        System.out.println("   - 파괴한 장애물: " + track.getObstaclesDestroyed());
        System.out.println("   - 회피한 장애물: " + track.getObstaclesAvoided());
    }

    /**
     * 게임 루프 중지
     */
    public void stopGame() {
        if (gameLoop != null && !gameLoop.isShutdown()) {
            gameLoop.shutdown();
            try {
                if (!gameLoop.awaitTermination(1, TimeUnit.SECONDS)) {
                    gameLoop.shutdownNow();
                }
            } catch (InterruptedException e) {
                gameLoop.shutdownNow();
                Thread.currentThread().interrupt();
            }
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