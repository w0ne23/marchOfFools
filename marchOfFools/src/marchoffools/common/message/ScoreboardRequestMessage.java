package marchoffools.common.message;

import marchoffools.common.protocol.Message;

public class ScoreboardRequestMessage extends Message {
    private static final long serialVersionUID = 1L;
    
    public enum ScoreboardType {
        PERSONAL_BEST,  // 개인 최고 기록
        GLOBAL_TOP      // 전체 랭킹
    }
    
    private ScoreboardType type;
    private int limit; // 가져올 개수
    
    public ScoreboardRequestMessage() {
        super();
        this.limit = 10;
    }
    
    public ScoreboardRequestMessage(String playerId, ScoreboardType type, int limit) {
        super(playerId);
        this.type = type;
        this.limit = limit;
    }
    
    public ScoreboardType getType() {
        return type;
    }
    
    public void setType(ScoreboardType type) {
        this.type = type;
    }
    
    public int getLimit() {
        return limit;
    }
    
    public void setLimit(int limit) {
        this.limit = limit;
    }
}