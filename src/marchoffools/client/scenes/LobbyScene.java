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
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.IOException;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
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

import marchoffools.client.core.Scene;
import marchoffools.client.network.NetworkManager;
import marchoffools.client.ui.Button;
import marchoffools.client.ui.RatioLayout;
import marchoffools.common.message.ChatMessage;
import marchoffools.common.message.RoomActionMessage;
import marchoffools.common.message.RoomInfoMessage;
import marchoffools.common.model.PlayerInfo;
import marchoffools.common.protocol.MessageType;

/**
 * 대기실 화면 (LobbyScene with RoomScene Design)
 */
public class LobbyScene extends Scene {

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
    
    // 플레이어 슬롯 패널 (최대 2명)
    private JPanel player1Entry;
    private JPanel player2Entry;

    // 역할 선택 버튼 (자신만 보임)
    private JPanel roleSelectionPanel;
    private Button bSelectKnight;
    private Button bSelectHorse;
    
    // 캐릭터 선택 버튼
    private JPanel characterSelectionPanel;
    private Button bSelectCharacter;

    public LobbyScene() {
        super(DEFAULT); // 배경 이미지 설정 (RoomScene 스타일)
        
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
        add(lTitle);
    }
    
    private void createExitButton() {
        Button bExit = new Button("->");
        bExit.setFont(getFont().deriveFont(Font.BOLD, 30f));
        bExit.setSize(100, 50); 
        bExit.setLocation(WINDOW_WIDTH - bExit.getWidth() - 72, 40);
        bExit.addActionListener(e -> handleExit());
        add(bExit);
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
        mainContentPanel.add(createRightPanel(), Integer.valueOf(3));
        
        add(mainContentPanel);
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

        // 1. 플레이어 목록 & 역할 선택
        panel.add(createPlayerSection(), Integer.valueOf(4));
        
        // 2. 방 ID 정보
        panel.add(createRoomIdSection(), Integer.valueOf(2));
        
        // 3. 준비/시작 버튼
        panel.add(createActionButtonsSection(), Integer.valueOf(2));

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
        player1Entry = createPlayerEntry("Waiting...", false, "None");
        section.add(player1Entry);
        section.add(Box.createVerticalStrut(10));

        // 플레이어 2 슬롯
        player2Entry = createPlayerEntry("Waiting...", false, "None");
        section.add(player2Entry);
        
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
    
    private JPanel createPlayerEntry(String name, boolean isReady, String role) {
        JPanel entry = new JPanel(new BorderLayout(10, 0));
        entry.setOpaque(false);
        entry.setMaximumSize(new Dimension(Short.MAX_VALUE, 40));
        entry.setAlignmentX(LEFT_ALIGNMENT);

        // 이름 + 역할
        String labelText = String.format("<html>%s <font color='#888888' size='3'>[%s]</font></html>", name, role);
        JLabel lName = new JLabel(labelText);
        lName.setFont(getFont().deriveFont(16f));
        lName.setForeground(BLACK);
        entry.add(lName, BorderLayout.CENTER);

        // 준비 상태
        JLabel lReadyStatus = new JLabel(isReady ? "Ready" : "Wait", SwingConstants.CENTER);
        lReadyStatus.setFont(getFont().deriveFont(12f));
        lReadyStatus.setForeground(isReady ? WHITE : BLACK);
        lReadyStatus.setBackground(isReady ? GREEN : LIGHT_GRAY);
        lReadyStatus.setOpaque(true);
        lReadyStatus.setPreferredSize(new Dimension(60, 24));
        lReadyStatus.setBorder(BorderFactory.createLineBorder(LIGHT_GRAY, 1));
        
        entry.add(lReadyStatus, BorderLayout.EAST);
        
        return entry;
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
            // 캐릭터 선택 팝업이나 scene 띄우기 등의 로직 구현
        });
        
        panel.add(bSelectCharacter, BorderLayout.CENTER);
        
        return panel;
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
        lRoomIdValue.setFont(getFont().deriveFont(Font.BOLD, 32f));
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
        bReady.setFont(getFont().deriveFont(Font.BOLD, 36f));
        bReady.setForeground(WHITE);
        bReady.setButtonColors(LIGHT_GRAY, LIGHT_GRAY, LIGHT_GRAY.darker());
        bReady.setMinimumSize(new Dimension(100, 60));
        bReady.setMaximumSize(new Dimension(Short.MAX_VALUE, 80));
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
        bStart.setMaximumSize(new Dimension(Short.MAX_VALUE, 50));
        bStart.setVisible(false); // 방장만 보임
        bStart.addActionListener(e -> handleStart());
        
        section.add(Box.createVerticalStrut(10));
        section.add(bStart);

