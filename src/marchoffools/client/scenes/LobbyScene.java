package marchoffools.client.scenes;

import static marchoffools.client.core.Assets.Backgrounds.DEFAULT;
import static marchoffools.client.core.Assets.Colors.*;
import static marchoffools.client.core.Config.*;
import static marchoffools.common.message.RoomActionMessage.*;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.IOException;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

import marchoffools.client.core.Assets;
import marchoffools.client.core.ResourceManager;
import marchoffools.client.core.Scene;
import marchoffools.client.network.NetworkManager;
import marchoffools.client.network.NetworkListener;
import marchoffools.client.ui.Button;
import marchoffools.client.ui.RatioLayout;
import marchoffools.common.message.ChatMessage;
import marchoffools.common.message.RoomActionMessage;
import marchoffools.common.message.RoomInfoMessage;
import marchoffools.common.model.GameModeType;
import marchoffools.common.model.PlayerInfo;
import marchoffools.common.protocol.MessageType;

/**
 * 대기실 화면 (LobbyScene with RoomScene Design)
 */
public class LobbyScene extends Scene implements NetworkListener {

    private static final long serialVersionUID = 1L;

    // --- 데이터 ---
    private String roomId;
    private String hostId;
    private List<PlayerInfo> players;
    private boolean canStart;
    
    private boolean isReady = false;
    private int myRole = ROLE_NONE;

    // --- UI 컴포넌트 ---
    private JTextPane tChatPane;
    private JTextField tChatInput;
    private HTMLEditorKit kit;
    private HTMLDocument doc;

    private Button bReady;      // 준비 버튼
    private Button bStart;      // 게임 시작 버튼 (방장용)
    private JLabel lRoomIdValue;
    private JPanel pRoomIdContent, pHiddenIndicator;
    private boolean roomIdVisible = false;
    
    // 플레이어 슬롯 패널 
    private PlayerSlot slot1;
    private PlayerSlot slot2;

    // 역할 선택 버튼 
    private JPanel roleSelectionPanel;
    private Button bSelectKnight;
    private Button bSelectHorse;
    
    // 캐릭터 선택 버튼
    private JPanel characterSelectionPanel;
    private Button bSelectCharacter;
    
    private JPanel characterSelectionOverlay;
    private JPanel rightPanel;
    
    private JLayeredPane layeredPane;
    private JPanel mainLayerPanel;
    
    private GameModeType selectedGameMode = GameModeType.INFINITE;
    private Button bModeInfinite;
    private Button bModeStage;
    
    private static final String[] KNIGHT_NAMES = {"Warrior", "Archer", "Axe"};
    private static final String[] KNIGHT_DESCS = {"강력한 근접 공격", "다양한 마법 스킬", "원거리 제압 특화"};

    private static final String[] HORSE_NAMES = {"Unicorn", "Griffin", "Dragon"};
    private static final String[] HORSE_DESCS = {"이동 속도 보너스", "높은 체력과 방어력", "특수 점프 능력"};

