package marchoffools.client.scenes;

import marchoffools.client.core.Scene;
import marchoffools.client.ui.Button;

import static marchoffools.client.core.Assets.Backgrounds.DEFAULT;
import static marchoffools.client.core.Assets.Colors.*;
import static marchoffools.client.core.Config.*;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class ScoreScene extends Scene {

    // TODO: 실제 점수 데이터로 교체 필요
    private static class ScoreRecord {
        String medal;
        String playerName;
        String score;
        String date;
        
        ScoreRecord(String medal, String playerName, String score, String date) {
            this.medal = medal;
            this.playerName = playerName;
            this.score = score;
            this.date = date;
        }
    }
    
    public ScoreScene() {
        super(DEFAULT);
        setLayout(null);
        
        createFixedUiElements();
        createScoreBoardPanel();
        
        setFocusable(false);
    }
    
    private void createFixedUiElements() {
        JLabel lTitle = new JLabel("Score Board");
        lTitle.setFont(getFont().deriveFont(42f));
        lTitle.setForeground(BLACK);
        lTitle.setSize(lTitle.getPreferredSize().width + 50, 60);
        lTitle.setLocation(72, 48);
        add(lTitle);
        
        Button bExit = new Button("->");
        bExit.setFont(getFont().deriveFont(Font.BOLD, 30f));
        bExit.setBounds(WINDOW_WIDTH - 172, 40, 100, 50);
        bExit.addActionListener(e -> goBack());
        add(bExit);
        
        JPanel headerLine = new JPanel();
        headerLine.setBackground(LIGHT_GRAY);
        headerLine.setBounds(72, 110, WINDOW_WIDTH - 144, 2); 
        add(headerLine);
    }
    
    private void createScoreBoardPanel() {
        int contentMargin = 72;
        int topMargin = 130;
        int bottomMargin = 70;
        
        int contentW = WINDOW_WIDTH - (contentMargin * 2);
        int contentH = WINDOW_HEIGHT - topMargin - bottomMargin;
        
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBounds(contentMargin, topMargin, contentW, contentH);
        applySectionStyle(mainPanel);
        
        // TODO: 실제 데이터로 교체
        ScoreRecord[] myRecords = {
            new ScoreRecord("🥇", "Player123", "932,582", "2025/11/06"),
            new ScoreRecord("🥈", "Player123", "845,921", "2025/11/07"),
            new ScoreRecord("🥉", "Player123", "321,894", "2024/11/07")
        };
        
        ScoreRecord[] allRecords = {
            new ScoreRecord("🥇", "Player123", "14,872", "2025/11/07"),
            new ScoreRecord("🥈", "Player345", "845,921", "2025/11/07"),
            new ScoreRecord("🥉", "Player345", "549,716", "2024/11/07")
        };
        
        mainPanel.add(createScoreSection("나의 베스트 3", myRecords));
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        mainPanel.add(createHorizontalSeparator());
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        mainPanel.add(createScoreSection("전체 베스트 3", allRecords));
        
        add(mainPanel);
    }
    
    private JPanel createScoreSection(String title, ScoreRecord[] records) {
        JPanel section = new JPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setOpaque(false);
        
        section.add(Box.createVerticalGlue());
        
        section.add(createSectionTitle(title));
        section.add(Box.createRigidArea(new Dimension(0, 20)));
        
        for (ScoreRecord record : records) {
            section.add(createScoreRow(record));
            section.add(Box.createRigidArea(new Dimension(0, 12)));
        }
        
        section.add(Box.createVerticalGlue());
        
        return section;
    }
    
    private JPanel createHorizontalSeparator() {
        JPanel separator = new JPanel();
        separator.setBackground(LIGHT_GRAY);
        Dimension size = new Dimension(Short.MAX_VALUE, 2);
        separator.setPreferredSize(size);
        separator.setMaximumSize(size);
        separator.setMinimumSize(size);
        separator.setAlignmentX(LEFT_ALIGNMENT);
        return separator;
    }
    
    private void applySectionStyle(JPanel panel) {
        panel.setOpaque(true);
        panel.setBackground(WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(24, 32, 24, 32));
    }
    
    private JLabel createSectionTitle(String title) {
        JLabel label = new JLabel(title);
        label.setFont(getFont().deriveFont(Font.BOLD, 24f));
        label.setForeground(BLACK);
        label.setAlignmentX(LEFT_ALIGNMENT);
        label.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
        return label;
    }
    
    private JPanel createScoreRow(ScoreRecord record) {
    	
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Short.MAX_VALUE, 50));
        row.setAlignmentX(LEFT_ALIGNMENT);
        
        // --- [좌측] 메달 영역 ---
        JPanel medalPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0)); 
        medalPanel.setOpaque(false);
        medalPanel.setPreferredSize(new Dimension(80, 50));
        
        JLabel lMedal = new JLabel(record.medal);
        lMedal.setFont(getFont().deriveFont(36f));
        medalPanel.add(lMedal);
        
        row.add(medalPanel, BorderLayout.WEST);
        
        // --- [중앙] 데이터 영역 ---
        JPanel dataPanel = new JPanel(new GridLayout(1, 3));
        dataPanel.setOpaque(false);
        
        // 1) 플레이어 이름 
        JLabel lPlayer = new JLabel(record.playerName);
        lPlayer.setFont(getFont().deriveFont(20f));
        lPlayer.setForeground(BLACK);
        lPlayer.setHorizontalAlignment(SwingConstants.LEFT);
        dataPanel.add(lPlayer);
        
        // 2) 점수 
        JLabel lScore = new JLabel(record.score);
        lScore.setFont(getFont().deriveFont(Font.BOLD, 28f));
        lScore.setForeground(BLACK);
        lScore.setHorizontalAlignment(SwingConstants.CENTER);
        dataPanel.add(lScore);
        
        // 3) 날짜 
        JLabel lDate = new JLabel(record.date);
        lDate.setFont(getFont().deriveFont(18f));
        lDate.setForeground(GRAY);
        lDate.setHorizontalAlignment(SwingConstants.RIGHT);
        dataPanel.add(lDate);
        
        row.add(dataPanel, BorderLayout.CENTER);
        
        // 우측 여백을 위한 빈 패널 추가 
        JPanel rightMargin = new JPanel();
        rightMargin.setOpaque(false);
        rightMargin.setPreferredSize(new Dimension(32, 50)); 
        row.add(rightMargin, BorderLayout.EAST);

        return row;
    }
    
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(TRANSLUCENT_WHITE);
        g.fillRect(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);
        
        // 구분선
        g.setColor(LIGHT_GRAY);
        g.drawLine(72, 110, WINDOW_WIDTH - 72, 110);
    }
}