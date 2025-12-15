package marchoffools.client.scenes;

import marchoffools.client.core.Scene;
import marchoffools.client.ui.Button;

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

    // 추후 실제 데이터 반영 예정
    private String player1Name = "Player123";
    private String player2Name = "닉네임";
    private int totalScore = 932582;
    private int playTime = 9058; 
    private int distance = 3768; 
    
    private int defeatedMonsters = 532;
    private int hitMonsters = 478;
    private int usedBoosters = 23;
    private int noHitDistance = 1238; 

    public GameResultScene() {
        super(DEFAULT);
        setLayout(null);
        
        createFixedUiElements();
        createMainResultPanel();
        
        setFocusable(false);
    }
    
    // 실제 데이터를 받는 생성자
    // public GameResultScene(String p1Name, String p2Name, int score, int time, GameStats stats) { ... }
    
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
        
        JPanel leftPanel = createPlayerPanel(player1Name, "🛡️");
        section.add(leftPanel);
        
        JPanel centerPanel = createResultInfoPanel();
        section.add(centerPanel);
        
        JPanel rightPanel = createPlayerPanel(player2Name, "🦄");
        section.add(rightPanel);
        
        return section;
    }
    
    private JPanel createPlayerPanel(String playerName, String icon) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        
        panel.add(Box.createRigidArea(new Dimension(0, 20)));
        
        JLabel lName = new JLabel(playerName, SwingConstants.CENTER);
        lName.setFont(getFont().deriveFont(Font.BOLD, 22f));
        lName.setForeground(BLACK);
        lName.setAlignmentX(CENTER_ALIGNMENT);
        panel.add(lName);
        
        panel.add(Box.createRigidArea(new Dimension(0, 40)));
        
        try {
            java.awt.Image img = javax.imageio.ImageIO.read(
                getClass().getResourceAsStream("/assets/testCharacter.png")
            );
            
            if (img != null) {
                java.awt.Image scaledImg = img.getScaledInstance(200, 200, java.awt.Image.SCALE_SMOOTH);
                javax.swing.ImageIcon imageIcon = new javax.swing.ImageIcon(scaledImg);
                
                JLabel lImage = new JLabel(imageIcon);
                lImage.setAlignmentX(CENTER_ALIGNMENT);
                panel.add(lImage);
            } else {
                JLabel lIcon = new JLabel(icon, SwingConstants.CENTER);
                lIcon.setFont(getFont().deriveFont(100f));
                lIcon.setAlignmentX(CENTER_ALIGNMENT);
                panel.add(lIcon);
            }
        } catch (Exception e) {
            System.err.println("Failed to load testCharacter.png: " + e.getMessage());
            JLabel lIcon = new JLabel(icon, SwingConstants.CENTER);
            lIcon.setFont(getFont().deriveFont(100f));
            lIcon.setAlignmentX(CENTER_ALIGNMENT);
            panel.add(lIcon);
        }
        
        panel.add(Box.createVerticalGlue());
        
        return panel;
    }
    
    private JPanel createResultInfoPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        
        panel.add(Box.createVerticalGlue());
        
        JLabel lTimer = new JLabel("⏱ " + formatTime(playTime), SwingConstants.CENTER);
        lTimer.setFont(getFont().deriveFont(Font.BOLD, 28f));
        lTimer.setForeground(BLACK);
        lTimer.setAlignmentX(CENTER_ALIGNMENT);
        panel.add(lTimer);
        
        panel.add(Box.createRigidArea(new Dimension(0, 20)));
        
        JLabel lScoreLabel = new JLabel("Score", SwingConstants.CENTER);
        lScoreLabel.setFont(getFont().deriveFont(20f));
        lScoreLabel.setForeground(GRAY);
        lScoreLabel.setAlignmentX(CENTER_ALIGNMENT);
        panel.add(lScoreLabel);
        
        panel.add(Box.createRigidArea(new Dimension(0, 8)));
        
        JLabel lScore = new JLabel(String.format("%,d", totalScore), SwingConstants.CENTER);
        lScore.setFont(getFont().deriveFont(Font.BOLD, 56f));
        lScore.setForeground(BLACK);
        lScore.setAlignmentX(CENTER_ALIGNMENT);
        panel.add(lScore);
        
        panel.add(Box.createRigidArea(new Dimension(0, 20)));
        
        JLabel lDistance = new JLabel("▶ " + String.format("%,dm", distance), SwingConstants.CENTER);
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
        
        statsPanel.add(createStatItem("처치한 몬스터 수", String.valueOf(defeatedMonsters)));
        statsPanel.add(createStatItem("회피한 장애물 수", String.valueOf(hitMonsters)));
        statsPanel.add(createStatItem("사용한 부스터 수", String.valueOf(usedBoosters)));
        statsPanel.add(createStatItem("No Hit", String.format("%,dm", noHitDistance)));
        
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
        bBackToLobby.addActionListener(e -> {
            System.out.println("대기실로 돌아가기");
//            switchTo(LobbyScene);
        });
        buttonPanel.add(bBackToLobby);
        
        Button bPlayAgain = new Button("다시 게임하기");
        bPlayAgain.setFont(getFont().deriveFont(16f));
        bPlayAgain.setButtonColors(LIGHT_GRAY, WHITE, GRAY);
        bPlayAgain.addActionListener(e -> {
            System.out.println("다시 게임하기");
//            switchTo(GameScene);
            // 게임 재시작 로직 작성
        });
        buttonPanel.add(bPlayAgain);
        
        add(buttonPanel);
    }
    
    private String formatTime(int seconds) {
        int hours = seconds / 3600;
        int minutes = (seconds % 3600) / 60;
        int secs = seconds % 60;
        return String.format("%d:%02d:%02d", hours, minutes, secs);
    }
    
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(TRANSLUCENT_WHITE);
        g.fillRect(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);
    }
}