        return section;
    }

    // ==========================================
    //              메시지 처리 로직
    // ==========================================

    /**
     * 서버로부터 RoomInfoMessage 수신 시 호출
     */
    public void updateRoomInfo(RoomInfoMessage msg) {
        this.roomId = msg.getRoomId();
        this.hostId = msg.getHostId();
        this.players = msg.getPlayers();
        this.canStart = msg.isCanStart();
        
        SwingUtilities.invokeLater(this::refreshUI);
    }
    
 // [수정] UI 갱신 메서드: 모든 UI 상태를 서버 데이터(me) 기준으로 동기화
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
        // ★ [핵심] 서버 데이터(me)를 기반으로 내 UI 상태 강제 동기화
        // ============================================================
        if (me != null) {
            this.isReady = me.isReady(); // 서버 데이터로 덮어쓰기
            this.myRole = me.getRole();  // 서버 데이터로 덮어쓰기
            
            // 2-1. Ready 버튼 상태 동기화
            if (isReady) {
                bReady.setText("Wait");
                bReady.setButtonColors(BLUE, BLUE, BLUE_PRESSED);
            } else {
                bReady.setText("Ready");
                bReady.setButtonColors(LIGHT_GRAY, LIGHT_GRAY, LIGHT_GRAY.darker());
            }

            // 2-2. [추가] 역할 버튼 선택 상태 동기화 (내가 선택한 것 파란색으로)
            if (this.myRole == ROLE_KNIGHT) {
                bSelectKnight.setButtonColors(BLUE, BLUE, BLUE);
                bSelectHorse.setButtonColors(LIGHT_GRAY, WHITE, GRAY);
            } else if (this.myRole == ROLE_HORSE) {
                bSelectKnight.setButtonColors(LIGHT_GRAY, WHITE, GRAY);
                bSelectHorse.setButtonColors(BLUE, BLUE, BLUE);
            } else {
                bSelectKnight.setButtonColors(LIGHT_GRAY, WHITE, GRAY);
                bSelectHorse.setButtonColors(LIGHT_GRAY, WHITE, GRAY);
            }

            // 2-3. [추가] 캐릭터 선택 버튼 활성화 동기화
            // 역할(Role)이 선택되어 있어야만(NONE이 아니면) 활성화
            if (this.myRole != ROLE_NONE) {
                bSelectCharacter.setEnabled(true);
                bSelectCharacter.setButtonColors(LIGHT_GRAY, WHITE, GRAY);
            } else {
                bSelectCharacter.setEnabled(false);
                bSelectCharacter.setButtonColors(GRAY, GRAY, GRAY);
            }
        }

        // 3. 플레이어 목록(이름 옆 역할, Ready 상태 표시) 업데이트
        updatePlayerEntry(player1Entry, me);    // 내 정보
        updatePlayerEntry(player2Entry, other); // 상대방 정보
        
        // 4. 시작 버튼 상태 갱신 (방장 전용)
        if (nm != null) {
            boolean isHost = myId != null && myId.equals(hostId);
            
            bStart.setVisible(isHost);
            bStart.setEnabled(canStart);
            
            if (canStart) {
                bStart.setButtonColors(BLUE, BLUE.brighter(), BLUE_PRESSED);
                bStart.setText("Start Game");
            } else {
                bStart.setButtonColors(GRAY, GRAY, GRAY);
                bStart.setText("Waiting...");
            }
        }
    }

    private void updatePlayerEntry(JPanel entryPanel, PlayerInfo p) {
        // 내부 컴포넌트 찾기 (순서 의존: 0=NameLabel, 1=ReadyLabel)
        JLabel lName = (JLabel) entryPanel.getComponent(0);
        JLabel lStatus = (JLabel) entryPanel.getComponent(1);
        
        if (p != null) {
            // 플레이어 정보가 있을 때
            String rName = getRoleName(p.getRole());
            
            // 이름 및 역할 업데이트
            String displayName = p.getPlayerName();
            if (p.getPlayerId().equals(hostId)) displayName = "👑 " + displayName;
            
            lName.setText(String.format("<html><nobr>%s <font color='#888888' size='3'>[%s]</font></nobr></html>", displayName, rName));
            
            // 준비 상태 업데이트
            boolean ready = p.isReady();
            lStatus.setText(ready ? "Ready" : "Wait");
            lStatus.setBackground(ready ? GREEN : LIGHT_GRAY);
            lStatus.setForeground(ready ? WHITE : BLACK);
            
        } else {
            // 빈 슬롯
            lName.setText("Waiting...");
            lStatus.setText("Empty");
            lStatus.setBackground(LIGHT_GRAY);
            lStatus.setForeground(GRAY);
        }
        
        entryPanel.revalidate();
        entryPanel.repaint();
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

    // HTML 채팅 추가 (RoomScene 스타일)
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
        
        RoomActionMessage msg = new RoomActionMessage(nm.getPlayerId(), SELECT_CHARACTER);
        msg.setRoleType(role);
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
            switchTo(new TitleScene());
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
        super.paintComponent(g); // 배경 이미지
        
        g.setColor(TRANSLUCENT_WHITE);
        g.fillRect(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);
        
        g.setColor(LIGHT_GRAY);
        g.drawLine(72, 110, WINDOW_WIDTH - 72, 110);
    }
}