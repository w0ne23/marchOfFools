package marchoffools.server.ui;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 서버 로그 관리 싱글톤
 * 콘솔과 UI 양쪽에 로그 출력
 */
public class ServerLogger {
    
    public enum LogLevel { 
        DEBUG,  // 개발용 상세 로그
        INFO,   // 일반 정보
        WARN,   // 경고
        ERROR,  // 오류
        CHAT,   // 채팅 (방별)
        GAME    // 게임 로그 (방별)
    }
    
    private static ServerLogger instance;
    private List<LogListener> listeners = new CopyOnWriteArrayList<>();
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
    
    // 방별 로그 저장 
    private Map<String, List<String>> roomChatLogs = new ConcurrentHashMap<>();
    private Map<String, List<String>> roomGameLogs = new ConcurrentHashMap<>();
    private static final int MAX_ROOM_LOGS = 100;
    
    // 디버그 모드 
    private boolean debugMode = false;
    
    public interface LogListener {
        void onLog(LogLevel level, String timestamp, String message, String roomId);
    }
    
    private ServerLogger() {}
    
    public static synchronized ServerLogger getInstance() {
        if (instance == null) {
            instance = new ServerLogger();
        }
        return instance;
    }
    
    public void addListener(LogListener listener) {
        listeners.add(listener);
    }
    
    public void removeListener(LogListener listener) {
        listeners.remove(listener);
    }
    
    public void setDebugMode(boolean enabled) {
        this.debugMode = enabled;
        info("디버그 모드: " + (enabled ? "활성화" : "비활성화"));
    }
    
    public boolean isDebugMode() {
        return debugMode;
    }
    
    // ========== 서버 로그 ==========
    
    public void debug(String message) {
        if (debugMode) {
            log(LogLevel.DEBUG, message, null);
        }
    }
    
    public void info(String message) {
        log(LogLevel.INFO, message, null);
    }
    
    public void warn(String message) {
        log(LogLevel.WARN, message, null);
    }
    
    public void error(String message) {
        log(LogLevel.ERROR, message, null);
    }
    
    // ========== 방별 로그 ==========
    
    public void chat(String roomId, String sender, String message) {
        String formatted = "[" + sender + "] " + message;
        log(LogLevel.CHAT, formatted, roomId);
        
        // 방별 채팅 로그 저장
        roomChatLogs.computeIfAbsent(roomId, k -> new CopyOnWriteArrayList<>());
        List<String> logs = roomChatLogs.get(roomId);
        logs.add(getTimestamp() + " " + formatted);
        if (logs.size() > MAX_ROOM_LOGS) {
            logs.remove(0);
        }
    }
    
    public void game(String roomId, String message) {
        log(LogLevel.GAME, message, roomId);
        
        // 방별 게임 로그 저장
        roomGameLogs.computeIfAbsent(roomId, k -> new CopyOnWriteArrayList<>());
        List<String> logs = roomGameLogs.get(roomId);
        logs.add(getTimestamp() + " " + message);
        if (logs.size() > MAX_ROOM_LOGS) {
            logs.remove(0);
        }
    }
    
    // ========== 방 로그 조회 ==========
    
    public List<String> getRoomChatLogs(String roomId) {
        return roomChatLogs.getOrDefault(roomId, List.of());
    }
    
    public List<String> getRoomGameLogs(String roomId) {
        return roomGameLogs.getOrDefault(roomId, List.of());
    }
    
    public void clearRoomLogs(String roomId) {
        roomChatLogs.remove(roomId);
        roomGameLogs.remove(roomId);
    }
    
    // ========== 내부 메서드 ==========
    
    private void log(LogLevel level, String message, String roomId) {
        String timestamp = getTimestamp();
        
        // 콘솔 출력
        String prefix = getLevelPrefix(level);
        if (roomId != null) {
            System.out.println("[" + timestamp + "] " + prefix + " [방:" + roomId + "] " + message);
        } else {
            System.out.println("[" + timestamp + "] " + prefix + " " + message);
        }
        
        // 리스너에게 전달 (UI 업데이트)
        for (LogListener listener : listeners) {
            listener.onLog(level, timestamp, message, roomId);
        }
    }
    
    private String getTimestamp() {
        return LocalDateTime.now().format(formatter);
    }
    
    private String getLevelPrefix(LogLevel level) {
        switch (level) {
            case DEBUG: return "[DEBUG]";
            case INFO:  return "[INFO]";
            case WARN:  return "[WARN]";
            case ERROR: return "[ERROR]";
            case CHAT:  return "[CHAT]";
            case GAME:  return "[GAME]";
            default:    return "[???]";
        }
    }
}