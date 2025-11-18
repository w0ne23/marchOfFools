package marchoffools.common.message;

import marchoffools.common.protocol.Message;

public class PlayerReadyMessage extends Message {
    private static final long serialVersionUID = 1L;
    
    private boolean ready;
    
    public PlayerReadyMessage() {
        super();
    }
    
    public PlayerReadyMessage(String playerId, boolean ready) {
        super(playerId);
        this.ready = ready;
    }
    
    public boolean isReady() {
        return ready;
    }
    
    public void setReady(boolean ready) {
        this.ready = ready;
    }
}