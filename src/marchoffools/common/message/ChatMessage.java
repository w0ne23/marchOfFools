package marchoffools.common.message;

import marchoffools.common.protocol.Message;

public class ChatMessage extends Message {
    private static final long serialVersionUID = 1L;
    
    private String senderName;    // 발신자 이름
    private String content;       // 메시지 내용
    private String roomId;        // 방 ID
    
    public ChatMessage() {
        super();
    }
    
    public ChatMessage(String playerId, String senderName, String content) {
        super(playerId);
        this.senderName = senderName;
        this.content = content;
    }
    
    public ChatMessage(String playerId, String senderName, String content, String roomId) {
        super(playerId);
        this.senderName = senderName;
        this.content = content;
        this.roomId = roomId;
    }
    
    // Getters and Setters
    public String getSenderName() {
        return senderName;
    }
    
    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public String getRoomId() {
        return roomId;
    }
    
    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }
    
    @Override
    public String toString() {
        return "ChatMessage{sender=" + senderName + 
               ", content='" + content + "'" +
               ", roomId=" + roomId + "}";
    }
}