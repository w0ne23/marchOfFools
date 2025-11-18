package marchoffools.common.message;

import marchoffools.common.protocol.Message;

public class ChatMessage extends Message {
    private static final long serialVersionUID = 1L;
    
    private String content;
    private String senderName;
    
    public ChatMessage() {
        super();
    }
    
    public ChatMessage(String playerId, String senderName, String content) {
        super(playerId);
        this.senderName = senderName;
        this.content = content;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public String getSenderName() {
        return senderName;
    }
    
    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }
}