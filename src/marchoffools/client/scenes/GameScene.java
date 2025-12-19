package marchoffools.client.scenes;

import static marchoffools.client.core.Assets.Backgrounds.DEFAULT;
import static marchoffools.client.core.Assets.Colors.*;
import static marchoffools.client.core.Config.*;
import static marchoffools.common.message.RoomActionMessage.*;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import marchoffools.client.ui.Sprite;
import marchoffools.client.core.Scene;
import marchoffools.common.message.GameInputMessage;
import marchoffools.common.message.GameResultMessage;
import marchoffools.common.message.GameStateMessage;
import marchoffools.common.model.GameConstants;
import marchoffools.common.model.GameEntity;
import marchoffools.common.model.ObstacleType;
import marchoffools.common.protocol.MessageType;

public class GameScene extends Scene implements NetworkListener {

    private static final long serialVersionUID = 1L;
    
    private String myName;
    private String opponentName;
    private int myRole;

    private JLabel lStage, lScore, lTimer;
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

    // 쿨다운 관리
    private javax.swing.Timer cooldownTimer;
    private long[] lastSkillUsedTime = new long[6];

    // 스킬 쿨타임 상수 (GameSkill과 동일)
    private static final long[] SKILL_COOLDOWNS = {
        30000,  // 외침: 30초
        500,    // 찌르기: 0.5초
        500,    // 베기: 0.5초
        0,      // 점프: 쿨타임 없음
        0,      // 슬라이드: 쿨타임 없음
        10000   // 돌진: 10초
    };

    public GameScene(String myName, String opponentName, int myRole, int opponentRole) {
        super(DEFAULT);

        this.myName = myName;
        this.opponentName = opponentName;
        this.myRole = myRole;
        
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
        
//        createExitButton();
        createLabelSection();
        createEmotionSection();
        createGameCanvas();
        createSkillUseSection();
        
        setupKeyBindings();
        startCooldownTimer();

        System.out.println("GameScene initialized:");
        System.out.println("  My Name: " + myName + " [" + getRoleName(myRole) + "]");
        System.out.println("  Opponent: " + opponentName + " [" + getRoleName(opponentRole) + "]");

    }
    
    @Override
    public void onExit() {
        if (cooldownTimer != null) {
            cooldownTimer.stop();
        }
        super.onExit();
    }
    
    // ==========================================
    //        쿨다운 관리
    // ==========================================
    
    /**
     * 쿨다운 타이머 시작 (50ms마다 업데이트)
     */
    private void startCooldownTimer() {
        cooldownTimer = new javax.swing.Timer(50, e -> updateCooldownDisplay());
        cooldownTimer.start();
    }

    /**
     * 쿨다운 표시 업데이트
     */
    private void updateCooldownDisplay() {
        long now = System.currentTimeMillis();

        for (int i = 0; i < skillButtons.length; i++) {
            if (!(skillButtons[i] instanceof SkillButton)) continue;
            if (SKILL_COOLDOWNS[i] == 0) continue;

            SkillButton skillButton = (SkillButton) skillButtons[i];
            long elapsed = now - lastSkillUsedTime[i];
            long remaining = SKILL_COOLDOWNS[i] - elapsed;

            if (remaining > 0) {
                // 쿨다운 중
                float ratio = (float) remaining / SKILL_COOLDOWNS[i];
                skillButton.setCooldownRatio(ratio);
                skillButton.setRemainingSeconds(remaining / 1000.0);
                skillButton.setEnabled(false);
            } else {
                // 쿨다운 끝
                skillButton.setCooldownRatio(0);
                skillButton.setEnabled(true);
            }
        }
    }
    
    /**
     * 스킬 사용 가능 여부 확인
     */
    private boolean canUseSkill(int skillId) {
        if (skillId < 0 || skillId >= 6) return false;
        if (SKILL_COOLDOWNS[skillId] == 0) return true;

        long now = System.currentTimeMillis();
        long elapsed = now - lastSkillUsedTime[skillId];
        return elapsed >= SKILL_COOLDOWNS[skillId];
    }
    
    // ==========================================
    //        UI 컴포넌트 생성
    // ==========================================
    
