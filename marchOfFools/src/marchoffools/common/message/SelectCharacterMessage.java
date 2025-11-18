package marchoffools.common.message;

import marchoffools.common.protocol.Message;

public class SelectCharacterMessage extends Message {
    private static final long serialVersionUID = 1L;
    
    public enum CharacterType {
        KNIGHT,  // 기사
        HORSE    // 기마
    }
    
    private CharacterType characterType;
    
    public SelectCharacterMessage() {
        super();
    }
    
    public SelectCharacterMessage(String playerId, CharacterType characterType) {
        super(playerId);
        this.characterType = characterType;
    }
    
    public CharacterType getCharacterType() {
        return characterType;
    }
    
    public void setCharacterType(CharacterType characterType) {
        this.characterType = characterType;
    }
}