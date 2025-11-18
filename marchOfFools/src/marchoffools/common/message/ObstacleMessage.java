package marchoffools.common.message;

import marchoffools.common.protocol.Message;

public class ObstacleMessage extends Message {
    private static final long serialVersionUID = 1L;
    
    public enum Action {
        SPAWN,
        DESTROY,
        HIT
    }
    
    private Action action;
    private String obstacleId;
    private double x;
    private double y;
    
    public ObstacleMessage() {
        super();
    }
    
    public ObstacleMessage(String playerId, Action action, String obstacleId) {
        super(playerId);
        this.action = action;
        this.obstacleId = obstacleId;
    }
    
    // Getters and Setters
    public Action getAction() {
        return action;
    }
    
    public void setAction(Action action) {
        this.action = action;
    }
    
    public String getObstacleId() {
        return obstacleId;
    }
    
    public void setObstacleId(String obstacleId) {
        this.obstacleId = obstacleId;
    }
    
    public double getX() {
        return x;
    }
    
    public void setX(double x) {
        this.x = x;
    }
    
    public double getY() {
        return y;
    }
    
    public void setY(double y) {
        this.y = y;
    }
}