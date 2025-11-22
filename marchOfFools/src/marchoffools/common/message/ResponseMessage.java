package marchoffools.common.message;

import marchoffools.common.protocol.Message;

public class ResponseMessage extends Message {
    private static final long serialVersionUID = 1L;
    
    // ==================== 2xx: 성공 ====================
    public static final int SUCCESS = 200;                    // 일반 성공
    
    // ==================== 4xx: 클라이언트 오류 ====================
    public static final int BAD_REQUEST = 400;                // 잘못된 요청 (일반)
    
    // 연결 관련 오류
    public static final int ALREADY_CONNECTED = 411;          // 이미 연결됨
    
    // 방 관련 오류
    public static final int ROOM_NOT_FOUND = 420;             // 방을 찾을 수 없음
    public static final int ROOM_FULL = 421;                  // 방이 가득 찼음 (2명)
    // public static final int CANNOT_LEAVE_PLAYING = 422;    // [추후 필요 시 추가] 게임 중 나갈 수 없음
    
    // 게임 관련 오류
    public static final int GAME_ALREADY_STARTED = 430;       // 게임이 이미 시작됨
    public static final int NOT_ENOUGH_PLAYERS = 431;         // 플레이어 부족 (2명 필요)
    public static final int PLAYERS_NOT_READY = 432;          // 플레이어 준비 안 됨
    
    // 역할 관련 오류 
    public static final int ROLE_NOT_SELECTED = 440;          // 역할 미선택
    public static final int ROLE_ALREADY_TAKEN = 441;          // 두 플레이어가 같은 역할 (시작 불가)
    
    // 입력/액션 관련 오류
    public static final int INVALID_INPUT = 450;              // 잘못된 입력
    public static final int ACTION_NOT_ALLOWED = 451;         // 허용되지 않는 액션
    
    // 매칭 관련 오류
    public static final int NO_AVAILABLE_ROOM = 460;          // 사용 가능한 방 없음 (빠른 매칭)
    public static final int MATCHMAKING_TIMEOUT = 461;        // 매칭 시간 초과
    
    // ==================== 5xx: 서버 오류 ====================
    public static final int INTERNAL_SERVER_ERROR = 500;      // 내부 서버 오류
    public static final int NETWORK_ERROR = 501;              // 네트워크 오류
    
    // ==================== 필드 ====================
    private int code;             // 응답 코드
    private String message;       // 응답 메시지
    private Object data;          // 추가 데이터 (선택)
    
    // ==================== 생성자 ====================
    public ResponseMessage() {
        super();
    }
    
    public ResponseMessage(int code, String message) {
        super();
        this.code = code;
        this.message = message;
    }
    
    public ResponseMessage(int code, String message, Object data) {
        super();
        this.code = code;
        this.message = message;
        this.data = data;
    }
    
    // ==================== 편의 메서드 ====================
    
    // 성공 응답 여부 (2xx)
    public boolean isSuccess() {
        return code >= 200 && code < 300;
    }
    
    // 클라이언트 오류 여부 (4xx)
    public boolean isClientError() {
        return code >= 400 && code < 500;
    }
    
    // 서버 오류 여부 (5xx)
    public boolean isServerError() {
        return code >= 500 && code < 600;
    }
    
    // 오류 여부 (4xx 또는 5xx)
    public boolean isError() {
        return code >= 400;
    }
    
    // 방 관련 오류 여부 (42x)
    public boolean isRoomError() {
        return code >= 420 && code < 430;
    }
    
    // 게임 관련 오류 여부 (43x)
    public boolean isGameError() {
        return code >= 430 && code < 440;
    }
    
    // 역학 관련 오류 여부 (44x)
    public boolean isRoleError() {
        return code >= 440 && code < 450;
    }
    
    // ==================== 정적 팩토리 메서드 ====================
    
    // 성공 응답 생성
    public static ResponseMessage success(String message) {
        return new ResponseMessage(SUCCESS, message);
    }
    
    public static ResponseMessage success(String message, Object data) {
        return new ResponseMessage(SUCCESS, message, data);
    }
    
    // 오류 응답 생성
    public static ResponseMessage error(int code, String message) {
        return new ResponseMessage(code, message);
    }
    
    public static ResponseMessage error(int code, String message, Object data) {
        return new ResponseMessage(code, message, data);
    }
    
    // 서버 오류 응답 생성
    public static ResponseMessage serverError(String message) {
        return new ResponseMessage(INTERNAL_SERVER_ERROR, message);
    }
    
    // 잘못된 요청 응답 생성
    public static ResponseMessage badRequest(String message) {
        return new ResponseMessage(BAD_REQUEST, message);
    }
    
    // ==================== Getters and Setters ====================
    
    public int getCode() {
        return code;
    }
    
    public void setCode(int code) {
        this.code = code;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public Object getData() {
        return data;
    }
    
    public void setData(Object data) {
        this.data = data;
    }
    
    @Override
    public String toString() {
        return "ResponseMessage{code=" + code + 
               ", message='" + message + "'" +
               ", data=" + data + "}";
    }
}