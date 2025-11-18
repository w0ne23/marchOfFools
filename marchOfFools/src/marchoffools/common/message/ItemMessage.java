package marchoffools.common.message;

import marchoffools.common.protocol.Message;

public class ItemMessage extends Message {
    private static final long serialVersionUID = 1L;
    
    public enum ItemType {
        SPEED_BOOST,
        SHIELD,
        POWER_UP
    }
    
    private ItemType itemType;
    
    public ItemMessage() {
        super();
    }
    
    public ItemMessage(String playerId, ItemType itemType) {
        super(playerId);
        this.itemType = itemType;
    }
    
    public ItemType getItemType() {
        return itemType;
    }
    
    public void setItemType(ItemType itemType) {
        this.itemType = itemType;
    }
}