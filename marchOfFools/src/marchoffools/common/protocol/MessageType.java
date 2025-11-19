package marchoffools.common.protocol;

public enum MessageType {
    // 클라이언트 → 서버
    ROOM_ACTION,      // 방 관련 모든 액션
    GAME_INPUT,       // 게임 입력 (스킬/아이템/감정)
    
    // 서버 → 클라이언트
    ROOM_INFO,        // 방 정보/상태 업데이트
    GAME_STATE,       // 게임 상태 동기화
    GAME_RESULT,      // 게임 결과
    RESPONSE,         // 응답/에러
    
    // 양방향
    CHAT,             // 채팅
    
    // 연결
    CONNECT,
    DISCONNECT
}