    private static final javax.swing.border.Border SELECTED_BORDER = BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BLUE, 3),
            BorderFactory.createEmptyBorder(14, 14, 14, 14)
        );

        private static final javax.swing.border.Border UNSELECTED_BORDER = BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(LIGHT_GRAY, 2),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        );

        private static final Color SELECTED_BG = new Color(220, 240, 255);
        private static final Color HOVER_BG = new Color(245, 245, 245);
        private static final Color NORMAL_BG = WHITE;
        
    public LobbyScene() {
        super(DEFAULT); // 배경 이미지 설정 (RoomScene 스타일)
        
        setLayout(new BorderLayout());
        
        layeredPane = new JLayeredPane();
        layeredPane.setBounds(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);
        add(layeredPane, BorderLayout.CENTER);
        
        // 기본 UI들이 담길 패널 생성
        mainLayerPanel = new JPanel(null); // 절대 좌표 사용을 위해 null layout
        mainLayerPanel.setOpaque(false);
        mainLayerPanel.setBounds(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);
        
        // mainLayerPanel을 가장 아래 레이어에 추가
        layeredPane.add(mainLayerPanel, JLayeredPane.DEFAULT_LAYER);
        
        // 전체 레이아웃 구성
        createTitleSection();
        createExitButton();
        createMainContent();
        
        System.out.println("LobbyScene (Redesigned) initialized");
    }

    // ==========================================
    //              UI 초기화 섹션
    // ==========================================
    
    private void createTitleSection() {
        JLabel lTitle = new JLabel("대기실");
        lTitle.setFont(getFont().deriveFont(42f));
        lTitle.setForeground(BLACK);
        lTitle.setSize(lTitle.getPreferredSize().width + 42, lTitle.getPreferredSize().height);
        lTitle.setLocation(72, 48);
        mainLayerPanel.add(lTitle);
    }
    
    private void createExitButton() {
        Button bExit = new Button("->");
        bExit.setFont(getFont().deriveFont(Font.BOLD, 30f));
        bExit.setSize(100, 50); 
        bExit.setLocation(WINDOW_WIDTH - bExit.getWidth() - 72, 40);
        bExit.addActionListener(e -> {
            handleExit(); 
        });
        mainLayerPanel.add(bExit);
    }
    
    private void createMainContent() {
        int contentMargin = 72;
        int topMargin = 130;
        int bottomMargin = 70;
        
        int contentW = WINDOW_WIDTH - (contentMargin * 2);
        int contentH = WINDOW_HEIGHT - topMargin - bottomMargin;
        
        JPanel mainContentPanel = new JPanel(new RatioLayout(RatioLayout.HORIZONTAL, 48));
        mainContentPanel.setOpaque(false);
        mainContentPanel.setBounds(contentMargin, topMargin, contentW, contentH);
        
        // 왼쪽: 채팅 로그
        mainContentPanel.add(createChatLogPanel(), Integer.valueOf(8));
        // 오른쪽: 정보 및 액션 버튼
        rightPanel = createRightPanel();
        mainContentPanel.add(rightPanel, Integer.valueOf(3));
        
        mainLayerPanel.add(mainContentPanel);
    }
    
    private void applySectionStyle(JPanel panel) {
        panel.setOpaque(true);
        panel.setBackground(WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(16, 24, 16, 24));
    }

    // ==========================================
    //              LEFT PANEL: 채팅
    // ==========================================

    private JPanel createChatLogPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        applySectionStyle(panel);
        
        panel.add(createChatArea(), BorderLayout.CENTER);
        panel.add(createChatInputPanel(), BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JScrollPane createChatArea() {
        tChatPane = new JTextPane();
        tChatPane.setEditable(false);
        tChatPane.setContentType("text/html");
        tChatPane.setBackground(WHITE);
        tChatPane.setFocusable(false);
        
        kit = new HTMLEditorKit();
        tChatPane.setEditorKit(kit);
        doc = new HTMLDocument();
        tChatPane.setDocument(doc);
        
        try {
            kit.insertHTML(doc, doc.getLength(), "<style>body { font-family: SansSerif; font-size: 12px; }</style>", 0, 0, null);
        } catch (Exception e) {
            e.printStackTrace();
        }

        JScrollPane scrollPane = new JScrollPane(tChatPane);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setOpaque(false);
        
        return scrollPane;
    }
    
    private JPanel createChatInputPanel() {
        JPanel pInput = new JPanel(new BorderLayout(8, 0));
        pInput.setOpaque(false);
        
        tChatInput = new JTextField("메시지 보내기...");
        tChatInput.setFont(getFont().deriveFont(16f));
        tChatInput.setForeground(GRAY); 
        tChatInput.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(LIGHT_GRAY, 2),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        
        tChatInput.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent evt) {
                if (tChatInput.getText().equals("메시지 보내기...")) {
                    tChatInput.setText("");
                    tChatInput.setForeground(BLACK);
                }
            }
            @Override
            public void focusLost(FocusEvent evt) {
                if (tChatInput.getText().isEmpty()) {
                    tChatInput.setText("메시지 보내기...");
                    tChatInput.setForeground(GRAY);
                }
            }
        });
        tChatInput.addActionListener(e -> sendChat());
        pInput.add(tChatInput, BorderLayout.CENTER);
        
        Button bSend = createSendButton();
        pInput.add(bSend, BorderLayout.EAST);
        
        return pInput;
    }
    
    private Button createSendButton() {
        Button bSend = new Button(">"); 
        bSend.setFont(getFont().deriveFont(Font.BOLD, 20f));
        bSend.setPreferredSize(new Dimension(60, 40));
        bSend.setButtonColors(LIGHT_GRAY, WHITE, GRAY);
        bSend.addActionListener(e -> sendChat());
        return bSend;
    }

    // ==========================================
    //            RIGHT PANEL: 정보/액션
    // ==========================================

    private JPanel createRightPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new RatioLayout(RatioLayout.VERTICAL, 15));
        panel.setOpaque(false);

        panel.add(createPlayerSection(), Integer.valueOf(40));
        panel.add(createGameModeSection(), Integer.valueOf(15));
        panel.add(createRoomIdSection(), Integer.valueOf(15));
        panel.add(createActionButtonsSection(), Integer.valueOf(30));

        return panel;
    }
    
    private JPanel createPlayerSection() {
        JPanel section = new JPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setAlignmentX(LEFT_ALIGNMENT);
        applySectionStyle(section);

        JLabel lPlayerTitle = new JLabel("Players & Role");
        lPlayerTitle.setFont(getFont().deriveFont(Font.BOLD, 20f));
        lPlayerTitle.setForeground(BLACK);
        lPlayerTitle.setAlignmentX(LEFT_ALIGNMENT);
        section.add(lPlayerTitle);
        section.add(Box.createVerticalStrut(15));

        // 플레이어 1 슬롯
        slot1 = new PlayerSlot();
        section.add(slot1.getPanel()); // getPanel()로 JPanel을 꺼내서 붙임
        
        section.add(Box.createVerticalStrut(10));

        // 플레이어 2 슬롯
        slot2 = new PlayerSlot();
        section.add(slot2.getPanel());
        
        section.add(Box.createVerticalStrut(15));
        
        // 역할 선택 버튼들
        roleSelectionPanel = createRoleSelectionPanel();
        section.add(roleSelectionPanel);
        
        section.add(Box.createVerticalStrut(10));
        
        // 캐릭터 선택 버튼
        characterSelectionPanel = createCharacterSelectionPanel();
        section.add(characterSelectionPanel);

        return section;
    }
    
    private JPanel createRoleSelectionPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 0));
        panel.setOpaque(false);
        panel.setMaximumSize(new Dimension(Short.MAX_VALUE, 40));
        panel.setAlignmentX(LEFT_ALIGNMENT);
        
        bSelectKnight = new Button("기사 (Knight)");
        bSelectKnight.setFont(getFont().deriveFont(14f));
        bSelectKnight.setButtonColors(LIGHT_GRAY, WHITE, GRAY);
        bSelectKnight.addActionListener(e -> handleRoleSelect(ROLE_KNIGHT));
        
        bSelectHorse = new Button("말 (Horse)");
        bSelectHorse.setFont(getFont().deriveFont(14f));
        bSelectHorse.setButtonColors(LIGHT_GRAY, WHITE, GRAY);
        bSelectHorse.addActionListener(e -> handleRoleSelect(ROLE_HORSE));
        
        JPanel grid = new JPanel(new java.awt.GridLayout(1, 2, 10, 0));
        grid.setOpaque(false);
        grid.add(bSelectKnight);
        grid.add(bSelectHorse);
        
        panel.add(grid, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createCharacterSelectionPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setMaximumSize(new Dimension(Short.MAX_VALUE, 40)); // 높이 고정
        panel.setAlignmentX(LEFT_ALIGNMENT);

        bSelectCharacter = new Button("캐릭터 선택");
        bSelectCharacter.setFont(getFont().deriveFont(16f));
        
        bSelectCharacter.setEnabled(false);
        bSelectCharacter.setButtonColors(GRAY, GRAY, GRAY);
        
        // 클릭 리스너 (나중에 구현)
        bSelectCharacter.addActionListener(e -> {
            System.out.println("캐릭터 선택 버튼 클릭됨");
            showCharacterSelectionOverlay();
        });
        
        panel.add(bSelectCharacter, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createGameModeSection() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        applySectionStyle(panel);
        
        // 제목
        JLabel lTitle = new JLabel("게임 모드");
        lTitle.setFont(getFont().deriveFont(Font.BOLD, 14f));
        lTitle.setForeground(BLACK);
        panel.add(lTitle, BorderLayout.NORTH);
        
        // 버튼 패널
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        buttonPanel.setOpaque(false);
        
        // 무한 모드 버튼
        bModeInfinite = new Button("무한");
        bModeInfinite.setFont(getFont().deriveFont(12f));
        bModeInfinite.setPreferredSize(new Dimension(80, 35));
        bModeInfinite.addActionListener(e -> selectGameMode(GameModeType.INFINITE));
        buttonPanel.add(bModeInfinite);
        
        // 스테이지 모드 버튼
        bModeStage = new Button("스테이지");
        bModeStage.setFont(getFont().deriveFont(12f));
        bModeStage.setPreferredSize(new Dimension(80, 35));
        bModeStage.addActionListener(e -> selectGameMode(GameModeType.STAGE));
        buttonPanel.add(bModeStage);
        
        panel.add(buttonPanel, BorderLayout.CENTER);
        
        // 초기 상태 설정
        updateGameModeButtons();
        
        return panel;
    }

    private void selectGameMode(GameModeType mode) {
        this.selectedGameMode = mode;
        updateGameModeButtons();
        
        NetworkManager nm = getNetworkManager();
        if (nm != null) {
            RoomActionMessage msg = new RoomActionMessage(nm.getPlayerId(), RoomActionMessage.SELECT_MODE);
            msg.setGameMode(mode);
            nm.sendMessage(MessageType.ROOM_ACTION, msg);
        }
        
        System.out.println("게임 모드 선택: " + mode.getDisplayName());
    }

    private void updateGameModeButtons() {
        if (selectedGameMode == GameModeType.INFINITE) {
            bModeInfinite.setButtonColors(BLUE, BLUE, BLUE_PRESSED);
            bModeInfinite.setForeground(WHITE);
            bModeStage.setButtonColors(LIGHT_GRAY, WHITE, GRAY);
            bModeStage.setForeground(BLACK);
        } else {
            bModeInfinite.setButtonColors(LIGHT_GRAY, WHITE, GRAY);
            bModeInfinite.setForeground(BLACK);
            bModeStage.setButtonColors(BLUE, BLUE, BLUE_PRESSED);
            bModeStage.setForeground(WHITE);
        }
    }

    private JPanel createRoomIdSection() {
        JPanel section = new JPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setAlignmentX(LEFT_ALIGNMENT);
        applySectionStyle(section);

        JPanel pHeader = new JPanel(new BorderLayout());
        pHeader.setOpaque(false);
        pHeader.setMaximumSize(new Dimension(Short.MAX_VALUE, 30));
        pHeader.setAlignmentX(LEFT_ALIGNMENT);
        
        JLabel lRoomIdTitle = new JLabel("Room ID");
        lRoomIdTitle.setFont(getFont().deriveFont(Font.BOLD, 20f));
        lRoomIdTitle.setForeground(BLACK);
        pHeader.add(lRoomIdTitle, BorderLayout.WEST);
        
        // 토글 버튼
        Button bToggle = new Button("○");
        bToggle.setFont(getFont().deriveFont(14f));
        bToggle.setPreferredSize(new Dimension(40, 30));
        bToggle.setButtonColors(LIGHT_GRAY, WHITE, GRAY);
        bToggle.addActionListener(e -> {
            roomIdVisible = !roomIdVisible;
            pRoomIdContent.setVisible(roomIdVisible);
            pHiddenIndicator.setVisible(!roomIdVisible);
            bToggle.setText(roomIdVisible ? "●" : "○");
            section.revalidate();
            section.repaint();
        });
        pHeader.add(bToggle, BorderLayout.EAST);
        
        section.add(pHeader);
        section.add(Box.createVerticalStrut(10));

        // ID 표시 영역
        JPanel centerContainer = new JPanel();
        centerContainer.setLayout(new BoxLayout(centerContainer, BoxLayout.Y_AXIS));
        centerContainer.setOpaque(false);
        centerContainer.setAlignmentX(LEFT_ALIGNMENT);

        pRoomIdContent = new JPanel();
        pRoomIdContent.setLayout(new BoxLayout(pRoomIdContent, BoxLayout.Y_AXIS));
        pRoomIdContent.setOpaque(false);
        pRoomIdContent.setVisible(false);
        
        lRoomIdValue = new JLabel("------");
        lRoomIdValue.setFont(getFont().deriveFont(Font.BOLD, 20f));
        lRoomIdValue.setForeground(BLACK);
        lRoomIdValue.setAlignmentX(CENTER_ALIGNMENT);
        pRoomIdContent.add(lRoomIdValue);
        
        // 가려짐 표시
        pHiddenIndicator = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(GRAY);
                int y = getHeight() / 2;
                g.drawLine(20, y, getWidth() - 20, y);
            }
        };
        pHiddenIndicator.setOpaque(false);
        pHiddenIndicator.setVisible(true);
        pHiddenIndicator.setPreferredSize(new Dimension(100, 32));
        
        centerContainer.add(pRoomIdContent);
        centerContainer.add(pHiddenIndicator);
        section.add(centerContainer);

        return section;
    }
    
    private JPanel createActionButtonsSection() {
        JPanel section = new JPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setAlignmentX(LEFT_ALIGNMENT);
        applySectionStyle(section);

        // 준비 버튼
        bReady = new Button("Ready");
        bReady.setFont(getFont().deriveFont(Font.BOLD, 28f));
        bReady.setForeground(WHITE);
        bReady.setButtonColors(LIGHT_GRAY, LIGHT_GRAY, LIGHT_GRAY.darker());
        bReady.setMinimumSize(new Dimension(100, 60));
        bReady.setMaximumSize(new Dimension(Short.MAX_VALUE, 60));
        bReady.setPreferredSize(new Dimension(100, 80));
        bReady.setBorder(BorderFactory.createLineBorder(GRAY, 2));
        bReady.addActionListener(e -> handleReady());
        bReady.setAlignmentX(CENTER_ALIGNMENT);
        
        section.add(bReady);
        
        // 시작 버튼 (초기엔 숨김/비활성 처리 가능하지만 여기선 보이되 비활성화)
        bStart = new Button("Start Game");
        bStart.setFont(getFont().deriveFont(Font.BOLD, 24f));
        bStart.setButtonColors(BLUE, BLUE.brighter(), BLUE_PRESSED);
        bStart.setForeground(WHITE);
        bStart.setMaximumSize(new Dimension(Short.MAX_VALUE, 45));
        bStart.setPreferredSize(new Dimension(100, 45));
        bStart.setVisible(false); // 방장만 보임
        bStart.addActionListener(e -> handleStart());
        
        section.add(Box.createVerticalStrut(10));
        section.add(bStart);

        return section;
    }
    
    // ==========================================
    //      캐릭터 선택 오버레이 패널
    // ==========================================
    
    /**
     * 캐릭터 선택 오버레이 표시
     */
    private void showCharacterSelectionOverlay() {
    	if (characterSelectionOverlay != null) return;
    	
    	String[] targetNames;
        String[] targetDescs;
        String[] targetImages;
        String roleTitle;

        if (myRole == ROLE_KNIGHT) {
            targetNames = KNIGHT_NAMES;
            targetDescs = KNIGHT_DESCS;
            targetImages = new String[]{"knight_warrior", "knight_archer", "knight_axe"};
            roleTitle = "기사(Knight) 캐릭터 선택";
        } else if (myRole == ROLE_HORSE) {
            targetNames = HORSE_NAMES;
            targetDescs = HORSE_DESCS;
            targetImages = new String[]{"horse_unicorn", "horse_griffin", "horse_dragon"};
            roleTitle = "말(Horse) 캐릭터 선택";
        } else {
            JOptionPane.showMessageDialog(this, "먼저 역할을 선택해주세요.");
            return;
        }
        
    	setRightPanelButtonsEnabled(false);
        
        JPanel mainContentPanel = (JPanel) rightPanel.getParent();
        int mainX = mainContentPanel.getX();
        int mainY = mainContentPanel.getY();
        
        int relativeX = rightPanel.getX();
        int relativeY = rightPanel.getY();
        
        int x = mainX + relativeX;
        int y = mainY + relativeY;
        int width = rightPanel.getWidth();
        int height = rightPanel.getHeight();
        
        // 오버레이 패널 생성
        characterSelectionOverlay = new JPanel(new BorderLayout(0, 15));
        characterSelectionOverlay.setBackground(WHITE);
        characterSelectionOverlay.setOpaque(true);
        characterSelectionOverlay.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BLUE, 3),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        characterSelectionOverlay.setBounds(x, y, width, height);
        
        // 제목
        JLabel titleLabel = new JLabel(roleTitle, SwingConstants.CENTER);
        titleLabel.setFont(getFont().deriveFont(Font.BOLD, 22f));
        titleLabel.setForeground(BLACK);
        characterSelectionOverlay.add(titleLabel, BorderLayout.NORTH);
        
        // 캐릭터 리스트 패널
        JPanel characterListPanel = new JPanel();
        characterListPanel.setLayout(new BoxLayout(characterListPanel, BoxLayout.Y_AXIS));
        characterListPanel.setBackground(WHITE);
        
        // 선택된 캐릭터 추적
        final int[] selectedCharacterIndex = {-1};
        
        JPanel[] characterPanels = new JPanel[targetNames.length];
        
        for (int i = 0; i < targetNames.length; i++) {
            // 1. 패널 생성
            JPanel characterPanel = createCharacterItemPanel(targetNames[i], targetDescs[i], targetImages[i], i);
            characterPanels[i] = characterPanel;
            
            // 2. 초기 스타일 적용 (선택 안됨, 호버 아님)
            updateItemStyle(characterPanel, false, false);
            
            // 3. [최적화] 익명 클래스 대신 명시적 리스너 클래스 사용
            characterPanel.addMouseListener(
                new CharacterItemListener(i, characterPanel, selectedCharacterIndex, characterPanels)
            );
            
            characterListPanel.add(characterPanel);
            characterListPanel.add(Box.createVerticalStrut(10));
        }
        
        // 스크롤 패널
        JScrollPane scrollPane = new JScrollPane(characterListPanel);
        scrollPane.setBorder(BorderFactory.createLineBorder(LIGHT_GRAY, 1));
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        characterSelectionOverlay.add(scrollPane, BorderLayout.CENTER);
        
        // 버튼 패널
        JPanel buttonPanel = new JPanel(new BorderLayout(10, 0));
        buttonPanel.setOpaque(false);
        
        Button cancelButton = new Button("취소");
        cancelButton.setFont(getFont().deriveFont(16f));
        cancelButton.setButtonColors(LIGHT_GRAY, WHITE, GRAY);
        // 취소 버튼 - 오버레이 제거
        cancelButton.addActionListener(e -> hideCharacterSelectionOverlay());
        
        Button confirmButton = new Button("선택 완료");
        confirmButton.setFont(getFont().deriveFont(Font.BOLD, 16f));
        confirmButton.setButtonColors(BLUE, WHITE, new Color(0, 100, 200));
        confirmButton.addActionListener(e -> {
            if (selectedCharacterIndex[0] >= 0) {
                // 캐릭터 선택 완료 처리
                System.out.println("선택된 캐릭터: " + targetNames[selectedCharacterIndex[0]]);
                // TODO: 서버에 캐릭터 선택 정보 전송
                // 선택 완료 후 오버레이 제거
                hideCharacterSelectionOverlay();
            } else {
                JOptionPane.showMessageDialog(LobbyScene.this, "캐릭터를 선택해주세요!", "알림", JOptionPane.WARNING_MESSAGE);
            }
        });
        
        buttonPanel.add(cancelButton, BorderLayout.WEST);
        buttonPanel.add(confirmButton, BorderLayout.EAST);
        characterSelectionOverlay.add(buttonPanel, BorderLayout.SOUTH);
        
        layeredPane.add(characterSelectionOverlay, JLayeredPane.POPUP_LAYER);
        
        revalidate();
        repaint();
    }
    
    /**
     * 캐릭터 선택 오버레이 숨기기
     */
    private void hideCharacterSelectionOverlay() {
        if (characterSelectionOverlay != null) {
        	layeredPane.remove(characterSelectionOverlay);
            characterSelectionOverlay = null;
            
            setRightPanelButtonsEnabled(true);
            
            revalidate();
            repaint();
        }
    }
    
    private void updateItemStyle(JPanel panel, boolean isSelected, boolean isHover) {
        if (isSelected) {
            panel.setBackground(SELECTED_BG);
            panel.setBorder(SELECTED_BORDER);
        } else if (isHover) {
            panel.setBackground(HOVER_BG);
            panel.setBorder(UNSELECTED_BORDER);
        } else {
            panel.setBackground(NORMAL_BG);
            panel.setBorder(UNSELECTED_BORDER);
        }
    }
    
    /**
     * 캐릭터 아이템 패널 생성
     */
    private JPanel createCharacterItemPanel(String characterName, String descText, String imageKey, int index) {
        JPanel panel = new JPanel(new BorderLayout(15, 0));
        panel.setBackground(WHITE);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        panel.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        
        // 이미지 영역 
        JLabel imageLabel = new JLabel();
        imageLabel.setPreferredSize(new Dimension(80, 80));
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        imageLabel.setOpaque(true);
        imageLabel.setBackground(LIGHT_GRAY); // 이미지가 없을 때를 대비한 배경색

        ImageIcon icon = ResourceManager.getScaledIcon(imageKey, 80, 80);
        if (icon != null) {
            imageLabel.setIcon(icon);
            imageLabel.setText("");
        } else {
            imageLabel.setText("No IMG");
        }
        
        panel.add(imageLabel, BorderLayout.WEST);
        
        // 캐릭터 정보 영역
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);
        
        JLabel nameLabel = new JLabel(characterName);
        nameLabel.setFont(getFont().deriveFont(Font.BOLD, 18f));
        nameLabel.setForeground(BLACK);
        nameLabel.setAlignmentX(LEFT_ALIGNMENT);
        
        // 넘겨받은 설명 텍스트 적용
        JLabel descLabel = new JLabel("<html>" + descText + "</html>");
        descLabel.setFont(getFont().deriveFont(14f));
        descLabel.setForeground(GRAY);
        descLabel.setAlignmentX(LEFT_ALIGNMENT);
        
        infoPanel.add(nameLabel);
        infoPanel.add(Box.createVerticalStrut(5));
        infoPanel.add(descLabel);
        
        panel.add(infoPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * 오른쪽 패널의 모든 버튼 활성화/비활성화
     */
    private void setRightPanelButtonsEnabled(boolean enabled) {
        NetworkManager nm = getNetworkManager();
        String myId = (nm != null) ? nm.getPlayerId() : null;
        boolean isHost = (myId != null && hostId != null) && myId.equals(hostId);
        
        // 역할 선택 버튼
        if (bSelectKnight != null) {
            boolean canSelectKnight = myRole == ROLE_KNIGHT 
                                   || (myRole == ROLE_NONE && !isRoleAlreadyTaken(ROLE_KNIGHT)); // 또는 선택 가능
            bSelectKnight.setEnabled(enabled && canSelectKnight);
        }
        if (bSelectHorse != null) {
            boolean canSelectHorse = myRole == ROLE_HORSE  
                                  || (myRole == ROLE_NONE && !isRoleAlreadyTaken(ROLE_HORSE)); // 또는 선택 가능
            bSelectHorse.setEnabled(enabled && canSelectHorse);
        }
        
        // 캐릭터 선택 버튼
        if (bSelectCharacter != null) {
            bSelectCharacter.setEnabled(enabled && myRole != ROLE_NONE);
        }
        
        // 준비 버튼
        if (bReady != null) {
            bReady.setEnabled(enabled);
        }
        
        // 시작 버튼
        if (bStart != null) {
            bStart.setEnabled(enabled && isHost && canStart);
        }
    }
    
    private boolean isRoleAlreadyTaken(int role) {
        if (players == null) return false;
        NetworkManager nm = getNetworkManager();
        if (nm == null) return false;
        
        String myId = nm.getPlayerId();
        for (PlayerInfo p : players) {
            if (!p.getPlayerId().equals(myId) && p.getRole() == role) {
                return true;
            }
        }
        return false;
    }

    // ==========================================
    //              메시지 처리 로직
    // ==========================================

    /**
     * 서버로부터 RoomInfoMessage 수신 시 호출
     */
    public void updateRoomInfo(RoomInfoMessage msg) {
    	if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(() -> updateRoomInfo(msg));
            return;
        }
    	
        this.roomId = msg.getRoomId();
        this.hostId = msg.getHostId();
        this.players = msg.getPlayers();
        this.canStart = msg.isCanStart();
        
        if (msg.getGameMode() != null) {
            this.selectedGameMode = msg.getGameMode();
            updateGameModeButtons();
        }
        
        refreshUI();
    }
    
 // UI 갱신 메서드: 모든 UI 상태를 서버 데이터(me) 기준으로 동기화
    private void refreshUI() {
        // 1. Room ID 업데이트
        if (lRoomIdValue != null && roomId != null) {
            lRoomIdValue.setText(roomId);
        }
        
        NetworkManager nm = getNetworkManager();
        String myId = (nm != null) ? nm.getPlayerId() : "";

        PlayerInfo me = null;
        PlayerInfo other = null;

        if (players != null) {
            for (PlayerInfo p : players) {
                if (p.getPlayerId().equals(myId)) {
                    me = p; // 나를 찾음
                } else {
                    other = p; // 다른 플레이어
                }
            }
        }
        
        // ============================================================
        // 서버 데이터(me)를 기반으로 내 UI 상태 강제 동기화
        // ============================================================
        if (me != null) {
            this.isReady = me.isReady(); 
            this.myRole = me.getRole();  
            
            // 2-1. Ready 버튼 상태 동기화
            if (isReady) {
                bReady.setText("Wait");
                bReady.setButtonColors(BLUE, BLUE, BLUE_PRESSED);
            } else {
                bReady.setText("Ready");
                bReady.setButtonColors(LIGHT_GRAY, LIGHT_GRAY, LIGHT_GRAY.darker());
            }

            // 2-2. 역할 버튼 선택 상태 동기화
            if (this.myRole == ROLE_KNIGHT) {
                bSelectKnight.setButtonColors(BLUE, BLUE, BLUE);
                bSelectHorse.setButtonColors(LIGHT_GRAY, WHITE, GRAY);
                bSelectKnight.setEnabled(true);
                bSelectHorse.setEnabled(false);
            } else if (this.myRole == ROLE_HORSE) {
                bSelectKnight.setButtonColors(LIGHT_GRAY, WHITE, GRAY);
                bSelectHorse.setButtonColors(BLUE, BLUE, BLUE);
                bSelectKnight.setEnabled(false);
                bSelectHorse.setEnabled(true);
            } else {
                bSelectKnight.setButtonColors(LIGHT_GRAY, WHITE, GRAY);
                bSelectHorse.setButtonColors(LIGHT_GRAY, WHITE, GRAY);
                bSelectKnight.setEnabled(!isRoleAlreadyTaken(ROLE_KNIGHT));
                bSelectHorse.setEnabled(!isRoleAlreadyTaken(ROLE_HORSE));
            }

            // 2-3. 캐릭터 선택 버튼 활성화 동기화
            if (this.myRole != ROLE_NONE) {
                bSelectCharacter.setEnabled(true);
                bSelectCharacter.setButtonColors(LIGHT_GRAY, WHITE, GRAY);
            } else {
                bSelectCharacter.setEnabled(false);
                bSelectCharacter.setButtonColors(GRAY, GRAY, GRAY);
            }
        }

        // 3. 플레이어 목록(이름 옆 역할, Ready 상태 표시) 업데이트
        slot1.update(me, hostId);
        slot2.update(other, hostId);
        
        // 4. 시작 버튼 상태 갱신 (방장 전용)
        if (nm != null) {
            boolean isHost = myId != null && myId.equals(hostId);
            
            bStart.setVisible(isHost);
            bStart.setEnabled(canStart);
            
            // 게임 모드 버튼 - 방장만 활성화
            if (bModeInfinite != null && bModeStage != null) {
                bModeInfinite.setEnabled(isHost);
                bModeStage.setEnabled(isHost);
            }
            
            if (canStart) {
                bStart.setButtonColors(BLUE, BLUE.brighter(), BLUE_PRESSED);
                bStart.setText("Start Game");
            } else {
                bStart.setButtonColors(GRAY, GRAY, GRAY);
                bStart.setText("Waiting...");
            }
        }
    }

    /**
     * 채팅 메시지 수신 시 호출
     */
    public void receiveChat(ChatMessage msg) {
        NetworkManager nm = getNetworkManager();
        String myId = (nm != null) ? nm.getPlayerId() : "";
        
        String senderId = msg.getPlayerId();
        String type;
        
        if (senderId.equals(myId)) type = "SELF";
        else if ("server".equals(senderId) || "system".equals(senderId)) type = "SYSTEM";
        else type = "OTHER";
        
        addChatMessage(msg.getSenderName(), msg.getContent(), type);
    }

    private void addChatMessage(String sender, String message, String type) {
        String html = "";
        String selfBg = toHex(BLUE);
        String selfText = toHex(WHITE);
        String otherBg = toHex(WHITE);
        String otherText = toHex(BLACK);
        String otherBorder = toHex(LIGHT_GRAY);
        String sysText = toHex(GRAY);

        switch (type) {
            case "SYSTEM":
                html = String.format(
                    "<div style='text-align: center; margin: 10px 0; font-family: sans-serif;'>" +
                    "  <span style='color: %s; font-size: 10px; padding: 4px 8px; border-radius: 4px;'>%s</span>" +
                    "</div>", sysText, message);
                break;
            case "SELF":
                html = String.format(
                    "<div style='text-align: right; margin-top: 5px; font-family: sans-serif;'>" +
                    "  <table align='right' style='border: 0px;'>" +
                    "    <tr><td bgcolor='%s' style='padding: 6px 10px; border: 0px;'>" +
                    "        <font color='%s'>%s</font></td></tr></table></div>", 
                    selfBg, selfText, message);
                break;
            case "OTHER":
                html = String.format(
                    "<div style='text-align: left; margin-top: 5px; font-family: sans-serif;'>" +
                    "  <div style='font-size: 10px; color: %s; margin-left: 4px; margin-bottom: 2px;'>%s</div>" +
                    "  <table align='left' style='border: 0px;'>" +
                    "    <tr><td bgcolor='%s' style='padding: 6px 10px; border: 1px solid %s;'>" +
                    "        <font color='%s'>%s</font></td></tr></table></div>", 
                    toHex(GRAY), sender, otherBg, otherBorder, otherText, message);
                break;
        }

        try {
            kit.insertHTML(doc, doc.getLength(), html, 0, 0, null);
            tChatPane.setCaretPosition(doc.getLength()); 
        } catch (BadLocationException | IOException e) {
            e.printStackTrace();
        }
    }
    
    private String toHex(Color c) {
        return String.format("#%02x%02x%02x", c.getRed(), c.getGreen(), c.getBlue());
    }

    // ==========================================
    //              액션 핸들러
    // ==========================================

    private void handleRoleSelect(int role) {
        NetworkManager nm = getNetworkManager();
        if (nm == null) return;
        
        // 토글 로직: 이미 선택한 역할을 다시 클릭하면 취소 (ROLE_NONE으로 설정)
        int targetRole = (myRole == role) ? ROLE_NONE : role;
        
        RoomActionMessage msg = new RoomActionMessage(nm.getPlayerId(), SELECT_CHARACTER);
        msg.setRoleType(targetRole);
        nm.sendMessage(MessageType.ROOM_ACTION, msg);
    }
    
    private void handleReady() {
        NetworkManager nm = getNetworkManager();
        if (nm == null) return;
        
        boolean targetState = !isReady;
        
        RoomActionMessage msg = new RoomActionMessage(nm.getPlayerId(), PLAYER_READY);
        msg.setReady(targetState);
        nm.sendMessage(MessageType.ROOM_ACTION, msg);
    }
    
    private void handleStart() {
        NetworkManager nm = getNetworkManager();
        if (nm == null) return;
        
        RoomActionMessage msg = new RoomActionMessage(nm.getPlayerId(), START_GAME);
        msg.setGameMode(GameModeType.STAGE);
        nm.sendMessage(MessageType.ROOM_ACTION, msg);
    }
    
    private void handleExit() {
        int confirm = JOptionPane.showConfirmDialog(
            this, "방을 나가시겠습니까?", "방 나가기", JOptionPane.YES_NO_OPTION
        );
        
        if (confirm == JOptionPane.YES_OPTION) {
            NetworkManager nm = getNetworkManager();
            if (nm != null) {
                RoomActionMessage msg = new RoomActionMessage(nm.getPlayerId(), LEAVE_ROOM);
                nm.sendMessage(MessageType.ROOM_ACTION, msg);
            }
            
            switchToWithoutHistory(new TitleScene());
        }
    }
    
    private void sendChat() {
        NetworkManager nm = getNetworkManager();
        if (nm == null) return;
        
        String content = tChatInput.getText().trim();
        if (content.isEmpty() || content.equals("메시지 보내기...")) return;
        
        ChatMessage msg = new ChatMessage(nm.getPlayerId(), nm.getPlayerName(), content);
        nm.sendMessage(MessageType.CHAT, msg);
        
        tChatInput.setText("");
        tChatInput.setForeground(BLACK);
    }
    
    private String getRoleName(int role) {
        switch (role) {
            case ROLE_KNIGHT: return "Knight";
            case ROLE_HORSE: return "Horse";
            default: return "None";
        }
    }
    
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g); 
        
        g.setColor(TRANSLUCENT_WHITE);
        g.fillRect(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);
        
        g.setColor(LIGHT_GRAY);
        g.drawLine(72, 110, WINDOW_WIDTH - 72, 110);
    }
    
    // 서버로부터 게임 시작 메시지를 받았을 때
    public void onGameStart() {
        SwingUtilities.invokeLater(() -> {
            System.out.println("Game starting! Switching to GameScene...");
            
            hideCharacterSelectionOverlay();
            
            NetworkManager nm = getNetworkManager();
            if (nm == null || players == null || players.size() != 2) {
                return;
            }
            
            nm.removeListener(this);
            
            String myId = nm.getPlayerId();
            PlayerInfo me = null;
            PlayerInfo opponent = null;
            for (PlayerInfo p : players) {
                if (p.getPlayerId().equals(myId)) me = p;
                else opponent = p;
            }
            if (me == null || opponent == null) return;
            
            GameScene gameScene = new GameScene(
                me.getPlayerName(), opponent.getPlayerName(), me.getRole(), opponent.getRole()
            );
            
            switchToWithoutHistory(gameScene);
            
        });
    }
    
    // ==========================================
    //        NetworkListener 구현
    // ==========================================
    
    @Override
    public void onRoomInfo(RoomInfoMessage msg) {
        System.out.println("LobbyScene received RoomInfo");
        SwingUtilities.invokeLater(() -> {
            updateRoomInfo(msg);
        });
    }
    
    @Override
    public void onChat(ChatMessage msg) {
        System.out.println("LobbyScene received Chat");
        SwingUtilities.invokeLater(() -> {
            receiveChat(msg);
        });
    }
    
    // ==========================================
    //       PlayerSlot
    // ==========================================
    private class PlayerSlot {
        private JPanel panel;
        private JLabel lName;
        private JLabel lStatus;

        public PlayerSlot() {
            // 1. 패널 생성 및 설정
            panel = new JPanel(new BorderLayout(10, 0));
            panel.setOpaque(false);
            panel.setMaximumSize(new Dimension(Short.MAX_VALUE, 40));
            panel.setAlignmentX(LEFT_ALIGNMENT);

            // 2. 이름 라벨 생성 (초기값)
            lName = new JLabel("Waiting...");
            lName.setFont(getFont().deriveFont(16f));
            lName.setForeground(BLACK);
            panel.add(lName, BorderLayout.CENTER);

            // 3. 상태 라벨 생성 (초기값)
            lStatus = new JLabel("Wait", SwingConstants.CENTER);
            lStatus.setFont(getFont().deriveFont(12f));
            lStatus.setOpaque(true);
            lStatus.setPreferredSize(new Dimension(60, 24));
            lStatus.setBorder(BorderFactory.createLineBorder(LIGHT_GRAY, 1));
            
            // 초기 상태 스타일 적용
            resetToEmpty();
            
            panel.add(lStatus, BorderLayout.EAST);
        }

        // 외부에서 패널을 가져다 쓰기 위한 Getter
        public JPanel getPanel() {
            return panel;
        }

        // 데이터 갱신 메서드 (핵심 로직 이동)
        public void update(PlayerInfo p, String currentHostId) {
            if (p != null) {
                // 플레이어 정보가 있을 때
                String rName = getRoleName(p.getRole());
                String displayName = p.getPlayerName();
                
                // 방장 표시 로직
                if (p.getPlayerId().equals(currentHostId)) {
                    displayName = "👑 " + displayName;
                }
                
                // 이름 및 역할 업데이트
                lName.setText(String.format("<html><nobr>%s <font color='#888888' size='3'>[%s]</font></nobr></html>", displayName, rName));
                
                // 준비 상태 업데이트
                boolean ready = p.isReady();
                lStatus.setText(ready ? "Ready" : "Wait");
                lStatus.setBackground(ready ? GREEN : LIGHT_GRAY);
                lStatus.setForeground(ready ? WHITE : BLACK);
            } else {
                // 빈 슬롯일 때
                resetToEmpty();
            }
            
            panel.revalidate();
            panel.repaint();
        }
        
        // 빈 슬롯 상태로 초기화하는 헬퍼 메서드
        private void resetToEmpty() {
            lName.setText("<html><font color='#888888'>Waiting...</font></html>");
            lStatus.setText("Empty");
            lStatus.setBackground(LIGHT_GRAY);
            lStatus.setForeground(GRAY);
        }
    }
    
    // 캐릭터 아이템 마우스 이벤트 리스너 
    private class CharacterItemListener extends java.awt.event.MouseAdapter {
        private final int index;
        private final JPanel panel;
        private final int[] selectedIndexRef; // 선택된 인덱스 배열 참조
        private final JPanel[] allPanels;     // 전체 패널 배열 (다른 패널 초기화용)

        public CharacterItemListener(int index, JPanel panel, int[] selectedIndexRef, JPanel[] allPanels) {
            this.index = index;
            this.panel = panel;
            this.selectedIndexRef = selectedIndexRef;
            this.allPanels = allPanels;
        }

        @Override
        public void mouseClicked(java.awt.event.MouseEvent e) {
            // 1. 이전에 선택된 패널이 있다면 스타일 초기화
            int prevIndex = selectedIndexRef[0];
            if (prevIndex >= 0 && prevIndex < allPanels.length) {
                // 이전 패널: 선택 X, 호버 X 상태로 복구
                updateItemStyle(allPanels[prevIndex], false, false);
            }

            // 2. 현재 선택 인덱스 갱신
            selectedIndexRef[0] = index;

            // 3. 현재 패널 선택 스타일 적용
            updateItemStyle(panel, true, false);
        }

        @Override
        public void mouseEntered(java.awt.event.MouseEvent e) {
            // 선택된 상태가 아닐 때만 호버 효과
            if (selectedIndexRef[0] != index) {
                updateItemStyle(panel, false, true);
            }
        }

        @Override
        public void mouseExited(java.awt.event.MouseEvent e) {
            // 선택된 상태가 아닐 때만 원래대로 복구
            if (selectedIndexRef[0] != index) {
                updateItemStyle(panel, false, false);
            }
        }
    }
    
}