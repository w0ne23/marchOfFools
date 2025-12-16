package marchoffools.client.data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 게임 점수 기록 데이터 클래스
 */
public class ScoreRecord implements Serializable, Comparable<ScoreRecord> {
    
    private static final long serialVersionUID = 1L;
    
    private String player1Name;     // 기사 플레이어
    private String player2Name;     // 말 플레이어
    private int score;              // 총 점수
    private int playTime;           // 플레이 시간 (초)
    private int distance;           // 이동 거리 (미터)
    private int monstersDestroyed;  // 처치한 몬스터
    private int obstaclesAvoided;   // 회피한 장애물
    private LocalDateTime dateTime; // 기록 시간
    
    public ScoreRecord(String player1Name, String player2Name, int score, 
                       int playTime, int distance, 
                       int monstersDestroyed, int obstaclesAvoided) {
        this.player1Name = player1Name;
        this.player2Name = player2Name;
        this.score = score;
        this.playTime = playTime;
        this.distance = distance;
        this.monstersDestroyed = monstersDestroyed;
        this.obstaclesAvoided = obstaclesAvoided;
        this.dateTime = LocalDateTime.now();
    }
    
    // 점수 기준 내림차순 정렬
    @Override
    public int compareTo(ScoreRecord other) {
        return Integer.compare(other.score, this.score);
    }
    
    // Getters
    public String getPlayer1Name() {
        return player1Name;
    }
    
    public String getPlayer2Name() {
        return player2Name;
    }
    
    public int getScore() {
        return score;
    }
    
    public int getPlayTime() {
        return playTime;
    }
    
    public int getDistance() {
        return distance;
    }
    
    public int getMonstersDestroyed() {
        return monstersDestroyed;
    }
    
    public int getObstaclesAvoided() {
        return obstaclesAvoided;
    }
    
    public LocalDateTime getDateTime() {
        return dateTime;
    }
    
    /**
     * 날짜를 포맷된 문자열로 반환
     */
    public String getFormattedDate() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        return dateTime.format(formatter);
    }
    
    /**
     * 플레이어 이름 조합 반환
     */
    public String getPlayerNames() {
        return player1Name + " & " + player2Name;
    }
    
    /**
     * 점수를 포맷된 문자열로 반환
     */
    public String getFormattedScore() {
        return String.format("%,d", score);
    }
    
    @Override
    public String toString() {
        return String.format("ScoreRecord[%s, score=%d, date=%s]", 
                             getPlayerNames(), score, getFormattedDate());
    }
}