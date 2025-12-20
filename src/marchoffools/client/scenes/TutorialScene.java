package marchoffools.client.scenes;

import static marchoffools.client.core.Assets.Backgrounds.DEFAULT;
import static marchoffools.client.core.Assets.Colors.*;
import static marchoffools.client.core.Config.*;

import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.swing.*;

import marchoffools.client.core.ResourceManager;
import marchoffools.client.core.Scene;
import marchoffools.client.ui.Button;
import marchoffools.common.model.GameConstants;
import marchoffools.common.model.GameEntity;
import marchoffools.common.model.ObstacleType;

public class TutorialScene extends Scene {

    private static final long serialVersionUID = 1L;
    
    private static final int ROLE_KNIGHT = 1;
    private static final int ROLE_HORSE = 2;
    
    private static final double GRAVITY = 0.4;
    private static final double JUMP_VELOCITY = -16.0;
    
    private int currentRole = 0;
    
    private JPanel roleSelectPanel;
    private JPanel gamePanel;
    private TutorialCanvas gameCanvas;
    private JLabel tutorialGuideLabel;
    private JPanel guidePanel;
    
    private boolean isRunning = false;
    private boolean isPaused = true;
    private boolean isFreePlay = false; 
    private boolean isGameOver = false; 
    private Thread gameThread;
    private final int FPS = 60;
    private final int TARGET_TIME = 1000 / FPS;
    
    private List<TutorialStep> tutorialSteps;
    private int currentStepIndex = 0;
    private boolean waitingForInput = false;
    private int expectedKeyCode = -1;
    
    private double playerY = GameConstants.GROUND_Y - GameConstants.CHARACTER_HEIGHT;
    private double playerVelocityY = 0;
    private boolean isJumping = false;
    private boolean isSliding = false;
    private int playerState = 0; // 0: 일반, 1: 점프, 2: 슬라이드
    private boolean isInvincible = false;
    private long invincibleStartTime = 0;
    private static final long INVINCIBLE_DURATION = 2000; // 돌진 무적 2초
    
    // 장애물
    private List<TutorialObstacle> obstacles = new ArrayList<>();
    private int obstacleSpawnX = WINDOW_WIDTH + 100;
    private Random random = new Random();
    
    private boolean[] activeSkills = new boolean[6];
    private long[] skillActiveTimes = new long[6];
    
    private long[] lastSkillUsedTime = new long[6];
    private static final long[] SKILL_COOLDOWNS = {
        3000, 500, 500, 0, 0, 5000 
    };

    public TutorialScene() {
        super(DEFAULT);
        
        createRoleSelectPanel();
        
        setFocusable(true);
        requestFocusInWindow();
    }
    
    @Override
    public void onExit() {
        stopGameLoop();
        super.onExit();
    }
    
    // ==========================================
    //        역할 선택 화면
    // ==========================================
    
    private void createRoleSelectPanel() {
        roleSelectPanel = new JPanel();
        roleSelectPanel.setLayout(null);
        roleSelectPanel.setOpaque(false);
        roleSelectPanel.setBounds(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);
        
        JLabel titleLabel = new JLabel("튜토리얼", SwingConstants.CENTER);
        titleLabel.setFont(getFont().deriveFont(Font.BOLD, 48f));
        titleLabel.setForeground(BLACK);
        titleLabel.setBounds(0, 100, WINDOW_WIDTH, 60);
        roleSelectPanel.add(titleLabel);
        
        JLabel descLabel = new JLabel("플레이할 역할을 선택하세요", SwingConstants.CENTER);
        descLabel.setFont(getFont().deriveFont(24f));
        descLabel.setForeground(DARK_GRAY);
        descLabel.setBounds(0, 170, WINDOW_WIDTH, 40);
        roleSelectPanel.add(descLabel);
        
        int buttonWidth = 250;
        int buttonHeight = 300;
        int gap = 80;
        int startX = (WINDOW_WIDTH - (buttonWidth * 2 + gap)) / 2;
        int startY = 250;
        
        JPanel knightPanel = createRoleButton(
            "기사 (Knight)",
            "장애물을 공격하여 파괴합니다",
            "Q: 외침 | W: 찌르기 | E: 베기",
            ROLE_KNIGHT,
            new Color(200, 150, 100)
        );
        knightPanel.setBounds(startX, startY, buttonWidth, buttonHeight);
        roleSelectPanel.add(knightPanel);
        
        JPanel horsePanel = createRoleButton(
            "말 (Horse)",
            "이동으로 장애물을 회피합니다",
            "Space: 점프 | Shift: 슬라이드 | R: 돌진",
            ROLE_HORSE,
            new Color(150, 180, 220)
        );
        horsePanel.setBounds(startX + buttonWidth + gap, startY, buttonWidth, buttonHeight);
        roleSelectPanel.add(horsePanel);
        
        Button bExit = new Button("->");
        bExit.setFont(getFont().deriveFont(Font.BOLD, 30f));
        bExit.setBounds(WINDOW_WIDTH - 172, 40, 100, 50);
        bExit.addActionListener(e -> goBack());
        add(bExit);
        
        add(roleSelectPanel);
    }
    
