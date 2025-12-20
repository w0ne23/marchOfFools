package marchoffools.common.model;

/**
 * 게임 모드 타입 열거형
 */
public enum GameModeType {
    INFINITE("INFINITE"),
    STAGE("STAGE");
    
    private final String displayName;
    
    GameModeType(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    @Override
    public String toString() {
        return displayName;
    }
}