package marchoffools.common.protocol;

public enum MessageType {
	// 연결
    CONNECT,
    DISCONNECT,
    HEARTBEAT,
    
    // 방 관련
    CREATE_ROOM,
    JOIN_ROOM,
    QUICK_MATCH,
    LEAVE_ROOM,
    ROOM_INFO,
    ROOM_LIST,
    
    // 대기실
    SELECT_CHARACTER,
    PLAYER_READY,
    CHAT,
    START_GAME,
    
    // 인게임
    PLAYER_INPUT,
    EMOTION,
    GAME_STATE_UPDATE,
    OBSTACLE_UPDATE,
    ITEM_USE,
    GAME_OVER,
    
    // 점수/통계
    GAME_RESULT,
    SCOREBOARD_REQUEST,
    SCOREBOARD_DATA,
    
    // 응답/오류
    SUCCESS,
    ERROR
}
