package marchoffools.common.message;

import marchoffools.common.protocol.Message;

public class GameResultMessage extends Message {
    private static final long serialVersionUID = 1L;
    
    private String player1Id;
    private String player1Name;
    private String player2Id;
    private String player2Name;
    
    private double finalDistance;          // 최종 거리
    private int totalScore;                // 총 점수
    private int obstaclesDestroyed;        // 파괴한 방해물 수
    private int obstaclesAvoided;          // 회피한 방해물 수
    private long playTime;                 // 플레이 시간 (ms)
    
    // 개별 기여도
    private int player1Destroyed;          // 플레이어1이 파괴한 수
    private int player2Avoided;            // 플레이어2가 회피한 수
    
    public GameResultMessage() {
        super();
    }
    
    // Getters and Setters
    public String getPlayer1Id() {
        return player1Id;
    }
    
    public void setPlayer1Id(String player1Id) {
        this.player1Id = player1Id;
    }
    
    public String getPlayer1Name() {
        return player1Name;
    }
    
    public void setPlayer1Name(String player1Name) {
        this.player1Name = player1Name;
    }
    
    public String getPlayer2Id() {
        return player2Id;
    }
    
    public void setPlayer2Id(String player2Id) {
        this.player2Id = player2Id;
    }
    
    public String getPlayer2Name() {
        return player2Name;
    }
    
    public void setPlayer2Name(String player2Name) {
        this.player2Name = player2Name;
    }
    
    public double getFinalDistance() {
        return finalDistance;
    }
    
    public void setFinalDistance(double finalDistance) {
        this.finalDistance = finalDistance;
    }
    
    public int getTotalScore() {
        return totalScore;
    }
    
    public void setTotalScore(int totalScore) {
        this.totalScore = totalScore;
    }
    
    public int getObstaclesDestroyed() {
        return obstaclesDestroyed;
    }
    
    public void setObstaclesDestroyed(int obstaclesDestroyed) {
        this.obstaclesDestroyed = obstaclesDestroyed;
    }
    
    public int getObstaclesAvoided() {
        return obstaclesAvoided;
    }
    
    public void setObstaclesAvoided(int obstaclesAvoided) {
        this.obstaclesAvoided = obstaclesAvoided;
    }
    
    public long getPlayTime() {
        return playTime;
    }
    
    public void setPlayTime(long playTime) {
        this.playTime = playTime;
    }
    
    public int getPlayer1Destroyed() {
        return player1Destroyed;
    }
    
    public void setPlayer1Destroyed(int player1Destroyed) {
        this.player1Destroyed = player1Destroyed;
    }
    
    public int getPlayer2Avoided() {
        return player2Avoided;
    }
    
    public void setPlayer2Avoided(int player2Avoided) {
        this.player2Avoided = player2Avoided;
    }
    
    @Override
    public String toString() {
        return "GameResultMessage{" +
               "distance=" + finalDistance +
               ", score=" + totalScore +
               ", destroyed=" + obstaclesDestroyed +
               ", avoided=" + obstaclesAvoided +
               ", playTime=" + playTime + "ms}";
    }
}