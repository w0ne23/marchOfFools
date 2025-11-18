package marchoffools.common.message;

import marchoffools.common.protocol.Message;

public class QuickMatchMessage extends Message {
    private static final long serialVersionUID = 1L;
    
    public QuickMatchMessage() {
        super();
    }
    
    public QuickMatchMessage(String playerId) {
        super(playerId);
    }
}