    private JPanel createRoleButton(String title, String desc, String keys, int role, Color bgColor) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(bgColor);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(DARK_GRAY, 3),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        panel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        JLabel iconLabel = new JLabel(role == ROLE_KNIGHT ? "[기사]" : "[말]", SwingConstants.CENTER);
        iconLabel.setFont(getFont().deriveFont(Font.BOLD, 36f));
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(iconLabel);
        
        panel.add(Box.createVerticalStrut(15));
        
        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(getFont().deriveFont(Font.BOLD, 24f));
        titleLabel.setForeground(BLACK);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(titleLabel);
        
        panel.add(Box.createVerticalStrut(10));
        
        JLabel descLabel = new JLabel("<html><center>" + desc + "</center></html>", SwingConstants.CENTER);
        descLabel.setFont(getFont().deriveFont(14f));
        descLabel.setForeground(DARK_GRAY);
        descLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(descLabel);
        
        panel.add(Box.createVerticalStrut(15));
        
        JLabel keysLabel = new JLabel("<html><center>" + keys + "</center></html>", SwingConstants.CENTER);
        keysLabel.setFont(getFont().deriveFont(Font.BOLD, 12f));
        keysLabel.setForeground(new Color(80, 80, 80));
        keysLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(keysLabel);
        
        panel.add(Box.createVerticalGlue());
        
        Button startBtn = new Button("시작하기");
        startBtn.setFont(getFont().deriveFont(Font.BOLD, 16f));
        startBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        startBtn.setMaximumSize(new Dimension(150, 40));
        startBtn.addActionListener(e -> startTutorial(role));
        panel.add(startBtn);
        
