package marchoffools.common.message;

import marchoffools.common.protocol.Message;

public class EmotionMessage extends Message {
    private static final long serialVersionUID = 1L;
    
    public enum EmotionType {
        HAPPY,
        SAD,
        ANGRY,
        SURPRISED,
        THUMBS_UP,
        THUMBS_DOWN
    }
    
    private EmotionType emotionType;
    
    public EmotionMessage() {
        super();
    }
    
    public EmotionMessage(String playerId, EmotionType emotionType) {
        super(playerId);
        this.emotionType = emotionType;
    }
    
    public EmotionType getEmotionType() {
        return emotionType;
    }
    
    public void setEmotionType(EmotionType emotionType) {
        this.emotionType = emotionType;
    }
}