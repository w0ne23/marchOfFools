package marchoffools.common.message;

import marchoffools.common.protocol.Message;

public class DisconnectMessage extends Message {
    private static final long serialVersionUID = 1L;
    
    private String reason;
    
    public DisconnectMessage() {
        super();
    }
    
    public DisconnectMessage(String playerId, String reason) {
        super(playerId);
        this.reason = reason;
    }
    
    public String getReason() {
        return reason;
    }
    
    public void setReason(String reason) {
        this.reason = reason;
    }
}