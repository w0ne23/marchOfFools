package marchoffools.common.message;

import marchoffools.common.protocol.Message;

public class GameOverMessage extends Message {
    private static final long serialVersionUID = 1L;
    
    private String reason;
    
    public GameOverMessage() {
        super();
    }
    
    public GameOverMessage(String reason) {
        super();
        this.reason = reason;
    }
    
    public String getReason() {
        return reason;
    }
    
    public void setReason(String reason) {
        this.reason = reason;
    }
}