package marchoffools.common.model;

import java.io.Serializable;
import java.util.Date;

public class ScoreData implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private int rank;
    private String player1Name;
    private String player2Name;
    private int score;
    private double distance;
    private Date date;
    
    public ScoreData() {}
    
    public ScoreData(int rank, String player1Name, String player2Name, int score, double distance, Date date) {
        this.rank = rank;
        this.player1Name = player1Name;
        this.player2Name = player2Name;
        this.score = score;
        this.distance = distance;
        this.date = date;
    }
    
    // Getters & Setters
    public int getRank() {
        return rank;
    }
    
    public void setRank(int rank) {
        this.rank = rank;
    }
    
    public String getPlayer1Name() {
        return player1Name;
    }
    
    public void setPlayer1Name(String player1Name) {
        this.player1Name = player1Name;
    }
    
    public String getPlayer2Name() {
        return player2Name;
    }
    
    public void setPlayer2Name(String player2Name) {
        this.player2Name = player2Name;
    }
    
    public int getScore() {
        return score;
    }
    
    public void setScore(int score) {
        this.score = score;
    }
    
    public double getDistance() {
        return distance;
    }
    
    public void setDistance(double distance) {
        this.distance = distance;
    }
    
    public Date getDate() {
        return date;
    }
    
    public void setDate(Date date) {
        this.date = date;
    }
}