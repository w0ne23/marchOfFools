package marchoffools.client.scenes;

import static marchoffools.client.core.Assets.Backgrounds.DEFAULT;
import static marchoffools.client.core.Assets.Colors.*;
import static marchoffools.client.core.Config.*;
import static marchoffools.common.message.RoomActionMessage.*;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import marchoffools.client.network.NetworkManager;
import marchoffools.client.network.NetworkListener;
import marchoffools.client.ui.Button;
import marchoffools.client.core.Scene;
import marchoffools.client.core.Skill;
import marchoffools.common.message.GameInputMessage;
import marchoffools.common.message.GameResultMessage;
import marchoffools.common.message.GameStateMessage;
import marchoffools.common.protocol.MessageType;

public class GameScene extends Scene implements NetworkListener {

    private static final long serialVersionUID = 1L;
    
    private String myName;
    private String opponentName;
    private int myRole;           
    private int opponentRole;

    private JLabel lScore;
    private JLabel lTimer;
    private GameCanvas gameCanvas;
    
    private int score = 0;
    private int playTime = 0;
    
    private JPanel currentEmojiSelector = null;
    private Button currentEmojiButton = null;
    
    private MouseAdapter sceneMouseListener;
    
    private Button myEmojiButton;
    private Button opponentEmojiButton;
    
    // 스킬 버튼들
    private Button[] skillButtons = new Button[6];
    
    // 슬라이드 키 상태
    private boolean isSlideKeyPressed = false;

