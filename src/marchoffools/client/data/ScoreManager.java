package marchoffools.client.data;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 점수 기록을 관리하는 싱글톤 클래스
 * 로컬 파일에 점수를 저장하고 불러옵니다.
 */
public class ScoreManager {
    
    private static final String SCORE_FILE = "scores.dat";
    private static final int MAX_RECORDS = 100;  // 최대 저장 기록 수
    
    private static ScoreManager instance;
    
    private List<ScoreRecord> allRecords;
    private String currentPlayerName;  // 현재 로그인한 플레이어 이름
    
    private ScoreManager() {
        allRecords = new ArrayList<>();
        loadScores();
    }
    
    /**
     * 싱글톤 인스턴스 반환
     */
    public static synchronized ScoreManager getInstance() {
        if (instance == null) {
            instance = new ScoreManager();
        }
        return instance;
    }
    
    /**
     * 현재 플레이어 이름 설정
     */
    public void setCurrentPlayerName(String name) {
        this.currentPlayerName = name;
    }
    
    /**
     * 현재 플레이어 이름 반환
     */
    public String getCurrentPlayerName() {
        return currentPlayerName;
    }
    
    /**
     * 새 점수 기록 추가
     */
    public synchronized void addScore(ScoreRecord record) {
        allRecords.add(record);
        
        // 점수 내림차순 정렬
        Collections.sort(allRecords);
        
        // 최대 개수 초과 시 오래된 기록 삭제
        while (allRecords.size() > MAX_RECORDS) {
            allRecords.remove(allRecords.size() - 1);
        }
        
        // 파일에 저장
        saveScores();
        
        System.out.println("점수 추가됨: " + record);
        System.out.println("전체 기록 수: " + allRecords.size());
    }
    
    /**
     * 전체 베스트 기록 반환 (상위 N개)
     */
    public List<ScoreRecord> getTopScores(int count) {
        int size = Math.min(count, allRecords.size());
        return new ArrayList<>(allRecords.subList(0, size));
    }
    
    /**
     * 특정 플레이어의 베스트 기록 반환 (상위 N개)
     */
    public List<ScoreRecord> getPlayerTopScores(String playerName, int count) {
        List<ScoreRecord> playerRecords = new ArrayList<>();
        
        for (ScoreRecord record : allRecords) {
            // player1 또는 player2에 해당 플레이어가 있는지 확인
            if (record.getPlayer1Name().equals(playerName) || 
                record.getPlayer2Name().equals(playerName)) {
                playerRecords.add(record);
            }
        }
        
        // 이미 정렬되어 있으므로 상위 N개만 반환
        int size = Math.min(count, playerRecords.size());
        return new ArrayList<>(playerRecords.subList(0, size));
    }
    
    /**
     * 현재 플레이어의 베스트 기록 반환
     */
    public List<ScoreRecord> getCurrentPlayerTopScores(int count) {
        if (currentPlayerName == null || currentPlayerName.isEmpty()) {
            return new ArrayList<>();
        }
        return getPlayerTopScores(currentPlayerName, count);
    }
    
    /**
     * 전체 기록 수 반환
     */
    public int getTotalRecordCount() {
        return allRecords.size();
    }
    
    /**
     * 특정 플레이어의 기록 수 반환
     */
    public int getPlayerRecordCount(String playerName) {
        int count = 0;
        for (ScoreRecord record : allRecords) {
            if (record.getPlayer1Name().equals(playerName) || 
                record.getPlayer2Name().equals(playerName)) {
                count++;
            }
        }
        return count;
    }
    
    /**
     * 점수 기록을 파일에 저장
     */
    @SuppressWarnings("unchecked")
    private void saveScores() {
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream(SCORE_FILE))) {
            oos.writeObject(allRecords);
            System.out.println("점수 파일 저장 완료: " + SCORE_FILE);
        } catch (IOException e) {
            System.err.println("점수 저장 실패: " + e.getMessage());
        }
    }
    
    /**
     * 파일에서 점수 기록 불러오기
     */
    @SuppressWarnings("unchecked")
    private void loadScores() {
        File file = new File(SCORE_FILE);
        
        if (!file.exists()) {
            System.out.println("점수 파일 없음, 새로 시작합니다.");
            return;
        }
        
        try (ObjectInputStream ois = new ObjectInputStream(
                new FileInputStream(file))) {
            Object loaded = ois.readObject();
            
            if (loaded instanceof List) {
                allRecords = (List<ScoreRecord>) loaded;
                Collections.sort(allRecords);  // 정렬 확인
                System.out.println("점수 파일 로드 완료: " + allRecords.size() + "개 기록");
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("점수 로드 실패: " + e.getMessage());
            allRecords = new ArrayList<>();
        }
    }
    
    /**
     * 모든 기록 삭제 (테스트용)
     */
    public synchronized void clearAllRecords() {
        allRecords.clear();
        saveScores();
        System.out.println("모든 점수 기록이 삭제되었습니다.");
    }
    
    /**
     * 현재 플레이어의 최고 점수 반환
     */
    public int getCurrentPlayerBestScore() {
        List<ScoreRecord> records = getCurrentPlayerTopScores(1);
        return records.isEmpty() ? 0 : records.get(0).getScore();
    }
    
    /**
     * 전체 최고 점수 반환
     */
    public int getOverallBestScore() {
        return allRecords.isEmpty() ? 0 : allRecords.get(0).getScore();
    }
}