        return panel;
    }
    
    // ==========================================
    //        튜토리얼 시작
    // ==========================================
    
    private void startTutorial(int role) {
        this.currentRole = role;
        
        remove(roleSelectPanel);
        createGamePanel();
        initTutorialSteps();
        setupKeyBindings();
        startGameLoop();
        startNextStep();
        
        revalidate();
        repaint();
        requestFocusInWindow();
    }
    
    private void createGamePanel() {
        gamePanel = new JPanel();
        gamePanel.setLayout(null);
        gamePanel.setOpaque(false);
        gamePanel.setBounds(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);
        
        String roleName = currentRole == ROLE_KNIGHT ? "기사" : "말";
        JLabel roleLabel = new JLabel("[" + roleName + " 튜토리얼]", SwingConstants.CENTER);
        roleLabel.setFont(getFont().deriveFont(18f));
        roleLabel.setForeground(DARK_GRAY);
        roleLabel.setBounds(0, 75, WINDOW_WIDTH, 30);
        gamePanel.add(roleLabel);
       
        gameCanvas = new TutorialCanvas();
        gameCanvas.setBounds(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);
        gamePanel.add(gameCanvas, Integer.valueOf(javax.swing.JLayeredPane.DEFAULT_LAYER));
        
        createGuidePanel();
        
        Button bExit = new Button("->");
        bExit.setFont(getFont().deriveFont(Font.BOLD, 30f));
        bExit.setBounds(WINDOW_WIDTH - 172, 40, 100, 50);
        bExit.addActionListener(e -> {
            stopGameLoop();
            goBack();
        });
        gamePanel.add(bExit, Integer.valueOf(javax.swing.JLayeredPane.PALETTE_LAYER));
        
        add(gamePanel);
    }
    
    private void createGuidePanel() {
        guidePanel = new JPanel();
        guidePanel.setLayout(new BorderLayout());
        guidePanel.setBackground(new Color(0, 0, 0, 180));
        
        guidePanel.setBounds(WINDOW_WIDTH / 4, 110, WINDOW_WIDTH / 2, 70);
        guidePanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(WHITE, 2),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        tutorialGuideLabel = new JLabel("", SwingConstants.CENTER);
        tutorialGuideLabel.setFont(getFont().deriveFont(Font.BOLD, 16f));
        tutorialGuideLabel.setForeground(WHITE);
        guidePanel.add(tutorialGuideLabel, BorderLayout.CENTER);
        
        gamePanel.add(guidePanel, Integer.valueOf(javax.swing.JLayeredPane.POPUP_LAYER));
    }
    
    // ==========================================
    //        튜토리얼 단계 정의
    // ==========================================
    
    private void initTutorialSteps() {
        tutorialSteps = new ArrayList<>();
        
        if (currentRole == ROLE_HORSE) {
            tutorialSteps.add(new TutorialStep("점프", "<html><center>하방 장애물!<br><b>[Space]</b>로 점프하세요</center></html>", KeyEvent.VK_SPACE, ObstacleType.GROUND_OBSTACLE, TutorialStep.Action.JUMP));
            tutorialSteps.add(new TutorialStep("슬라이드", "<html><center>상방 장애물!<br><b>[Shift]</b>로 슬라이드하세요</center></html>", KeyEvent.VK_SHIFT, ObstacleType.AIR_OBSTACLE, TutorialStep.Action.SLIDE));
            tutorialSteps.add(new TutorialStep("돌진", "<html><center>몬스터!<br><b>[R]</b>로 돌진하세요</center></html>", KeyEvent.VK_R, ObstacleType.SOFT_MONSTER, TutorialStep.Action.RUSH));
        } else {
            tutorialSteps.add(new TutorialStep("찌르기", "<html><center>연질 몬스터!<br><b>[W]</b>로 찌르세요</center></html>", KeyEvent.VK_W, ObstacleType.SOFT_MONSTER, TutorialStep.Action.THRUST));
            tutorialSteps.add(new TutorialStep("베기", "<html><center>경질 몬스터!<br><b>[E]</b>로 베세요</center></html>", KeyEvent.VK_E, ObstacleType.HARD_MONSTER, TutorialStep.Action.SLASH));
            tutorialSteps.add(new TutorialStep("외침", "<html><center>보스 몬스터!<br><b>[Q]</b>로 외치세요</center></html>", KeyEvent.VK_Q, ObstacleType.BOSS_MONSTER, TutorialStep.Action.SHOUT));
        }
        
        tutorialSteps.add(new TutorialStep("자유연습", "<html><center>기초 훈련 완료!<br>이제 자유롭게 연습해보세요. (아무 키나 누르세요)</center></html>", -1, null, TutorialStep.Action.COMPLETE));
    }
    
    private void startNextStep() {
        if (currentStepIndex >= tutorialSteps.size()) {
            startFreePlayMode();
            return;
        }
        
        TutorialStep step = tutorialSteps.get(currentStepIndex);
        
        if (step.obstacleType != null) {
            spawnObstacle(step.obstacleType);
            isPaused = false;
            waitingForInput = false;
        } else if (step.action == TutorialStep.Action.COMPLETE) {
            showGuide(step.guideText);
            isPaused = true;
            waitingForInput = true;
            expectedKeyCode = -1;
        }
    }
    
    private void spawnObstacle(ObstacleType type) {
        TutorialObstacle obs = new TutorialObstacle(type, obstacleSpawnX);
        obstacles.add(obs);
    }
    
    // ==========================================
    //        자유 연습 모드 (Free Play)
    // ==========================================

    private void startFreePlayMode() {
        isFreePlay = true;
        isPaused = false;
        waitingForInput = false;
        
        String controlsText;
        if (currentRole == ROLE_HORSE) {
            controlsText = "<html><center>" +
                "<b>[Space]</b> 점프 | " +
                "<b>[Shift]</b> 슬라이드 | " +
                "<b>[R]</b> 돌진" +
                "</center></html>";
        } else {
            controlsText = "<html><center>" +
                "<b>[Q]</b> 외침(보스) | " +
                "<b>[W]</b> 찌르기(연질) | " +
                "<b>[E]</b> 베기(경질)" +
                "</center></html>";
        }
        showGuide(controlsText);
    }

    private void spawnRandomObstacle() {
        if (!obstacles.isEmpty()) {
            TutorialObstacle last = obstacles.get(obstacles.size() - 1);
            if (last.x > WINDOW_WIDTH - 300) return;
        }

        ObstacleType type;
        if (currentRole == ROLE_HORSE) {
            int r = random.nextInt(3);
            if (r == 0) type = ObstacleType.GROUND_OBSTACLE;
            else if (r == 1) type = ObstacleType.AIR_OBSTACLE;
            else type = ObstacleType.SOFT_MONSTER;
        } else {
            int r = random.nextInt(3);
            if (r == 0) type = ObstacleType.SOFT_MONSTER;
            else if (r == 1) type = ObstacleType.HARD_MONSTER;
            else type = ObstacleType.BOSS_MONSTER;
        }
        
        spawnObstacle(type);
    }

    private void showGuide(String text) {
        tutorialGuideLabel.setText(text);
        guidePanel.setVisible(true);
    }
    
    private void hideGuide() {
        guidePanel.setVisible(false);
    }
    
    // ==========================================
    //        키보드 입력
    // ==========================================
    
    private void setupKeyBindings() {
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                handleKeyPress(e.getKeyCode());
            }
            
            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
                    isSliding = false;
                    if (playerState == 2 && !isJumping) playerState = 0;
                }
            }
        });
        
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);
    }
    
    private void handleKeyPress(int keyCode) {
        if (isGameOver) return;
        
        // 1. 자유 연습 모드
        if (isFreePlay) {
            handleGameInput(keyCode);
            return;
        }

        // 2. 튜토리얼 모드 (특정 키 대기)
        if (waitingForInput) {
            TutorialStep step = tutorialSteps.get(currentStepIndex);
            
            if (step.action == TutorialStep.Action.COMPLETE) {
            	currentStepIndex++;
                startNextStep(); 
                return;
            }
            
            if (keyCode == expectedKeyCode) {
                handleGameInput(keyCode); 
                waitingForInput = false;
                hideGuide();
                isPaused = false;
            }
        }
    }

    /**
     * 실제 게임 동작 처리 (점프, 공격 등)
     */
    private void handleGameInput(int keyCode) {
        if (currentRole == ROLE_HORSE) {
            switch (keyCode) {
                case KeyEvent.VK_SPACE: performJump(); break; // 슬라이드
                case KeyEvent.VK_SHIFT: performSlide(); break; // 점프
                case KeyEvent.VK_R: activateSkill(5); break; // 돌진
            }
        } else if (currentRole == ROLE_KNIGHT) {
            switch (keyCode) {
                case KeyEvent.VK_Q: activateSkill(0); break; // 외침
                case KeyEvent.VK_W: activateSkill(1); break; // 찌르기
                case KeyEvent.VK_E: activateSkill(2); break; // 베기
            }
        }
    }
    
    private void performJump() {
        if (!isJumping) {
            isJumping = true;
            playerState = 1;
            playerVelocityY = JUMP_VELOCITY;
        }
    }
    
    private void performSlide() {
        isSliding = true;
        playerState = 2;
    }
    
    private void activateSkill(int skillId) {
        long now = System.currentTimeMillis();
        if (now - lastSkillUsedTime[skillId] < SKILL_COOLDOWNS[skillId]) return;
        
        lastSkillUsedTime[skillId] = now;
        activeSkills[skillId] = true;
        skillActiveTimes[skillId] = now;
        
        if (skillId == 5) {
            isInvincible = true;
            invincibleStartTime = now;
            return;
        }
        
        for (TutorialObstacle obs : obstacles) {
            if (!obs.destroyed) {
                boolean inRange = checkSkillRange(skillId, obs);
                boolean canKill = canSkillKillObstacle(skillId, obs.type);
                
                if (inRange && canKill) {
                    obs.destroyed = true;
                }
            }
        }
    }
    
    /**
     * 스킬이 해당 장애물 타입을 처치할 수 있는지 확인
     */
    private boolean canSkillKillObstacle(int skillId, ObstacleType type) {
        switch (skillId) {
            case 0: // 외침 - 모든 몬스터 제거
            	return type == ObstacleType.BOSS_MONSTER || 
                type == ObstacleType.SOFT_MONSTER || 
                type == ObstacleType.HARD_MONSTER;
            case 1: // 찌르기 - 연질 몬스터만
                return type == ObstacleType.SOFT_MONSTER;
            case 2: // 베기 - 경질 몬스터만
                return type == ObstacleType.HARD_MONSTER;
            default:
                return false;
        }
    }
    
    /**
     * 스킬 범위 내에 장애물이 있는지 확인
     */
    private boolean checkSkillRange(int skillId, TutorialObstacle obs) {
        int playerRight = GameConstants.CHARACTER_X + GameConstants.CHARACTER_WIDTH;
        
        switch (skillId) {
            case 0: // 외침 - 전체
                return true;
            case 1: // 찌르기 - 150px 범위
                return obs.x < playerRight + GameConstants.THRUST_RANGE && obs.x + obs.width > playerRight;
            case 2: // 베기 - 100px 범위
                return obs.x < playerRight + GameConstants.SLASH_RANGE && obs.x + obs.width > playerRight;
            case 5: // 돌진 - 전방
                return obs.x > GameConstants.CHARACTER_X;
            default:
                return false;
        }
    }
    
    // ==========================================
    //        게임 루프
    // ==========================================
    
    private void startGameLoop() {
        if (isRunning) return;
        isRunning = true;
        
        gameThread = new Thread(() -> {
            while (isRunning) {
                long startTime = System.currentTimeMillis();
                
                if (!isPaused && !isGameOver) {
                    updateGame();
                }
                
                SwingUtilities.invokeLater(() -> {
                    if (gameCanvas != null) gameCanvas.repaint();
                });
                
                long elapsed = System.currentTimeMillis() - startTime;
                long sleepTime = TARGET_TIME - elapsed;
                if (sleepTime > 0) {
                    try {
                        Thread.sleep(sleepTime);
                    } catch (InterruptedException e) {
                        break;
                    }
                }
            }
        });
        gameThread.start();
    }
    
    private void stopGameLoop() {
        isRunning = false;
    }
    
    private void updateGame() {
        updatePlayer();
        updateObstacles();
        updateSkills();
        updateInvincible();
        checkCollisions();
        
        if (!isFreePlay) {
            checkTutorialTrigger();
        }
    }
    
    private void updatePlayer() {
        if (isJumping) {
            playerVelocityY += GRAVITY;
            playerY += playerVelocityY;
            
            double groundY = GameConstants.GROUND_Y - GameConstants.CHARACTER_HEIGHT;
            if (playerY >= groundY) {
                playerY = groundY;
                playerVelocityY = 0;
                isJumping = false;
                playerState = isSliding ? 2 : 0; // 슬라이드 키 누르고 있으면 슬라이드 상태로
            }
        }
    }
    
    private void updateObstacles() {
        int scrollSpeed = 5;
        
        for (int i = obstacles.size() - 1; i >= 0; i--) {
            TutorialObstacle obs = obstacles.get(i);
            
            if (!obs.destroyed) {
                obs.x -= scrollSpeed;
            } else {
                obs.alpha -= 10;
                obs.x -= scrollSpeed;
            }
            
            if (obs.x + obs.width < -50 || obs.alpha <= 0) {
                obstacles.remove(i);
                
                if (!isFreePlay && !waitingForInput && currentStepIndex < tutorialSteps.size() - 1) {
                    currentStepIndex++;
                    SwingUtilities.invokeLater(this::startNextStep);
                }
            }
        }

        if (isFreePlay) {
            spawnRandomObstacle();
        }
    }
    
    private void updateSkills() {
        long now = System.currentTimeMillis();
        for (int i = 0; i < activeSkills.length; i++) {
            if (activeSkills[i]) {
                if (now - skillActiveTimes[i] > 300) {
                    activeSkills[i] = false;
                }
            }
        }
    }
    
    private void updateInvincible() {
        if (isInvincible) {
            long now = System.currentTimeMillis();
            if (now - invincibleStartTime > INVINCIBLE_DURATION) {
                isInvincible = false;
            }
        }
    }
    
    /**
     * 충돌 감지 - 플레이어와 장애물의 충돌을 확인
     */
    private void checkCollisions() {
    	if (isInvincible) return;
    	
        int playerX = GameConstants.CHARACTER_X;
        int playerWidth = GameConstants.CHARACTER_WIDTH;
        int playerHeight = GameConstants.CHARACTER_HEIGHT;
        int currentPlayerY = (int) playerY;
        
        if (isSliding) {
            int slideHeight = playerHeight / 2;
            currentPlayerY = GameConstants.GROUND_Y - slideHeight;
            playerHeight = slideHeight;
        }
        
        int margin = 10;
        
        for (TutorialObstacle obs : obstacles) {
            if (obs.destroyed) continue;
            
            boolean collisionX = playerX + playerWidth - margin > obs.x && 
                                 playerX + margin < obs.x + obs.width;
            boolean collisionY = currentPlayerY + playerHeight - margin > obs.y && 
                                 currentPlayerY + margin < obs.y + obs.height;
            
            if (collisionX && collisionY) {
                handleCollision(obs);
                return;
            }
        }
    }
    
    /**
     * 충돌 처리 - 게임 오버
     */
    private void handleCollision(TutorialObstacle obs) {
        isGameOver = true;
        isPaused = true;
        
        String obstacleName = obs.type.getDisplayName();
        
        SwingUtilities.invokeLater(() -> {
            showGameOverDialog(obstacleName);
        });
    }
    
    /**
     * 게임 오버 다이얼로그 표시
     */
    private void showGameOverDialog(String obstacleName) {
        JPanel gameOverPanel = new JPanel();
        gameOverPanel.setLayout(new BoxLayout(gameOverPanel, BoxLayout.Y_AXIS));
        gameOverPanel.setBackground(new Color(0, 0, 0, 200));
        gameOverPanel.setBounds(WINDOW_WIDTH / 4, WINDOW_HEIGHT / 3, WINDOW_WIDTH / 2, 200);
        gameOverPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.RED, 3),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        
        JLabel titleLabel = new JLabel("GAME OVER", SwingConstants.CENTER);
        titleLabel.setFont(getFont().deriveFont(Font.BOLD, 36f));
        titleLabel.setForeground(Color.RED);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        gameOverPanel.add(titleLabel);
        
        gameOverPanel.add(Box.createVerticalStrut(10));
        
        JLabel messageLabel = new JLabel(obstacleName + "에 충돌했습니다!", SwingConstants.CENTER);
        messageLabel.setFont(getFont().deriveFont(18f));
        messageLabel.setForeground(WHITE);
        messageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        gameOverPanel.add(messageLabel);
        
        gameOverPanel.add(Box.createVerticalStrut(20));
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 0));
        
        Button retryBtn = new Button("다시 시도");
        retryBtn.setFont(getFont().deriveFont(Font.BOLD, 16f));
        retryBtn.setPreferredSize(new Dimension(120, 40));
        retryBtn.addActionListener(e -> {
            gamePanel.remove(gameOverPanel);
            restartTutorial();
        });
        buttonPanel.add(retryBtn);
        
        Button exitBtn = new Button("나가기");
        exitBtn.setFont(getFont().deriveFont(Font.BOLD, 16f));
        exitBtn.setPreferredSize(new Dimension(120, 40));
        exitBtn.addActionListener(e -> {
            stopGameLoop();
            goBack();
        });
        buttonPanel.add(exitBtn);
        
        gameOverPanel.add(buttonPanel);
        
        gamePanel.add(gameOverPanel, Integer.valueOf(javax.swing.JLayeredPane.MODAL_LAYER));
        gamePanel.revalidate();
        gamePanel.repaint();
    }
    
    /**
     * 튜토리얼 재시작
     */
    private void restartTutorial() {
        isGameOver = false;
        isPaused = false;
        isFreePlay = false;
        currentStepIndex = 0;
        waitingForInput = false;
        
        playerY = GameConstants.GROUND_Y - GameConstants.CHARACTER_HEIGHT;
        playerVelocityY = 0;
        isJumping = false;
        isSliding = false;
        playerState = 0;
        
        obstacles.clear();
        
        for (int i = 0; i < activeSkills.length; i++) {
            activeSkills[i] = false;
            lastSkillUsedTime[i] = 0;
        }
        
        initTutorialSteps();
        startNextStep();
        
        requestFocusInWindow();
    }
    
    private void checkTutorialTrigger() {
        if (waitingForInput || currentStepIndex >= tutorialSteps.size()) return;
        
        TutorialStep step = tutorialSteps.get(currentStepIndex);
        if (step.obstacleType == null) return; 
        
        for (TutorialObstacle obs : obstacles) {
            if (!obs.destroyed) {
                int triggerX = GameConstants.CHARACTER_X + GameConstants.CHARACTER_WIDTH + 50;
                
                if (obs.x <= triggerX && obs.x > triggerX - 10) {
                    isPaused = true;
                    waitingForInput = true;
                    expectedKeyCode = step.keyCode;
                    showGuide(step.guideText);
                    break;
                }
            }
        }
    }
    
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(TRANSLUCENT_WHITE);
        g.fillRect(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);
    }
    
    // ==========================================
    //        게임 캔버스
    // ==========================================
    
    private class TutorialCanvas extends JPanel {
        private static final long serialVersionUID = 1L;
        
        public TutorialCanvas() {
            setOpaque(false);
            setLayout(null);
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            g2d.setColor(new Color(100, 80, 60));
            g2d.fillRect(0, GameConstants.GROUND_Y, WINDOW_WIDTH, 5);
            
            drawSkillRanges(g2d);
            
            for (TutorialObstacle obs : obstacles) {
                obs.draw(g2d);
            }
            
            drawPlayer(g2d);
            
            if (isPaused && waitingForInput) {
                g2d.setColor(new Color(0, 0, 0, 100));
                g2d.fillRect(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);
            }
            
            if (isGameOver) {
                g2d.setColor(new Color(255, 0, 0, 100));
                g2d.setStroke(new BasicStroke(10));
                g2d.drawRect(5, 5, WINDOW_WIDTH - 10, WINDOW_HEIGHT - 10);
            }
        }
        
        private void drawPlayer(Graphics2D g2d) {
            int x = GameConstants.CHARACTER_X;
            int y = (int) playerY;
            int width = GameConstants.CHARACTER_WIDTH;
            int height = GameConstants.CHARACTER_HEIGHT;
            
            if (isSliding) {
                int slideHeight = height / 2;
                y = GameConstants.GROUND_Y - slideHeight;
                height = slideHeight;
            }
            
            Image playerImg = ResourceManager.getImage("player");
            if (playerImg != null) {
                g2d.drawImage(playerImg, x, y, width, height, null);
            } else {
                g2d.setColor(currentRole == ROLE_KNIGHT ? new Color(200, 150, 100) : new Color(150, 180, 220));
                g2d.fillRoundRect(x, y, width, height, 10, 10);
                
                g2d.setColor(BLACK);
                g2d.setStroke(new BasicStroke(2));
                g2d.drawRoundRect(x, y, width, height, 10, 10);
                
                g2d.setFont(getFont().deriveFont(Font.BOLD, 14f));
                String label = currentRole == ROLE_KNIGHT ? "기사" : "말";
                FontMetrics fm = g2d.getFontMetrics();
                int textX = x + (width - fm.stringWidth(label)) / 2;
                int textY = y + height / 2 + fm.getAscent() / 2;
                g2d.drawString(label, textX, textY);
            }
            
            if (isJumping) {
                g2d.setColor(new Color(255, 200, 0, 150));
                g2d.fillOval(x - 5, y + height - 10, width + 10, 20);
            }
            if (isSliding) {
                g2d.setColor(new Color(100, 200, 255, 150));
                g2d.fillRect(x - 5, y + height - 5, width + 30, 10);
            }
            if (isInvincible) {
                g2d.setColor(new Color(255, 200, 0, 100));
                g2d.setStroke(new BasicStroke(3));
                g2d.drawRoundRect(x - 3, y - 3, width + 6, height + 6, 15, 15);
            }
        }
        
        private void drawSkillRanges(Graphics2D g2d) {
            int playerRight = GameConstants.CHARACTER_X + GameConstants.CHARACTER_WIDTH;
            int playerHeight = GameConstants.CHARACTER_HEIGHT;
            
            if (activeSkills[0]) { // 외침
                g2d.setColor(new Color(255, 200, 0, 50));
                g2d.fillRect(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);
                g2d.setColor(new Color(255, 200, 0, 200));
                g2d.setStroke(new BasicStroke(3));
                g2d.drawRect(5, 5, WINDOW_WIDTH - 10, WINDOW_HEIGHT - 10);
            }
            if (activeSkills[1]) { // 찌르기
                g2d.setColor(new Color(255, 0, 0, 80));
                g2d.fillRect(playerRight, (int) playerY, GameConstants.THRUST_RANGE, playerHeight);
            }
            if (activeSkills[2]) { // 베기
                g2d.setColor(new Color(0, 150, 255, 80));
                g2d.fillRect(playerRight, (int) playerY, GameConstants.SLASH_RANGE, playerHeight);
            }
            if (activeSkills[5]) { // 돌진
                g2d.setColor(new Color(255, 100, 0, 100));
                g2d.fillRect(playerRight, (int) playerY - 20, WINDOW_WIDTH - playerRight, playerHeight + 40);
            }
        }
    }
    
    private class TutorialObstacle {
        ObstacleType type;
        int x, y;
        int width, height;
        boolean destroyed = false;
        int alpha = 255;
        
        TutorialObstacle(ObstacleType type, int startX) {
            this.type = type;
            this.x = startX;
            this.width = type.getWidth();
            this.height = type.getHeight();
            this.y = type.getY();
            
            if (type == ObstacleType.AIR_OBSTACLE) {
                this.y += 20; 
            }
        }
        
        void draw(Graphics2D g2d) {
            if (alpha <= 0) return;
            
            GameEntity entity = GameEntity.getByType(type);
            Composite originalComposite = g2d.getComposite();
            
            if (destroyed) {
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha / 255f));
            }
            
            Image img = null;
            if (entity != null) img = ResourceManager.getImage(entity.name());
            
            if (img != null) {
                g2d.drawImage(img, x, y, width, height, null);
            } else {
                Color color = entity != null ? entity.getDefaultColor() : GRAY;
                g2d.setColor(color);
                g2d.fillRect(x, y, width, height);
                g2d.setColor(BLACK);
                g2d.drawRect(x, y, width, height);
                
                String name = type.getDisplayName();
                g2d.setFont(getFont().deriveFont(12f));
                g2d.setColor(WHITE);
                FontMetrics fm = g2d.getFontMetrics();
                g2d.drawString(name, x + (width - fm.stringWidth(name)) / 2, y + height / 2 + fm.getAscent() / 2);
            }
            
            if (destroyed) {
                g2d.setComposite(originalComposite);
                g2d.setColor(Color.RED);
                g2d.setStroke(new BasicStroke(4));
                g2d.drawLine(x, y, x + width, y + height);
                g2d.drawLine(x + width, y, x, y + height);
            }
            g2d.setComposite(originalComposite);
        }
    }
    
    private static class TutorialStep {
        enum Action { JUMP, SLIDE, RUSH, THRUST, SLASH, SHOUT, COMPLETE }
        String name, guideText;
        int keyCode;
        ObstacleType obstacleType;
        Action action;
        
        TutorialStep(String name, String guideText, int keyCode, ObstacleType obstacleType, Action action) {
            this.name = name;
            this.guideText = guideText;
            this.keyCode = keyCode;
            this.obstacleType = obstacleType;
            this.action = action;
        }
    }
}