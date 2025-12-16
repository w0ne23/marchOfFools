package marchoffools.client.scenes;

import marchoffools.client.core.Scene;
import marchoffools.client.data.ScoreManager;
import marchoffools.client.data.ScoreRecord;
import marchoffools.client.network.NetworkManager;
import marchoffools.client.ui.Button;
import marchoffools.common.message.GameResultMessage;
import marchoffools.common.message.RoomActionMessage;
import marchoffools.common.protocol.MessageType;

import static marchoffools.client.core.Assets.Backgrounds.DEFAULT;
import static marchoffools.client.core.Assets.Colors.*;
import static marchoffools.client.core.Config.*;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class GameResultScene extends Scene {

    private static final long serialVersionUID = 1L;

    // 게임 결과 데이터
    private String player1Name;
    private String player2Name;
    private int totalScore;
    private int playTime;        // 초 단위
    private double distance;     // 미터 단위
    
    // 통계 데이터
    private int obstaclesDestroyed;
    private int obstaclesAvoided;
    private int player1Destroyed;
    private int player2Avoided;

    /**
     * GameResultMessage를 받는 생성자 (권장)
     */
    public GameResultScene(GameResultMessage result) {
        super(DEFAULT);
        
        // 메시지에서 데이터 추출
        this.player1Name = result.getPlayer1Name() != null ? result.getPlayer1Name() : "Player1";
        this.player2Name = result.getPlayer2Name() != null ? result.getPlayer2Name() : "Player2";
        this.totalScore = result.getTotalScore();
        this.playTime = (int)(result.getPlayTime() / 1000);  // ms -> 초
        this.distance = result.getFinalDistance() / 100.0;   // px -> m (100px = 1m)
        
        this.obstaclesDestroyed = result.getObstaclesDestroyed();
        this.obstaclesAvoided = result.getObstaclesAvoided();
        this.player1Destroyed = result.getPlayer1Destroyed();
        this.player2Avoided = result.getPlayer2Avoided();
        
        initializeScene();
        
        // 점수 저장
        saveScore();
    }
    
    /**
     * 간단한 생성자 (이전 버전 호환용)
     */
    public GameResultScene(int score, String playerName, int role) {
        super(DEFAULT);
        
        this.totalScore = score;
        this.player1Name = playerName;
        this.player2Name = "상대방";
        this.playTime = 0;
        this.distance = 0;
        this.obstaclesDestroyed = 0;
        this.obstaclesAvoided = 0;
        this.player1Destroyed = 0;
        this.player2Avoided = 0;
        
        initializeScene();
        
        // 점수 저장
        saveScore();
    }
    
    private void initializeScene() {
        setLayout(null);
        
        createFixedUiElements();
        createMainResultPanel();
        
        setFocusable(false);
    }
    
    /**
     * 점수 저장
     */
    private void saveScore() {
        try {
            ScoreRecord record = new ScoreRecord(
                player1Name,
                player2Name,
                totalScore,
                playTime,
                (int) distance,
                obstaclesDestroyed,
                obstaclesAvoided
            );
            
            ScoreManager.getInstance().addScore(record);
            System.out.println("점수 저장 완료: " + totalScore);
        } catch (Exception e) {
            System.err.println("점수 저장 실패: " + e.getMessage());
        }
    }
    
    private void createFixedUiElements() {
        JLabel lTitle = new JLabel("Game Over!");
        lTitle.setFont(getFont().deriveFont(42f));
        lTitle.setForeground(BLACK);
        lTitle.setSize(lTitle.getPreferredSize().width + 50, 60);
        lTitle.setLocation(72, 48);
        add(lTitle);
        
        JPanel headerLine = new JPanel();
        headerLine.setBackground(LIGHT_GRAY);
        headerLine.setBounds(72, 110, WINDOW_WIDTH - 144, 2); 
        add(headerLine);
    }
    
    private void createMainResultPanel() {
        int panelMargin = 72;
        int topMargin = 120; 
        
        int panelW = WINDOW_WIDTH - (panelMargin * 2);
        int panelH = 380; 
        
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBounds(panelMargin, topMargin, panelW, panelH);
        mainPanel.setOpaque(true);
        mainPanel.setBackground(WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(25, 40, 25, 40));
        
        mainPanel.add(createPlayerInfoSection());
        add(mainPanel);
        
        createStatisticsPanel();
        createButtonPanel();
    }
    
    private JPanel createPlayerInfoSection() {
        JPanel section = new JPanel(new GridLayout(1, 3, 20, 0));
        section.setOpaque(false);
        section.setMaximumSize(new Dimension(Short.MAX_VALUE, 330));
        
        // 기사 (Player 1)
        JPanel leftPanel = createPlayerPanel(player1Name, "🛡️", "기사");
        section.add(leftPanel);
        
        // 중앙 결과
        JPanel centerPanel = createResultInfoPanel();
        section.add(centerPanel);
        
        // 말 (Player 2)
        JPanel rightPanel = createPlayerPanel(player2Name, "🦄", "말");
        section.add(rightPanel);
        
        return section;
    }
    
    private JPanel createPlayerPanel(String playerName, String icon, String roleName) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        // 역할 이름
        JLabel lRole = new JLabel(roleName, SwingConstants.CENTER);
        lRole.setFont(getFont().deriveFont(16f));
        lRole.setForeground(GRAY);
        lRole.setAlignmentX(CENTER_ALIGNMENT);
        panel.add(lRole);
        
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        
        // 플레이어 이름
        JLabel lName = new JLabel(playerName, SwingConstants.CENTER);
        lName.setFont(getFont().deriveFont(Font.BOLD, 22f));
        lName.setForeground(BLACK);
        lName.setAlignmentX(CENTER_ALIGNMENT);
        panel.add(lName);
        
        panel.add(Box.createRigidArea(new Dimension(0, 30)));
        
        // 캐릭터 이미지
        try {
            java.awt.Image img = javax.imageio.ImageIO.read(
                getClass().getResourceAsStream("/assets/testCharacter.png")
            );
            
            if (img != null) {
                java.awt.Image scaledImg = img.getScaledInstance(180, 180, java.awt.Image.SCALE_SMOOTH);
                javax.swing.ImageIcon imageIcon = new javax.swing.ImageIcon(scaledImg);
                
                JLabel lImage = new JLabel(imageIcon);
                lImage.setAlignmentX(CENTER_ALIGNMENT);
                panel.add(lImage);
            } else {
                addFallbackIcon(panel, icon);
            }
        } catch (Exception e) {
            System.err.println("Failed to load testCharacter.png: " + e.getMessage());
            addFallbackIcon(panel, icon);
        }
        
        panel.add(Box.createVerticalGlue());
        
        return panel;
    }
    
    private void addFallbackIcon(JPanel panel, String icon) {
        JLabel lIcon = new JLabel(icon, SwingConstants.CENTER);
        lIcon.setFont(getFont().deriveFont(100f));
        lIcon.setAlignmentX(CENTER_ALIGNMENT);
        panel.add(lIcon);
    }
    
    private JPanel createResultInfoPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        
        panel.add(Box.createVerticalGlue());
        
        // 플레이 시간
        JLabel lTimer = new JLabel("⏱ " + formatTime(playTime), SwingConstants.CENTER);
        lTimer.setFont(getFont().deriveFont(Font.BOLD, 28f));
        lTimer.setForeground(BLACK);
        lTimer.setAlignmentX(CENTER_ALIGNMENT);
        panel.add(lTimer);
        
        panel.add(Box.createRigidArea(new Dimension(0, 20)));
        
        // 점수 라벨
        JLabel lScoreLabel = new JLabel("Score", SwingConstants.CENTER);
        lScoreLabel.setFont(getFont().deriveFont(20f));
        lScoreLabel.setForeground(GRAY);
        lScoreLabel.setAlignmentX(CENTER_ALIGNMENT);
        panel.add(lScoreLabel);
        
        panel.add(Box.createRigidArea(new Dimension(0, 8)));
        
        // 점수 값
        JLabel lScore = new JLabel(String.format("%,d", totalScore), SwingConstants.CENTER);
        lScore.setFont(getFont().deriveFont(Font.BOLD, 56f));
        lScore.setForeground(BLACK);
        lScore.setAlignmentX(CENTER_ALIGNMENT);
        panel.add(lScore);
        
        panel.add(Box.createRigidArea(new Dimension(0, 20)));
        
        // 거리
        JLabel lDistance = new JLabel("▶ " + String.format("%,.0fm", distance), SwingConstants.CENTER);
        lDistance.setFont(getFont().deriveFont(Font.BOLD, 24f));
        lDistance.setForeground(RED);
        lDistance.setAlignmentX(CENTER_ALIGNMENT);
        panel.add(lDistance);
        
        panel.add(Box.createVerticalGlue());
        
        return panel;
    }
    
    private void createStatisticsPanel() {
        int panelMargin = 72;
        int panelY = 120 + 380 + 20;
        int panelW = WINDOW_WIDTH - (panelMargin * 2);
        int panelH = 140;
        
        JPanel statsPanel = new JPanel(new GridLayout(2, 2, 60, 20));
        statsPanel.setBounds(panelMargin, panelY, panelW, panelH);
        statsPanel.setOpaque(true);
        statsPanel.setBackground(WHITE);
        statsPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        
        // 실제 통계 데이터 표시
        statsPanel.add(createStatItem("처치한 몬스터 수", String.valueOf(obstaclesDestroyed)));
        statsPanel.add(createStatItem("회피한 장애물 수", String.valueOf(obstaclesAvoided)));
        statsPanel.add(createStatItem("기사 기여 (처치)", String.valueOf(player1Destroyed)));
        statsPanel.add(createStatItem("말 기여 (회피)", String.valueOf(player2Avoided)));
        
        add(statsPanel);
    }
    
    private JPanel createStatItem(String label, String value) {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setOpaque(false);
        
        JLabel lLabel = new JLabel(label);
        lLabel.setFont(getFont().deriveFont(18f));
        lLabel.setForeground(GRAY);
        lLabel.setHorizontalAlignment(SwingConstants.LEFT);
        panel.add(lLabel, BorderLayout.CENTER);
        
        JLabel lValue = new JLabel(value);
        lValue.setFont(getFont().deriveFont(Font.BOLD, 26f));
        lValue.setForeground(BLACK);
        lValue.setHorizontalAlignment(SwingConstants.RIGHT);
        panel.add(lValue, BorderLayout.EAST);
        
        return panel;
    }
    
    private void createButtonPanel() {
        int buttonPanelWidth = 600;
        int buttonPanelHeight = 50;
        int panelX = (WINDOW_WIDTH - buttonPanelWidth) / 2; 
        int panelY = 520 + 140 + 30; 
        
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        buttonPanel.setBounds(panelX, panelY, buttonPanelWidth, buttonPanelHeight);
        buttonPanel.setOpaque(false);
        
        Button bBackToLobby = new Button("대기실로 돌아가기");
        bBackToLobby.setFont(getFont().deriveFont(16f));
        bBackToLobby.setButtonColors(LIGHT_GRAY, WHITE, GRAY);
        bBackToLobby.addActionListener(e -> handleBackToLobby());
        buttonPanel.add(bBackToLobby);
        
        Button bQuit = new Button("게임 종료하기");
        bQuit.setFont(getFont().deriveFont(16f));
        bQuit.setButtonColors(LIGHT_GRAY, WHITE, GRAY);
        bQuit.addActionListener(e -> handleQuit());
        buttonPanel.add(bQuit);
        
        add(buttonPanel);
    }
    
    /**
     * 대기실로 돌아가기 처리
     */
    private void handleBackToLobby() {
        System.out.println("대기실로 돌아가기 버튼 클릭");
        
        NetworkManager nm = getNetworkManager();
        if (nm != null && nm.isConnected()) {
            // 서버에 BACK_TO_LOBBY 요청 전송
            RoomActionMessage msg = new RoomActionMessage(
                nm.getPlayerId(),
                RoomActionMessage.BACK_TO_LOBBY
            );
            nm.sendMessage(MessageType.ROOM_ACTION, msg);
            System.out.println("서버에 BACK_TO_LOBBY 요청 전송");
            
            // ⭐ 히스토리 정리 후 LobbyScene으로 전환
            clearHistory();
            LobbyScene lobbyScene = new LobbyScene();
            switchToWithoutHistory(lobbyScene);
        } else {
            System.err.println("서버 연결이 없습니다. 타이틀로 이동합니다.");
            clearHistory();
            switchToWithoutHistory(new TitleScene());
        }
    }
    
    /**
     * 게임 종료 처리
     */
    private void handleQuit() {
        System.out.println("게임 종료하기 버튼 클릭");
        
        NetworkManager nm = getNetworkManager();
        if (nm != null && nm.isConnected()) {
            nm.disconnect();
            System.out.println("서버 연결 해제됨");
        }
        
        clearHistory();
        switchToWithoutHistory(new TitleScene());
    }
    
    private String formatTime(int seconds) {
        int hours = seconds / 3600;
        int minutes = (seconds % 3600) / 60;
        int secs = seconds % 60;
        
        if (hours > 0) {
            return String.format("%d:%02d:%02d", hours, minutes, secs);
        } else {
            return String.format("%d:%02d", minutes, secs);
        }
    }
    
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(TRANSLUCENT_WHITE);
        g.fillRect(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);
    }
}