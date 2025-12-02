package marchoffools.client.scenes;

import static marchoffools.client.core.Assets.Backgrounds.DEFAULT;
import static marchoffools.client.core.Assets.Colors.*;
import static marchoffools.client.core.Config.*;
import static marchoffools.common.message.RoomActionMessage.*;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.MouseAdapter; 
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
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
    private javax.swing.Timer gameTimer;
    private int remainingTime = 180;
    
    private JPanel currentEmojiSelector = null;
    private Button currentEmojiButton = null;
    
    private MouseAdapter sceneMouseListener;
    
    private Button myEmojiButton;
    private Button opponentEmojiButton;

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
        
        System.out.println("GameScene initialized:");
        System.out.println("  My Name: " + myName + " [" + getRoleName(myRole) + "]");
        System.out.println("  Opponent: " + opponentName + " [" + getRoleName(opponentRole) + "]");
    }
    
    @Override
    public void onExit() {
        stopGameTimer();
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
        
        lTimer = new JLabel("⏱ " + formatTime(remainingTime), SwingConstants.CENTER);
        lTimer.setFont(getFont().deriveFont(Font.BOLD, 24f));
        lTimer.setForeground(BLACK);
        lTimer.setBounds(0, 55, WINDOW_WIDTH, 30);
        topPanel.add(lTimer);
        
        add(topPanel);
        
        startGameTimer();
    }
    
    private void createGameCanvas() {
        gameCanvas = new GameCanvas();
        gameCanvas.setBounds(0, 120, WINDOW_WIDTH, WINDOW_HEIGHT - 120);
//        gameCanvas.setBackground(Color.WHITE);
//        gameCanvas.setOpaque(true);
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
        
        int sectionHeight = buttonSize + 20;  // 버튼 + 라벨
        int totalHeight = sectionHeight * 2 + gap;
        int startY = (WINDOW_HEIGHT - totalHeight) / 2;
        
        // 내 감정 표현 섹션 (클릭 가능)
        createPlayerEmojiSection(myName, true, 20, startY);
        
        // 상대방 감정 표현 섹션 (클릭 불가)
        createPlayerEmojiSection(opponentName, false, 20, startY + sectionHeight + gap);
    }
    
    private void createPlayerEmojiSection(String playerName, boolean isMyButton, int x, int y) {
        int buttonSize = 70;
        
        // 이모지 버튼
        Button emojiButton = createEmojiButton(playerName, isMyButton);
        emojiButton.setBounds(x, y, buttonSize, buttonSize);
        add(emojiButton);
        
        // 참조 저장
        if (isMyButton) {
            myEmojiButton = emojiButton;
        } else {
            opponentEmojiButton = emojiButton;
        }
        
        // 플레이어 이름 라벨
        JLabel nameLabel = new JLabel(playerName, SwingConstants.CENTER);
        nameLabel.setFont(getFont().deriveFont(12f));
        nameLabel.setForeground(BLACK);
        nameLabel.setBounds(x, y + buttonSize + 2, buttonSize, 20);
        add(nameLabel);
    }
    
    private Button createEmojiButton(String playerName, boolean clickable) {
        // 초기 이모지 설정
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
            button.setEnabled(true);  // 버튼은 활성화 상태 유지
            button.setFocusable(false);  // 포커스 불가
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
        
        // 실제 스킬 이름 배열 (추후 변경 가능)
        Skill[] skills = {Skill.SHIELD, Skill.SPECIAL, Skill.INVINCIBLE};
        
        for (int i = 0; i < skills.length; i++) {
            Button bSkill = createSkillButton(skills[i]);
            bSkill.setBounds(startX + i * (buttonW + gap), startY, buttonW, buttonH);
            add(bSkill);
        }
    }
    
    private Button createSkillButton(Skill skill) {
        Button button = new Button(skill.getDisplayName());
        button.setFont(getFont().deriveFont(Font.BOLD, 18f));
        button.setForeground(BLACK);
        button.setPreferredSize(new Dimension(100, 70));
        button.setMinimumSize(new Dimension(100, 70));
        button.setMaximumSize(new Dimension(100, 70));

        button.setButtonColors(WHITE, WHITE.brighter(), LIGHT_GRAY);
        button.setBorder(BorderFactory.createLineBorder(GRAY, 3));

        button.addActionListener(e -> {
        	useSkill(skill);
            System.out.println("Used skill: " + skill.getDisplayName() + " (ID: " + skill.getId() + ")");
        });

        return button;
    }
    
    // ==========================================
    //        게임 로직
    // ==========================================
    
    private void startGameTimer() {
        gameTimer = new javax.swing.Timer(1000, e -> {
            if (remainingTime > 0) {
                remainingTime--;
                updateTimer(remainingTime);
            } else {
                gameTimer.stop();
                onTimeUp();
            }
        });
        gameTimer.start();
    }
    
    public void stopGameTimer() {
        if (gameTimer != null && gameTimer.isRunning()) {
            gameTimer.stop();
        }
    }
    
    private void onTimeUp() {
        System.out.println("Time's up!");
        // TODO: 게임 종료 처리(결과 화면 띄우기, 점수 저장, 대기실?로 돌아가기)
    }
    
    public void updateTimer(int seconds) {
        this.remainingTime = seconds;
        lTimer.setText("⏱ " + formatTime(remainingTime));
    }
    
    private String formatTime(int seconds) {
        int minutes = seconds / 60;
        int secs = seconds % 60;
        return String.format("%d:%02d", minutes, secs);
    }
    
    public void updateScore(int newScore) {
        this.score = newScore;
        lScore.setText(String.format("%,d", score));
    }
    
    private void useSkill(Skill skill) {
        switch (skill) {
            case SHIELD:
                // 방어막 로직
                break;
            case SPECIAL:
                // 필살기 로직
                break;
            case INVINCIBLE:
                // 무적 로직
                break;
        }
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
        
        // 팝업 패널
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
                    System.out.println("Emotion sent to server: " + emoji + " (type=" + emotionType + ")");
                }
                
                closeEmojiSelector();
            });
            
            emojiSelectorPanel.add(emojiOption, Integer.valueOf(javax.swing.JLayeredPane.POPUP_LAYER));
            emojiSelectorPanel.add(Box.createVerticalStrut(5));
        }
        
        int popupX = targetButton.getParent().getX() + targetButton.getX() + targetButton.getWidth() + 10;
        int popupY = targetButton.getParent().getY() + targetButton.getY();
        int popupWidth = 70;
        int popupHeight = availableEmojis.length * 65 + 10;
        
        emojiSelectorPanel.setBounds(popupX, popupY, popupWidth, popupHeight);
        
        // 기존 팝업이 있으면 제거
        closeEmojiSelector();
        
        currentEmojiSelector = emojiSelectorPanel;
        currentEmojiButton = targetButton;
        
        // 최상위 레이어에 팝업 추가
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
        
        if (playerId.equals(nm.getPlayerId())) {
            if (myEmojiButton != null) {
                myEmojiButton.setText(emoji);
            }
        } else {
            if (opponentEmojiButton != null) {
                opponentEmojiButton.setText(emoji);
            }
        }
        
        System.out.println("Emotion updated: " + playerId + " -> " + emoji);
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
    
    private class GameCanvas extends JPanel {
        private static final long serialVersionUID = 1L;
        
        private PlayerCharacter player;
        private List<Obstacle> obstacles;
        private List<Enemy> enemies;
        
        public GameCanvas() {
            setOpaque(false);
            setLayout(null);
            
            initializeGameObjects();
        }
        
        private void initializeGameObjects() {
            player = new PlayerCharacter(100, 300);
            
            obstacles = new ArrayList<>();
            obstacles.add(new Obstacle(700, 470));
            
            enemies = new ArrayList<>();
            enemies.add(new Enemy(900, 70, "eagle"));
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            
            if (player != null) {
                player.draw(g);
            }
            
            for (Obstacle obstacle : obstacles) {
                obstacle.draw(g);
            }
            
            for (Enemy enemy : enemies) {
                enemy.draw(g);
            }
        }
        
        public void addObstacle(Obstacle obstacle) {
            obstacles.add(obstacle);
        }
        
        public void addEnemy(Enemy enemy) {
            enemies.add(enemy);
        }
        
        public void removeObstacle(Obstacle obstacle) {
            obstacles.remove(obstacle);
        }
        
        public void removeEnemy(Enemy enemy) {
            enemies.remove(enemy);
        }
    }
    
    private class PlayerCharacter {
        private int x, y;
        private int width, height;
        private Image image;
        
        public PlayerCharacter(int x, int y) {
            this.x = x;
            this.y = y;
            loadImage();
        }
        
        private void loadImage() {
            try {
                image = new ImageIcon(getClass().getResource("/assets/testCharacter.png")).getImage();
                
                // 이미지 크기에 맞게 width, height 조정
                 width = image.getWidth(null);
                 height = image.getHeight(null);
            } catch (Exception e) {
                System.err.println("Failed to load player image: " + e.getMessage());
                image = null;
            }
        }
        
        public void draw(Graphics g) {
            if (image != null) {
                g.drawImage(image, x, y, width, height, null);
            } else {
                g.setColor(BLUE);
                g.fillRect(x, y, width, height);
                g.setColor(WHITE);
                g.drawString("Player", x + 20, y + 60);
            }
        }
        
        public int getWidth() { return width; }
        public int getHeight() { return height; }
    }
    
    private class Obstacle {
        private int x, y;
        private int width, height;
        private Image image;
        
        public Obstacle(int x, int y) {
            this.x = x;
            this.y = y;
            loadImage();
        }
        
        private void loadImage() {
            try {
                image = new ImageIcon(getClass().getResource("/assets/testObstacle.png")).getImage();
                
                // 이미지 크기에 맞게 width, height 조정 
                width = image.getWidth(null);
                height = image.getHeight(null);
            } catch (Exception e) {
                System.err.println("Failed to load obstacle image: " + e.getMessage());
                image = null;
            }
        }
        
        public void draw(Graphics g) {
            if (image != null) {
                g.drawImage(image, x, y, width, height, null);
            } else {
                g.setColor(GRAY);
                g.fillRect(x, y, width, height);
            }
        }
        
        public void move(int dx, int dy) {
            this.x += dx;
            this.y += dy;
        }
        
        public int getX() { return x; }
        public int getY() { return y; }
        public int getWidth() { return width; }
        public int getHeight() { return height; }
    }
    
    private class Enemy {
        private int x, y;
        private int width, height;
        private Image image;
        private String type; 
        
        public Enemy(int x, int y, String type) {
            this.x = x;
            this.y = y;
            this.type = type;
            loadImage();
        }
        
        private void loadImage() {
            try {
                image = new ImageIcon(getClass().getResource("/assets/testEnemy2.png")).getImage();
                
                // 이미지 크기에 맞게 width, height 조정 
                 width = image.getWidth(null);
                 height = image.getHeight(null);
            } catch (Exception e) {
                System.err.println("Failed to load enemy image: " + e.getMessage());
                image = null;
            }
        }
        
        public void draw(Graphics g) {
            if (image != null) {
                g.drawImage(image, x, y, width, height, null);
            } else {
                g.setColor(BLUE);
                g.fillOval(x, y, width, height);
                g.setColor(WHITE);
                g.drawString(type, x + 15, y + 45);
            }
        }
        
        public void move(int dx, int dy) {
            this.x += dx;
            this.y += dy;
        }
        
        public int getX() { return x; }
        public int getY() { return y; }
        public int getWidth() { return width; }
        public int getHeight() { return height; }
        public String getType() { return type; }
    }
    
    
    
    // ==========================================
    //        NetworkListener 구현
    // ==========================================
    
    @Override
    public void onGameInput(GameInputMessage msg) {
        System.out.println("GameScene received GameInput: type=" + msg.getInputType());
        
        switch (msg.getInputType()) {
            case GameInputMessage.EMOTION:
                // 감정 표현 업데이트
                updateEmotion(msg.getPlayerId(), msg.getValue());
                break;
                
            case GameInputMessage.JUMP:
            case GameInputMessage.SLIDE:
            case GameInputMessage.ATTACK:
                // TODO: 게임 액션 처리
                System.out.println("Game action received: " + msg.getInputType());
                break;
                
            case GameInputMessage.USE_ITEM:
                // TODO: 아이템 사용 처리
                System.out.println("Item use received: " + msg.getValue());
                break;
        }
    }
    
    @Override
    public void onGameState(GameStateMessage msg) {
        System.out.println("GameScene received GameState");
        
        // TODO: 서버에서 보낸 게임 상태 업데이트(예: 타이머, 점수, 플레이어 위치 등)
    }
    
    @Override
    public void onGameResult(GameResultMessage msg) {
        System.out.println("GameScene received GameResult: score=" + msg.getTotalScore());
        
        stopGameTimer();
        
        // TODO: 결과 화면으로 전환
    }
}