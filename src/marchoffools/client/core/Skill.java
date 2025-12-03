package marchoffools.client.core;

public enum Skill {
	SHIELD("방어막", 1),
	SPECIAL("필살기", 2),
	INVINCIBLE("무적", 3);
    
    private final String displayName;
    private final int id;
    
    Skill(String displayName, int id) {
        this.displayName = displayName;
        this.id = id;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public int getId() {
        return id;
    }
    
    public static Skill getById(int id) {
        for (Skill skill : values()) {
            if (skill.id == id) {
                return skill;
            }
        }
        return null;
    }
}