    public GameScene(String myName, String opponentName, int myRole, int opponentRole) {
        super(DEFAULT);
        
        this.myName = myName;
        this.opponentName = opponentName;
        this.myRole = myRole;
        this.opponentRole = opponentRole;
        
        sceneMouseListener = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (currentEmojiSelector != null && 
                    !currentEmojiSelector.getBounds().contains(e.getPoint())) {
                    closeEmojiSelector();
                }
            }
        };
        addMouseListener(sceneMouseListener);
        
        createExitButton();
        createScoreTimeSection();
        createEmotionSection();
        createGameCanvas();
        createSkillUseSection();
        setupKeyBindings();
        
        System.out.println("GameScene initialized:");
        System.out.println("  My Name: " + myName + " [" + getRoleName(myRole) + "]");
        System.out.println("  Opponent: " + opponentName + " [" + getRoleName(opponentRole) + "]");
    }
    
    @Override
    public void onExit() {
        super.onExit();
    }
    
    // ==========================================
    //        UI 컴포넌트 생성
    // ==========================================
    
    private void createScoreTimeSection() {
        JPanel topPanel = new JPanel();
        topPanel.setLayout(null); 
        topPanel.setOpaque(false);
        topPanel.setBounds(0, 30, WINDOW_WIDTH, 100);
        
        lScore = new JLabel(String.format("%,d", score), SwingConstants.CENTER);
        lScore.setFont(getFont().deriveFont(Font.BOLD, 48f));
        lScore.setForeground(BLACK);
        lScore.setBounds(0, 0, WINDOW_WIDTH, 50);
        topPanel.add(lScore);
        
        lTimer = new JLabel("⏱ " + formatTime(playTime), SwingConstants.CENTER);
        lTimer.setFont(getFont().deriveFont(Font.BOLD, 24f));
        lTimer.setForeground(BLACK);
        lTimer.setBounds(0, 55, WINDOW_WIDTH, 30);
        topPanel.add(lTimer);
        
        add(topPanel);
    }
    
    private void createGameCanvas() {
        gameCanvas = new GameCanvas();
        gameCanvas.setBounds(0, 120, WINDOW_WIDTH, WINDOW_HEIGHT - 120);
        add(gameCanvas, Integer.valueOf(javax.swing.JLayeredPane.DEFAULT_LAYER));
    }
    
    private void createExitButton() {
        Button bExit = new Button("->");
        bExit.setFont(getFont().deriveFont(Font.BOLD, 30f));
        bExit.setSize(100, 50); 
        bExit.setLocation(WINDOW_WIDTH - bExit.getWidth() - 72, 40);
        bExit.addActionListener(e -> {
            goBack();
        });
        add(bExit);
    }
    
    private void createEmotionSection() {
        int buttonSize = 70;
        int gap = 40;
        
        int sectionHeight = buttonSize + 20;
        int totalHeight = sectionHeight * 2 + gap;
        int startY = (WINDOW_HEIGHT - totalHeight) / 2;
        
        createPlayerEmojiSection(myName, true, 20, startY);
        createPlayerEmojiSection(opponentName, false, 20, startY + sectionHeight + gap);
    }
    
    private void createPlayerEmojiSection(String playerName, boolean isMyButton, int x, int y) {
        int buttonSize = 70;
        
        Button emojiButton = createEmojiButton(playerName, isMyButton);
        emojiButton.setBounds(x, y, buttonSize, buttonSize);
        add(emojiButton);
        
        if (isMyButton) {
            myEmojiButton = emojiButton;
        } else {
            opponentEmojiButton = emojiButton;
        }
        
        JLabel nameLabel = new JLabel(playerName, SwingConstants.CENTER);
        nameLabel.setFont(getFont().deriveFont(12f));
        nameLabel.setForeground(BLACK);
        nameLabel.setBounds(x, y + buttonSize + 2, buttonSize, 20);
        add(nameLabel);
    }
    
    private Button createEmojiButton(String playerName, boolean clickable) {
        Button button = new Button("😐");
        button.setFont(getFont().deriveFont(40f));
        button.setPreferredSize(new Dimension(70, 70));
        button.setMinimumSize(new Dimension(70, 70));
        button.setMaximumSize(new Dimension(70, 70));
        
        if (clickable) {
            button.setButtonColors(WHITE, WHITE.brighter(), LIGHT_GRAY);
            button.setBorder(BorderFactory.createLineBorder(GRAY, 2));
            
            button.addActionListener(e -> {
                if (currentEmojiButton == button && currentEmojiSelector != null) {
                    closeEmojiSelector();
                } else {
                    showEmojiSelector(button);
                }
            });
        } else {
            button.setButtonColors(LIGHT_GRAY, LIGHT_GRAY, LIGHT_GRAY);
            button.setBorder(BorderFactory.createLineBorder(GRAY, 1));
            button.setEnabled(true);
            button.setFocusable(false);
        }
        
        return button;
    }
    
    private void createSkillUseSection() {
        int buttonW = 100;
        int buttonH = 70;
        int gap = 10;
        int margin = 30;
        
        int startX = WINDOW_WIDTH - (buttonW * 3 + gap * 2 + margin);
        int startY = WINDOW_HEIGHT - buttonH - margin;
        
        // 역할에 따라 다른 스킬 표시
        if (myRole == ROLE_KNIGHT) {
            // 기사 스킬: 외침, 찌르기, 베기
            skillButtons[0] = createSkillButton("외침(Q)", 0);
            skillButtons[1] = createSkillButton("찌르기(W)", 1);
            skillButtons[2] = createSkillButton("베기(E)", 2);
            
            for (int i = 0; i < 3; i++) {
                skillButtons[i].setBounds(startX + i * (buttonW + gap), startY, buttonW, buttonH);
                add(skillButtons[i]);
            }
        } else if (myRole == ROLE_HORSE) {
            // 말 스킬: 돌진만 (점프/슬라이드는 키보드)
            skillButtons[5] = createSkillButton("돌진(R)", 5);
            skillButtons[5].setBounds(startX + buttonW + gap, startY, buttonW, buttonH);
            add(skillButtons[5]);
        }
    }
    
    private Button createSkillButton(String text, int skillId) {
        Button button = new Button(text);
        button.setFont(getFont().deriveFont(Font.BOLD, 16f));
        button.setForeground(BLACK);
        button.setPreferredSize(new Dimension(100, 70));
        button.setMinimumSize(new Dimension(100, 70));
        button.setMaximumSize(new Dimension(100, 70));

        button.setButtonColors(WHITE, WHITE.brighter(), LIGHT_GRAY);
        button.setBorder(BorderFactory.createLineBorder(GRAY, 3));

        button.addActionListener(e -> handleSkillUse(skillId));

        return button;
    }
    
    // ==========================================
    //        키보드 입력 설정
    // ==========================================
    
    private void setupKeyBindings() {
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (myRole == ROLE_HORSE) {
                    switch (e.getKeyCode()) {
                        case KeyEvent.VK_SPACE:
                            // 점프
                            handleJumpInput();
                            break;
                        case KeyEvent.VK_SHIFT:
                            // 슬라이드 시작
                            if (!isSlideKeyPressed) {
                                isSlideKeyPressed = true;
                                handleSlideInput(true);
                            }
                            break;
                    }
                }
                
                // 스킬 단축키 (역할 무관)
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_Q:
                        if (myRole == ROLE_KNIGHT) handleSkillUse(0); // 외침
                        break;
                    case KeyEvent.VK_W:
                        if (myRole == ROLE_KNIGHT) handleSkillUse(1); // 찌르기
                        break;
                    case KeyEvent.VK_E:
                        if (myRole == ROLE_KNIGHT) handleSkillUse(2); // 베기
                        break;
                    case KeyEvent.VK_R:
                        if (myRole == ROLE_HORSE) handleSkillUse(5); // 돌진
                        break;
                }
            }
            
            @Override
            public void keyReleased(KeyEvent e) {
                if (myRole == ROLE_HORSE) {
                    if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
                        // 슬라이드 종료
                        isSlideKeyPressed = false;
                        handleSlideInput(false);
                    }
                }
            }
        });
        
        setFocusable(true);
        requestFocus();
    }
    
    // ==========================================
    //        입력 처리
    // ==========================================
    
    private void handleJumpInput() {
        NetworkManager nm = getNetworkManager();
        if (nm == null) return;
        
        GameInputMessage msg = new GameInputMessage(
            nm.getPlayerId(),
            GameInputMessage.JUMP
        );
        nm.sendMessage(MessageType.GAME_INPUT, msg);
        
        System.out.println("✓ Jump input sent");
    }
    
    private void handleSlideInput(boolean pressed) {
        NetworkManager nm = getNetworkManager();
        if (nm == null) return;
        
        GameInputMessage msg = new GameInputMessage(
            nm.getPlayerId(),
            GameInputMessage.SLIDE,
            pressed ? 1 : 0
        );
        nm.sendMessage(MessageType.GAME_INPUT, msg);
        
        System.out.println("✓ Slide " + (pressed ? "ON" : "OFF"));
    }
    
    private void handleSkillUse(int skillId) {
        NetworkManager nm = getNetworkManager();
        if (nm == null) return;
        
        GameInputMessage msg = new GameInputMessage(
            nm.getPlayerId(),
            GameInputMessage.USE_ITEM,
            skillId
        );
        nm.sendMessage(MessageType.GAME_INPUT, msg);
        
        System.out.println("✓ Skill use sent: " + skillId);
    }
    
    // ==========================================
    //        UI 업데이트
    // ==========================================
    
    public void updateTimer(int seconds) {
        SwingUtilities.invokeLater(() -> {
            this.playTime = seconds;
            lTimer.setText("⏱ " + formatTime(seconds));
        });
    }
    
    private String formatTime(int seconds) {
        int minutes = seconds / 60;
        int secs = seconds % 60;
        return String.format("%d:%02d", minutes, secs);
    }
    
    public void updateScore(int newScore) {
        SwingUtilities.invokeLater(() -> {
            this.score = newScore;
            lScore.setText(String.format("%,d", score));
        });
    }
    
    private String getRoleName(int role) {
        switch (role) {
            case ROLE_KNIGHT: return "Knight";
            case ROLE_HORSE: return "Horse";
            default: return "None";
        }
    }
    
    // ==========================================
    //        이모지 감정 표현 로직
    // ==========================================
    
    private void showEmojiSelector(Button targetButton) {
        String[] availableEmojis = {"😊", "😡", "😭", "😴", "😱"};
        
        JPanel emojiSelectorPanel = new JPanel();
        emojiSelectorPanel.setLayout(new BoxLayout(emojiSelectorPanel, BoxLayout.Y_AXIS));
        emojiSelectorPanel.setBackground(WHITE);
        emojiSelectorPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(LIGHT_GRAY, 1),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        
        for (String emoji : availableEmojis) {
            Button emojiOption = new Button(emoji);
            emojiOption.setFont(getFont().deriveFont(32f));
            emojiOption.setPreferredSize(new Dimension(60, 60));
            emojiOption.setMinimumSize(new Dimension(60, 60));
            emojiOption.setMaximumSize(new Dimension(60, 60));
            emojiOption.setAlignmentX(CENTER_ALIGNMENT);
            
            emojiOption.setButtonColors(WHITE, LIGHT_GRAY, GRAY);
            emojiOption.setBorder(BorderFactory.createLineBorder(LIGHT_GRAY, 1));
            
            emojiOption.addActionListener(e -> {
                NetworkManager nm = getNetworkManager();
                if (nm != null) {
                    int emotionType = emojiToEmotionType(emoji);
                    GameInputMessage msg = new GameInputMessage(
                        nm.getPlayerId(), 
                        GameInputMessage.EMOTION, 
                        emotionType
                    );
                    nm.sendMessage(MessageType.GAME_INPUT, msg);
                }
                
                closeEmojiSelector();
            });
            
            emojiSelectorPanel.add(emojiOption);
            emojiSelectorPanel.add(Box.createVerticalStrut(5));
        }
        
        int popupX = targetButton.getX() + targetButton.getWidth() + 10;
        int popupY = targetButton.getY();
        int popupWidth = 70;
        int popupHeight = availableEmojis.length * 65 + 10;
        
        emojiSelectorPanel.setBounds(popupX, popupY, popupWidth, popupHeight);
        
        closeEmojiSelector();
        
        currentEmojiSelector = emojiSelectorPanel;
        currentEmojiButton = targetButton;
        
        add(emojiSelectorPanel, Integer.valueOf(100)); 
        revalidate();
        repaint();
    }
    
    private void closeEmojiSelector() {
        if (currentEmojiSelector != null) {
            remove(currentEmojiSelector);
            currentEmojiSelector = null;
            currentEmojiButton = null;
            revalidate();
            repaint();
        }
    }
    
    public void updateEmotion(String playerId, int emotionType) {
        NetworkManager nm = getNetworkManager();
        if (nm == null) return;
        
        String emoji = emotionTypeToEmoji(emotionType);
        
        SwingUtilities.invokeLater(() -> {
            if (playerId.equals(nm.getPlayerId())) {
                if (myEmojiButton != null) {
                    myEmojiButton.setText(emoji);
                }
            } else {
                if (opponentEmojiButton != null) {
                    opponentEmojiButton.setText(emoji);
                }
            }
        });
    }
    
    private int emojiToEmotionType(String emoji) {
        switch (emoji) {
            case "😊": return GameInputMessage.EMOTION_HAPPY;
            case "😡": return GameInputMessage.EMOTION_ANGRY;
            case "😭": return GameInputMessage.EMOTION_SAD;
            case "😴": return GameInputMessage.EMOTION_SLEEP;
            case "😱": return GameInputMessage.EMOTION_SURPRISED;
            default: return GameInputMessage.EMOTION_HAPPY;
        }
    }
    
    private String emotionTypeToEmoji(int emotionType) {
        switch (emotionType) {
            case GameInputMessage.EMOTION_HAPPY: return "😊";
            case GameInputMessage.EMOTION_ANGRY: return "😡";
            case GameInputMessage.EMOTION_SAD: return "😭";
            case GameInputMessage.EMOTION_SLEEP: return "😴";
            case GameInputMessage.EMOTION_SURPRISED: return "😱";
            default: return "😐";
        }
    }
    
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
    }
    
    // ==========================================
    //        GameCanvas (간단 버전)
    // ==========================================
    
    private class GameCanvas extends JPanel {
        private static final long serialVersionUID = 1L;
        
        private double characterY = 300;
        private int characterState = 0;
        private boolean isInvincible = false;
        
        private List<GameStateMessage.ObstacleData> serverObstacles = new ArrayList<>();
        
        public GameCanvas() {
            setOpaque(false);
            setLayout(null);
        }
        
        public void updateCharacterState(double y, int state, boolean invincible) {
            this.characterY = y;
            this.characterState = state;
            this.isInvincible = invincible;
            repaint();
        }
        
        public void updateObstacles(List<GameStateMessage.ObstacleData> obstacles) {
            this.serverObstacles = new ArrayList<>(obstacles);
            repaint();
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            
            // 캐릭터 그리기
            g.setColor(BLUE);
            g.fillRect(100, (int)characterY, 50, 50);
            g.setColor(WHITE);
            g.drawString("Player", 110, (int)characterY + 30);
            
            // 무적 상태 표시
            if (isInvincible) {
                g.setColor(new java.awt.Color(255, 255, 0, 100));
                g.fillRect(80, (int)characterY - 10, 90, 70);
            }
            
            // 장애물 그리기
            for (GameStateMessage.ObstacleData obs : serverObstacles) {
                if (!obs.isDestroyed()) {
                    g.setColor(GRAY);
                    g.fillRect((int)obs.getX(), (int)obs.getY(), 50, 50);
                    
                    // 타입 표시
                    g.setColor(WHITE);
                    g.drawString("T" + obs.getType(), (int)obs.getX() + 15, (int)obs.getY() + 30);
                }
            }
        }
    }
    
    // ==========================================
    //        NetworkListener 구현
    // ==========================================
    
    @Override
    public void onGameInput(GameInputMessage msg) {
        switch (msg.getInputType()) {
            case GameInputMessage.EMOTION:
                updateEmotion(msg.getPlayerId(), msg.getValue());
                break;
        }
    }
    
    @Override
    public void onGameState(GameStateMessage msg) {
        SwingUtilities.invokeLater(() -> {
            // 점수/시간 업데이트
            updateScore(msg.getScore());
            updateTimer(msg.getRemainingTime());
            
            // 캐릭터 상태 업데이트
            gameCanvas.updateCharacterState(
                msg.getPlayerY(),
                msg.getCharState(),
                msg.isInvincible()
            );
            
            // 장애물 업데이트
            gameCanvas.updateObstacles(msg.getObstacles());
        });
    }
    
    @Override
    public void onGameResult(GameResultMessage msg) {
        System.out.println("GameScene received GameResult: score=" + msg.getTotalScore());
        // TODO: 결과 화면으로 전환
    }
}