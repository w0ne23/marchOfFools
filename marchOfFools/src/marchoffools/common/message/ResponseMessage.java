package marchoffools.common.message;

import marchoffools.common.protocol.Message;

public class ResponseMessage extends Message {
    private static final long serialVersionUID = 1L;
    
    // 응답 코드 상수
    public static final int SUCCESS = 200;
    public static final int ROOM_CREATED = 201;
    public static final int ROOM_JOINED = 202;
    
    public static final int BAD_REQUEST = 400;
    public static final int ROOM_NOT_FOUND = 404;
    public static final int ROOM_FULL = 405;
    public static final int ALREADY_IN_ROOM = 406;
    public static final int NOT_IN_ROOM = 407;
    public static final int NOT_HOST = 408;
    public static final int GAME_ALREADY_STARTED = 409;
    
    public static final int SERVER_ERROR = 500;
    
    private int code;             // 응답 코드
    private String message;       // 응답 메시지
    private Object data;          // 추가 데이터 (선택)
    
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
    
    // 편의 메서드
    public boolean isSuccess() {
        return code >= 200 && code < 300;
    }
    
    public boolean isError() {
        return code >= 400;
    }
    
    // Getters and Setters
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