    private void createLabelSection() {
        JPanel topPanel = new JPanel();
        topPanel.setLayout(null); 
        topPanel.setOpaque(false);
        topPanel.setBounds(0, 0, WINDOW_WIDTH, 120);
        
		lStage = new JLabel("Stage 0", SwingConstants.LEFT);
		lStage.setFont(getFont().deriveFont(18f));
		lStage.setForeground(BLACK);
		lStage.setBounds(12, 12, WINDOW_WIDTH, 20);
		topPanel.add(lStage);
		
        lScore = new JLabel(String.format("%,d", score), SwingConstants.CENTER);
        lScore.setFont(getFont().deriveFont(Font.BOLD, 48f));
        lScore.setForeground(BLACK);
        lScore.setBounds(0, 30, WINDOW_WIDTH, 50);
        topPanel.add(lScore);
        
        lTimer = new JLabel("⏱ " + formatTime(playTime), SwingConstants.CENTER);
        lTimer.setFont(getFont().deriveFont(Font.BOLD, 24f));
        lTimer.setForeground(BLACK);
        lTimer.setBounds(0, 85, WINDOW_WIDTH, 30);
        topPanel.add(lTimer);

        add(topPanel, Integer.valueOf(javax.swing.JLayeredPane.PALETTE_LAYER));
    }
    
    private void createGameCanvas() {
        gameCanvas = new GameCanvas();
        gameCanvas.setBounds(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);
        add(gameCanvas, Integer.valueOf(javax.swing.JLayeredPane.DEFAULT_LAYER));
    }
    
//    private void createExitButton() {
//        Button bExit = new Button("->");
//        bExit.setFont(getFont().deriveFont(Font.BOLD, 30f));
//        bExit.setSize(100, 50);
//        bExit.setLocation(WINDOW_WIDTH - bExit.getWidth() - 72, 40);
//        bExit.addActionListener(e -> {
//            goBack();
//        });
//        add(bExit, Integer.valueOf(javax.swing.JLayeredPane.PALETTE_LAYER));
//    }
    
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
        add(emojiButton, Integer.valueOf(javax.swing.JLayeredPane.PALETTE_LAYER));

        if (isMyButton) {
            myEmojiButton = emojiButton;
        } else {
            opponentEmojiButton = emojiButton;
        }

