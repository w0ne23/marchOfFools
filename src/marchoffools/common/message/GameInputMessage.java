package marchoffools.common.message;

import marchoffools.common.protocol.Message;

public class GameInputMessage extends Message {
    private static final long serialVersionUID = 1L;
    
    // 입력 타입 상수
    public static final int INPUT_NONE = 0;
    public static final int JUMP = 1;			// 점프
    public static final int SLIDE = 2;          // 슬라이드
    public static final int ATTACK = 3;         // 공격(기사 스킬)
    public static final int USE_ITEM = 4;       // 아이템 사용
    public static final int EMOTION = 5;        // 감정 표현
    
    // 감정 타입 상수
    public static final int EMOTION_HAPPY = 0;
    public static final int EMOTION_ANGRY = 1;
    public static final int EMOTION_SAD = 2;
    public static final int EMOTION_SLEEP = 3;
    public static final int EMOTION_SURPRISED = 4;
    
    // 아이템 타입 상수
    public static final int ITEM_SPEED_BOOST = 0;
    public static final int ITEM_SHIELD = 1;
    public static final int ITEM_POWER_UP = 2;
    
    private int inputType;        // 입력 타입
    private int value;            // 추가 값 (감정 타입, 아이템 타입 등)
    private boolean pressed;      // 키 눌림/뗌 (이동, 공격용)
    
    public GameInputMessage() {
        super();
    }
    
    public GameInputMessage(String playerId, int inputType) {
        super(playerId);
        this.inputType = inputType;
    }
    
    public GameInputMessage(String playerId, int inputType, int value) {
        super(playerId);
        this.inputType = inputType;
        this.value = value;
    }
    
    // Getters and Setters
    public int getInputType() {
        return inputType;
    }
    
    public void setInputType(int inputType) {
        this.inputType = inputType;
    }
    
    public int getValue() {
        return value;
    }
    
    public void setValue(int value) {
        this.value = value;
    }
    
    public boolean isPressed() {
        return pressed;
    }
    
    public void setPressed(boolean pressed) {
        this.pressed = pressed;
    }
    
    @Override
    public String toString() {
        return "GameInputMessage{inputType=" + inputType + 
               ", value=" + value + 
               ", pressed=" + pressed + 
               ", playerId=" + playerId + "}";
    }
}