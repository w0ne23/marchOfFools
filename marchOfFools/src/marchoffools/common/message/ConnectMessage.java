package marchoffools.common.message;

import marchoffools.common.protocol.Message;

public class ConnectMessage extends Message {
    private static final long serialVersionUID = 1L;
    
    private String playerName;
    private String version;
    
    public ConnectMessage() {
        super();
    }
    
    public ConnectMessage(String playerId, String playerName) {
        super(playerId);
        this.playerName = playerName;
        this.version = "1.0";
    }
    
    public String getPlayerName() {
        return playerName;
    }
    
    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }
    
    public String getVersion() {
        return version;
    }
    
    public void setVersion(String version) {
        this.version = version;
    }
}