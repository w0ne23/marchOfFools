package marchoffools.client.scenes;

import static marchoffools.client.core.Assets.Backgrounds.DEFAULT;
import static marchoffools.client.core.Assets.Colors.*;
import static marchoffools.client.core.Config.*;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.MouseAdapter; 
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import marchoffools.client.core.ResourceManager;
import marchoffools.client.core.Scene;
//import marchoffools.client.core.Skill;
import marchoffools.client.ui.Button;

public class TutorialScene extends Scene {

    private static final long serialVersionUID = 1L;
    
    private static final int ROLE_KNIGHT = 1;
    private static final int ROLE_HORSE = 2;
    
    // 플레이어 정보
    private String myName;
    private String opponentName;
    private int myRole;           
    private int opponentRole;

    private JLabel lScore;
    private JLabel lTimer;
    private GameCanvas gameCanvas;
    
    private int score = 0;
    private javax.swing.Timer gameTimer;
    private int playTime = 0;
    
    private JPanel currentEmojiSelector = null;
    private Button currentEmojiButton = null;
    
    private MouseAdapter sceneMouseListener;
    
    private Button myEmojiButton;
    private Button opponentEmojiButton;
    
    private Thread gameThread;
    private volatile boolean isRunning = false;
    private final int FPS = 60;                
    private final int TARGET_TIME = 1000 / FPS;

    public TutorialScene() {
        super(DEFAULT);
        
        marchoffools.client.core.ResourceManager.loadAllResources();
        
        this.myName = "Player1";
        this.opponentName = "Player2";
        this.myRole = ROLE_KNIGHT;
        this.opponentRole = ROLE_HORSE;
        
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
//        createSkillUseSection();
        
        System.out.println("TutorialScene initialized (Tutorial Mode):");
        System.out.println("  My Name: " + myName + " [" + getRoleName(myRole) + "]");
        System.out.println("  Opponent: " + opponentName + " [" + getRoleName(opponentRole) + "]");
        
        startGameLoop();
    }
    
    @Override
    public void onExit() {
        stopGameLoop();
        stopGameTimer();
        super.onExit();
    }
    
    // ==========================================
    //        게임 루프 로직 
    // ==========================================
    
    private void startGameLoop() {
        if (isRunning) return;
        isRunning = true;
        
        gameThread = new Thread(() -> {
            System.out.println("Game Loop Started");
            while (isRunning) {
                long startTime = System.currentTimeMillis();
                
                updateGame();
                repaint();
                
                long elapsedTime = System.currentTimeMillis() - startTime;
                long sleepTime = TARGET_TIME - elapsedTime;
                
                if (sleepTime > 0) {
                    try {
                        Thread.sleep(sleepTime);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        gameThread.start();
    }
    
    private void stopGameLoop() {
        isRunning = false;
        if (gameThread != null) {
            try {
                gameThread.join(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    
    private void updateGame() {
        if (gameCanvas != null) {
            gameCanvas.update();
        }
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
        
        startGameTimer();
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
    
//    private void createSkillUseSection() {
//        int buttonW = 100;
//        int buttonH = 70;
//        int gap = 10;
//        int margin = 30;
//        
//        int startX = WINDOW_WIDTH - (buttonW * 3 + gap * 2 + margin);
//        int startY = WINDOW_HEIGHT - buttonH - margin;
//        
//        Skill[] skills = {Skill.SHIELD, Skill.SPECIAL, Skill.INVINCIBLE};
//        
//        for (int i = 0; i < skills.length; i++) {
//            Button bSkill = createSkillButton(skills[i]);
//            bSkill.setBounds(startX + i * (buttonW + gap), startY, buttonW, buttonH);
//            add(bSkill);
//        }
//    }
    
//    private Button createSkillButton(Skill skill) {
//        Button button = new Button(skill.getDisplayName());
//        button.setFont(getFont().deriveFont(Font.BOLD, 18f));
//        button.setForeground(BLACK);
//        button.setPreferredSize(new Dimension(100, 70));
//        button.setMinimumSize(new Dimension(100, 70));
//        button.setMaximumSize(new Dimension(100, 70));
//
//        button.setButtonColors(WHITE, WHITE.brighter(), LIGHT_GRAY);
//        button.setBorder(BorderFactory.createLineBorder(GRAY, 3));
//
//        button.addActionListener(e -> {
//            useSkill(skill);
//            System.out.println("Used skill (Local): " + skill.getDisplayName());
//        });
//
//        return button;
//    }
    
    // ==========================================
    //        게임 로직
    // ==========================================
    
    private void startGameTimer() {
        gameTimer = new javax.swing.Timer(1000, e -> {
            playTime++;
            updateTimer(playTime);
        });
        gameTimer.start();
    }
    
    private void stopGameTimer() {
        if (gameTimer != null && gameTimer.isRunning()) {
            gameTimer.stop();
        }
        gameTimer = null;
    }
    
    public void updateTimer(int seconds) {
        SwingUtilities.invokeLater(() -> {
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
    
//    private void useSkill(Skill skill) {
//        switch (skill) {
//            case SHIELD: break;
//            case SPECIAL: break;
//            case INVINCIBLE: break;
//        }
//    }
    
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
                if (currentEmojiButton != null) {
                    currentEmojiButton.setText(emoji);
                    System.out.println("Local Emotion Updated: " + emoji);
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
            
            obstacles = new CopyOnWriteArrayList<>(); 
            obstacles.add(new Obstacle(700, 470));
            
            enemies = new CopyOnWriteArrayList<>();
            enemies.add(new Enemy(900, 70, "eagle"));
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            
            if (player != null) player.draw(g);
            for (Obstacle obstacle : obstacles) obstacle.draw(g);
            for (Enemy enemy : enemies) enemy.draw(g);
        }
        
        public void update() {
            int scrollSpeed = 5; 
            
            for (Obstacle obs : obstacles) obs.move(-scrollSpeed, 0);
            for (Enemy enemy : enemies) enemy.move(-scrollSpeed, 0);
        }
    }
    
    private class PlayerCharacter {
        private int x, y;
        private int width, height;
        private Image image;
        
        public PlayerCharacter(int x, int y) {
            this.x = x;
            this.y = y;
            initImage();
        }
        
        private void initImage() {
            this.image = ResourceManager.getImage("player");
            if (this.image != null) {
                this.width = image.getWidth(null);
                this.height = image.getHeight(null);
            } else {
                this.width = 50;
                this.height = 50;
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
    }
    
    private class Obstacle {
        private int x, y;
        private int width, height;
        private Image image;
        
        public Obstacle(int x, int y) {
            this.x = x;
            this.y = y;
            initImage();
        }
        
        private void initImage() {
            this.image = ResourceManager.getImage("obstacle");
            if (this.image != null) {
                this.width = image.getWidth(null);
                this.height = image.getHeight(null);
            } else {
                this.width = 50;
                this.height = 50;
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
            initImage();
        }
        
        private void initImage() {
            String resourceKey = "enemy_" + type; 
            this.image = ResourceManager.getImage(resourceKey);
            if (this.image == null) this.image = ResourceManager.getImage("enemy_default");

            if (this.image != null) {
                this.width = image.getWidth(null);
                this.height = image.getHeight(null);
            } else {
                this.width = 50;
                this.height = 50;
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
    }
}