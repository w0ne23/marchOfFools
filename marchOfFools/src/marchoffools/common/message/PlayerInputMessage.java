package marchoffools.common.message;

import marchoffools.common.protocol.Message;

public class PlayerInputMessage extends Message {
    private static final long serialVersionUID = 1L;
    
    public enum InputType {
        MOVE_LEFT,
        MOVE_RIGHT,
        JUMP,
        ATTACK,      // 기사 스킬
        USE_ITEM     // 부스트 아이템
    }
    
    private InputType inputType;
    private boolean pressed; // 키 눌림/뗌
    
    public PlayerInputMessage() {
        super();
    }
    
    public PlayerInputMessage(String playerId, InputType inputType, boolean pressed) {
        super(playerId);
        this.inputType = inputType;
        this.pressed = pressed;
    }
    
    public InputType getInputType() {
        return inputType;
    }
    
    public void setInputType(InputType inputType) {
        this.inputType = inputType;
    }
    
    public boolean isPressed() {
        return pressed;
    }
    
    public void setPressed(boolean pressed) {
        this.pressed = pressed;
    }
}