        JLabel nameLabel = new JLabel(playerName, SwingConstants.CENTER);
        nameLabel.setFont(getFont().deriveFont(12f));
        nameLabel.setForeground(BLACK);
        nameLabel.setBounds(x, y + buttonSize + 2, buttonSize, 20);
        add(nameLabel, Integer.valueOf(javax.swing.JLayeredPane.PALETTE_LAYER));
    }
    
    private Button createEmojiButton(String playerName, boolean clickable) {
        Button button = new Button("😀");
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
        int buttonH = 100;
        int gap = 10;
        int margin = 30;
        
        int startX = WINDOW_WIDTH - (buttonW * 3 + gap * 2 + margin);
        int startY = WINDOW_HEIGHT - buttonH - margin;
        
        if (myRole == ROLE_KNIGHT) {
            // 기사 스킬 버튼들
            skillButtons[0] = new SkillButton("<html><center>외침(Q)</center></html>", 0);
            skillButtons[1] = new SkillButton("<html><center>찌르기(W)</center></html>", 1);
            skillButtons[2] = new SkillButton("<html><center>베기(E)</center></html>", 2);

            for (int i = 0; i < 3; i++) {
                int x = startX + i * (buttonW + gap);
                skillButtons[i].setBounds(x, startY, buttonW, buttonH);
                add(skillButtons[i], Integer.valueOf(javax.swing.JLayeredPane.PALETTE_LAYER));
            }

        } else if (myRole == ROLE_HORSE) {
            // 말 기본 동작 버튼들 (쿨다운 없음)
            Button bJump = createSkillButton("점프(Space)", -1);
            bJump.setBounds(startX, startY, buttonW, buttonH);
            bJump.addActionListener(e -> handleJumpInput());
            add(bJump, Integer.valueOf(javax.swing.JLayeredPane.PALETTE_LAYER));

            Button bSlide = createSkillButton("슬라이드(Shift)", -2);
            bSlide.setBounds(startX + buttonW + gap, startY, buttonW, buttonH);
            bSlide.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mousePressed(java.awt.event.MouseEvent e) {
                    handleSlideInput(true);
                }
                @Override
                public void mouseReleased(java.awt.event.MouseEvent e) {
                    handleSlideInput(false);
                }
            });
            add(bSlide, Integer.valueOf(javax.swing.JLayeredPane.PALETTE_LAYER));

            // 돌진 스킬 (쿨다운 있음)
            int x = startX + (buttonW + gap) * 2;
            skillButtons[5] = new SkillButton("<html><center>돌진(R)</center></html>", 5);
            skillButtons[5].setBounds(x, startY, buttonW, buttonH);
            add(skillButtons[5], Integer.valueOf(javax.swing.JLayeredPane.PALETTE_LAYER));
        }
    }
    
    private Button createSkillButton(String text, int skillId) {
        Button button = new Button(String.format("<html><center>%s</center></html>", text));
        button.setFont(getFont().deriveFont(Font.BOLD, 14f));
        button.setForeground(BLACK);
        button.setPreferredSize(new Dimension(100, 100));
        button.setMinimumSize(new Dimension(100, 100));
        button.setMaximumSize(new Dimension(100, 100));

        button.setButtonColors(WHITE, WHITE.brighter(), LIGHT_GRAY);
        button.setBorder(BorderFactory.createLineBorder(GRAY, 3));

        if (skillId >= 0) {
            button.addActionListener(e -> handleSkillUse(skillId));
        }

        return button;
    }
    
    // ==========================================
    //        키보드 입력 설정
    // ==========================================
    
    private void setupKeyBindings() {
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
            	System.out.println("Key pressed: " + e.getKeyCode()); // DEBUG
                if (myRole == ROLE_HORSE) {
                    switch (e.getKeyCode()) {
                        case KeyEvent.VK_SPACE:
                            handleJumpInput();
                            break;
                        case KeyEvent.VK_SHIFT:
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
                        if (myRole == ROLE_KNIGHT) handleSkillUse(0);
                        break;
                    case KeyEvent.VK_W:
                        if (myRole == ROLE_KNIGHT) handleSkillUse(1);
                        break;
                    case KeyEvent.VK_E:
                        if (myRole == ROLE_KNIGHT) handleSkillUse(2);
                        break;
                    case KeyEvent.VK_R:
                        if (myRole == ROLE_HORSE) handleSkillUse(5);
                        break;
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                if (myRole == ROLE_HORSE) {
                    if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
                        isSlideKeyPressed = false;
                        handleSlideInput(false);
                    }
                }
            }
        });

        setFocusable(true);
        setFocusTraversalKeysEnabled(false);
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

        System.out.println("✔ Jump input sent");
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

        System.out.println("✔ Slide " + (pressed ? "ON" : "OFF"));
    }

    private void handleSkillUse(int skillId) {
        // 쿨다운 체크
        if (!canUseSkill(skillId)) {
            System.out.println("⏱ Skill " + skillId + " is on cooldown");
            return;
        }

        NetworkManager nm = getNetworkManager();
        if (nm == null) return;

        GameInputMessage msg = new GameInputMessage(
            nm.getPlayerId(),
            GameInputMessage.ATTACK,
            skillId
        );
        nm.sendMessage(MessageType.GAME_INPUT, msg);

        // 쿨타임 시작
        long now = System.currentTimeMillis();
        lastSkillUsedTime[skillId] = now;

        // 찌르기/베기는 쿨타임 묶기
        if (skillId == 1) {  // 찌르기
            lastSkillUsedTime[2] = now;
        } else if (skillId == 2) {  // 베기
            lastSkillUsedTime[1] = now;
        }

        System.out.println("✔ Skill use sent: " + skillId);

        // 즉시 UI 업데이트
        updateCooldownDisplay();
    }

    // ==========================================
    //        UI 업데이트
    // ==========================================
    public void updateStage(String stage) {
    	lStage.setText(stage);
    }
    
    public void updateTimer(int seconds) {
        this.playTime = seconds;
        lTimer.setText("⏱ " + formatTime(seconds));
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
        
        add(emojiSelectorPanel, Integer.valueOf(javax.swing.JLayeredPane.POPUP_LAYER));
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
            default: return "😀";
        }
    }
    
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
    }
    
    /**
     * 게임 화면을 그리는 캔버스
     */
    public class GameCanvas extends JPanel {
        private static final long serialVersionUID = 1L;
        
        private Sprite player;
        private Map<String, Sprite> obstacles;
        private List<Sprite> skillRanges;
//        private GameStateMessage lastGameState;
        
        public GameCanvas() {
            setOpaque(false);
            setLayout(null);
            
            player = new Sprite(
                Sprite.TYPE_PLAYER,
                GameConstants.CHARACTER_X,
                GameConstants.CHARACTER_Y,
                GameConstants.CHARACTER_WIDTH,
                GameConstants.CHARACTER_HEIGHT
            );

            player.setImage("player");
            player.setLabel("Player");

            obstacles = new HashMap<>();
            skillRanges = new ArrayList<>();
        }

        public void updateGameState(GameStateMessage msg) {
            updateCharacter(msg);
            updateObstacles(msg.getObstacles());
            updateSkillRanges(msg.getActiveSkills());
            repaint();
        }

        private void updateCharacter(GameStateMessage msg) {
            player.setY(msg.getPlayerY());
            player.setState(msg.getCharState());
            player.setInvincible(msg.isInvincible());
        }

        private void updateObstacles(List<GameStateMessage.ObstacleData> obsData) {
            obstacles.keySet().removeIf(id ->
                obsData.stream().noneMatch(o -> o.getId().equals(id))
            );

            for (GameStateMessage.ObstacleData data : obsData) {
                Sprite sprite = obstacles.get(data.getId());

                if (sprite == null) {
                    ObstacleType type = ObstacleType.getById(data.getType());
                    GameEntity entity = GameEntity.getByType(type);

                    sprite = new Sprite(
                        Sprite.TYPE_OBSTACLE,
                        data.getX(),
                        data.getY(),
                        type.getWidth(),
                        type.getHeight()
                    );
                    sprite.setSubType(data.getType());

                    if (entity != null) {
                        sprite.setImage(entity.name());
                        sprite.setFillColor(entity.getDefaultColor());
                        sprite.setLabel(entity.getName());
                    }

                    obstacles.put(data.getId(), sprite);
                } else {
                    sprite.setX(data.getX());
                    sprite.setY(data.getY());
                    sprite.setDestroyed(data.isDestroyed());
                }
            }
        }
        
        private void updateSkillRanges(boolean[] activeSkills) {
            skillRanges.clear();
            
            if (activeSkills == null) return;
            
            double playerX = player.getX();
            double playerY = player.getY();
            int playerWidth = player.getWidth();
            int playerHeight = player.getHeight();
            int playerRight = (int)(playerX + playerWidth);

            // 외침 스킬 (0번): 화면 전체
            if (activeSkills[0]) {
                Sprite shout = new Sprite(
                    Sprite.TYPE_SKILL,
                    0,
                    0,
                    GameConstants.GAME_WIDTH,
                    GameConstants.GAME_HEIGHT
                );
                shout.setFillColor(new Color(255, 200, 0, 50));
                shout.setStrokeColor(new Color(255, 200, 0, 200));
                shout.setStrokeWidth(3);
                shout.setLabel("외침!");
                skillRanges.add(shout);
            }

            // 찌르기 스킬 (1번): 플레이어 오른쪽 150px
            if (activeSkills[1]) {
                Sprite thrust = new Sprite(
                    Sprite.TYPE_SKILL,
                    playerRight,
                    (int)playerY,
                    GameConstants.THRUST_RANGE,
                    playerHeight
                );
                thrust.setFillColor(new Color(255, 0, 0, 80));
                thrust.setStrokeColor(new Color(255, 0, 0, 255));
                thrust.setStrokeWidth(2);
                thrust.setLabel("찌르기");
                skillRanges.add(thrust);
            }

            // 베기 스킬 (2번): 플레이어 오른쪽 100px
            if (activeSkills[2]) {
                Sprite slash = new Sprite(
                    Sprite.TYPE_SKILL,
                    playerRight,
                    (int)playerY,
                    GameConstants.SLASH_RANGE,
                    playerHeight
                );
                slash.setFillColor(new Color(0, 150, 255, 80));
                slash.setStrokeColor(new Color(0, 150, 255, 255));
                slash.setStrokeWidth(2);
                slash.setLabel("베기");
                skillRanges.add(slash);
            }
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                RenderingHints.VALUE_ANTIALIAS_ON);
            
//            // DEBUG: 지면 라인
//            g2d.setColor(new Color(150, 150, 150, 100));
//            g2d.drawLine(0, GameConstants.GROUND_Y, getWidth(), GameConstants.GROUND_Y);
            
            // 1. 스킬 범위 (제일 뒤)
            for (Sprite skillRange : skillRanges) {
                skillRange.draw(g2d);
            }
            
            // 2. 몬스터 (뒤)
            for (Sprite obstacle : obstacles.values()) {
                ObstacleType type = ObstacleType.getById(obstacle.getSubType());
                if (type.isMonster()) {
                    obstacle.draw(g2d);
                }
            }
            
            // 3. 장애물 (앞)
            for (Sprite obstacle : obstacles.values()) {
                ObstacleType type = ObstacleType.getById(obstacle.getSubType());
                if (type.isObstacle()) {
                    obstacle.draw(g2d);
                }
            }
            
            // 4. 플레이어 (제일 앞)
            player.draw(g2d);
        }
    }

    /**
     * 스킬 버튼 (쿨다운 시각화)
     */
    private class SkillButton extends Button {
        private static final long serialVersionUID = 1L;
        
        private int skillId;
        private float cooldownRatio = 0.0f;  // 0.0 (끝) ~ 1.0 (시작)
        private double remainingSeconds = 0.0;
        
        public SkillButton(String text, int skillId) {
            super(text);
            this.skillId = skillId;

            setFont(getFont().deriveFont(Font.BOLD, 14f));
            setForeground(BLACK);
            setPreferredSize(new Dimension(100, 100));
            setMinimumSize(new Dimension(100, 100));
            setMaximumSize(new Dimension(100, 100));

            setButtonColors(WHITE, WHITE.brighter(), LIGHT_GRAY);
            setBorder(BorderFactory.createLineBorder(GRAY, 3));
            
            // 스킬 사용 액션 리스너
            addActionListener(e -> handleSkillUse(skillId));
        }
        
        public int getSkillId() {
            return skillId;
        }
        
        /**
         * 쿨다운 비율 설정 (0.0 = 끝, 1.0 = 시작)
         */
        public void setCooldownRatio(float ratio) {
            this.cooldownRatio = Math.max(0, Math.min(1, ratio));
            repaint();
        }
        
        /**
         * 남은 시간 설정 (초 단위)
         */
        public void setRemainingSeconds(double seconds) {
            this.remainingSeconds = seconds;
            repaint();
        }
        
        /**
         * 쿨다운 중인지 확인
         */
        public boolean isOnCooldown() {
            return cooldownRatio > 0;
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            // 기본 버튼 그리기
            super.paintComponent(g);
            
            // 쿨다운이 없으면 오버레이 그리지 않음
            if (cooldownRatio <= 0) return;

            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();

            // 반투명 검정 오버레이 (위에서부터 cooldownRatio 비율만큼)
            int overlayHeight = (int) (height * cooldownRatio);
            g2d.setColor(new Color(0, 0, 0, 180));
            g2d.fillRoundRect(0, 0, width, overlayHeight, 10, 10);

            // 남은 시간 텍스트 (중앙)
            if (remainingSeconds > 0.05) {
                String timeText = String.format("%.0f", Math.ceil(remainingSeconds));

                // 텍스트 크기 계산
                g2d.setFont(new Font("Arial", Font.BOLD, 28));
                int textWidth = g2d.getFontMetrics().stringWidth(timeText);
                int textHeight = g2d.getFontMetrics().getAscent();

                int textX = (width - textWidth) / 2;
                int textY = height / 2 + textHeight / 2 - 5;

                // 텍스트 외곽선 (더 잘 보이도록)
                g2d.setColor(Color.BLACK);
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dy = -1; dy <= 1; dy++) {
                        if (dx != 0 || dy != 0) {
                            g2d.drawString(timeText, textX + dx, textY + dy);
                        }
                    }
                }

                // 실제 텍스트
                g2d.setColor(Color.WHITE);
                g2d.drawString(timeText, textX, textY);
            }

            g2d.dispose();
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
        	requestFocusInWindow();
        	updateStage(msg.getStageInfo());
            updateScore(msg.getScore());
            updateTimer(msg.getPlayTime());
            gameCanvas.updateGameState(msg);
        });
    }
    
    @Override
    public void onGameResult(GameResultMessage msg) {
        System.out.println("GameScene received GameResult: score=" + msg.getTotalScore());
        
        SwingUtilities.invokeLater(() -> {
            GameResultScene resultScene = new GameResultScene(msg);
            
            switchToWithoutHistory(resultScene);
        });
    }
}