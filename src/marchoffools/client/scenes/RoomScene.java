package marchoffools.client.scenes;

import static marchoffools.client.core.Assets.Backgrounds.DEFAULT;
import static marchoffools.client.core.Assets.Colors.*;
import static marchoffools.client.core.Config.*;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.text.BadLocationException;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

import marchoffools.client.ui.Button;
import marchoffools.client.ui.RatioLayout;
import marchoffools.client.core.Scene;

public class RoomScene extends Scene {

    private static final long serialVersionUID = 1L;

    private JTextPane tChatPane;
    private JTextField tChatInput;
    private HTMLEditorKit kit;
    private HTMLDocument doc;

    private Button bReady;
    private JLabel lRoomIdValue;
    private JPanel pRoomIdContent, pHiddenIndicator;
    private boolean roomIdVisible = false;

    public RoomScene() {
        super(DEFAULT);
        
        createTitleSection();
        createExitButton();
        createMainContent();
        
        // test message
        addChatMessage("", "Room이 생성되었습니다.", "SYSTEM");
        addChatMessage("", "<b>다른 플레이어</b> 님이 입장했습니다.", "SYSTEM");
        addChatMessage("다른 플레이어", "안녕하세요! 반갑습니다.", "OTHER");
    }
    
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
        bExit.addActionListener(e -> {
            // TODO: 서버에 나가기 요청
            goBack();
        });
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
        
        mainContentPanel.add(createChatLogPanel(), Integer.valueOf(8));
        mainContentPanel.add(createRightPanel(), Integer.valueOf(3));
        
        add(mainContentPanel);
    }

    private void applySectionStyle(JPanel panel) {
        panel.setOpaque(true);
        panel.setBackground(WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(16, 24, 16, 24));
    }

    
    // Left Panel
    // : Chat Log (HTML format)

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
        tChatPane.setContentType("text/html"); // HTML 모드 활성화
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

    private String toHex(Color c) {
        return String.format("#%02x%02x%02x", c.getRed(), c.getGreen(), c.getBlue());
    }

    /**
     * 채팅 메시지 추가 (말풍선 스타일)
     * @param sender 보낸 사람 이름
     * @param message 메시지 내용
     * @param type 메시지 타입 ("SYSTEM", "SELF", "OTHER")
     */
    public void addChatMessage(String sender, String message, String type) {
        String html = "";
        
        // 기존 Assets 색상만 사용
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
                    "</div>", 
                    sysText, message
                );
                break;
                
            case "SELF":
                html = String.format(
                    "<div style='text-align: right; margin-top: 5px; font-family: sans-serif;'>" +
                    "  <table align='right' style='border: 0px;'>" +
                    "    <tr>" +
                    "      <td bgcolor='%s' style='padding: 6px 10px; border: 0px;'>" +
                    "        <font color='%s'>%s</font>" +
                    "      </td>" +
                    "    </tr>" +
                    "  </table>" +
                    "</div>", 
                    selfBg, selfText, message
                );
                break;
                
            case "OTHER":
                html = String.format(
                    "<div style='text-align: left; margin-top: 5px; font-family: sans-serif;'>" +
                    "  <div style='font-size: 10px; color: %s; margin-left: 4px; margin-bottom: 2px;'>%s</div>" +
                    "  <table align='left' style='border: 0px;'>" +
                    "    <tr>" +
                    "      <td bgcolor='%s' style='padding: 6px 10px; border: 1px solid %s;'>" +
                    "        <font color='%s'>%s</font>" +
                    "      </td>" +
                    "    </tr>" +
                    "  </table>" +
                    "</div>", 
                    toHex(GRAY), sender,
                    otherBg, otherBorder, otherText, message
                );
                break;
        }

        try {
            kit.insertHTML(doc, doc.getLength(), html, 0, 0, null);
            tChatPane.setCaretPosition(doc.getLength()); 
        } catch (BadLocationException | IOException e) {
            e.printStackTrace();
        }
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
    
    private void sendChat() {
        String message = tChatInput.getText().trim();
        if (!message.isEmpty() && !message.equals("메시지 보내기...")) {
            // 화면에 내 메시지 표시
            addChatMessage(null, message, "SELF");
            
            // TODO: 서버 전송 로직 필요
            
            tChatInput.setText("");
            tChatInput.setForeground(BLACK);
        }
    }

    // Right Panel
    // : Info & Actions

    private JPanel createRightPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new RatioLayout(RatioLayout.VERTICAL, 15));
        panel.setOpaque(false);

        panel.add(createPlayerSection(), Integer.valueOf(3));
        panel.add(createRoomIdSection(), Integer.valueOf(3));
        panel.add(createReadyButtonSection(), Integer.valueOf(2));

        return panel;
    }
    
    private JPanel createPlayerSection() {
        JPanel section = new JPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));

        section.setAlignmentX(LEFT_ALIGNMENT);
        applySectionStyle(section);

        JLabel lPlayerTitle = createPlayerTitle();
        section.add(lPlayerTitle);
        section.add(Box.createVerticalStrut(24));

        section.add(createPlayerEntry("닉네임", true));
        section.add(Box.createVerticalStrut(12));
        section.add(createPlayerEntry("다른 플레이어", false));

        return section;
    }
    
    private JLabel createPlayerTitle() {
        JLabel lPlayerTitle = new JLabel("Player");
        lPlayerTitle.setFont(getFont().deriveFont(Font.BOLD, 24f));
        lPlayerTitle.setForeground(BLACK);
        lPlayerTitle.setAlignmentX(LEFT_ALIGNMENT);
        return lPlayerTitle;
    }
    
    private JPanel createRoomIdSection() {
        JPanel section = new JPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setAlignmentX(LEFT_ALIGNMENT);
        applySectionStyle(section);

        JPanel pHeader = createRoomIdHeader(section);
        section.add(pHeader);
        section.add(Box.createVerticalStrut(20));

        JPanel centerContainer = createRoomIdCenterContainer();
        section.add(centerContainer);
        section.add(Box.createVerticalStrut(20));

        return section;
    }
    
    private JPanel createRoomIdHeader(JPanel parentSection) {
        JPanel pHeader = new JPanel(new BorderLayout());
        pHeader.setOpaque(false);
        pHeader.setMaximumSize(new Dimension(Short.MAX_VALUE, 30));

        pHeader.setAlignmentX(LEFT_ALIGNMENT);
        
        JLabel lRoomIdTitle = new JLabel("Room ID");
        lRoomIdTitle.setFont(getFont().deriveFont(Font.BOLD, 24f));
        lRoomIdTitle.setForeground(BLACK);
        pHeader.add(lRoomIdTitle, BorderLayout.WEST);
        
        Button bToggle = createRoomIdToggleButton(parentSection);
        pHeader.add(bToggle, BorderLayout.EAST);
        
        return pHeader;
    }
    
    private Button createRoomIdToggleButton(JPanel parentSection) {
        Button bToggle = new Button("○");
        bToggle.setFont(getFont().deriveFont(18f));
        bToggle.setPreferredSize(new Dimension(60, 42));
        
        // [수정] setButtonColors 사용 (기본 LIGHT_GRAY)
        bToggle.setButtonColors(LIGHT_GRAY, WHITE, GRAY);
        
        bToggle.addActionListener(e -> {
            roomIdVisible = !roomIdVisible;
            pRoomIdContent.setVisible(roomIdVisible);
            pHiddenIndicator.setVisible(!roomIdVisible);
            bToggle.setText(roomIdVisible ? "●" : "○");
            parentSection.revalidate();
            parentSection.repaint();
        });
        return bToggle;
    }
    
    private JPanel createRoomIdCenterContainer() {
        JPanel centerContainer = new JPanel();
        centerContainer.setLayout(new BoxLayout(centerContainer, BoxLayout.Y_AXIS));
        centerContainer.setOpaque(false);
        centerContainer.setAlignmentX(LEFT_ALIGNMENT);

        pRoomIdContent = createRoomIdValuePanel();
        pHiddenIndicator = createRoomIdHiddenIndicator();
        
        centerContainer.add(pRoomIdContent);
        centerContainer.add(pHiddenIndicator);
        
        return centerContainer;
    }
    
    private JPanel createRoomIdValuePanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setVisible(false);
        panel.add(Box.createVerticalGlue());
        
        lRoomIdValue = new JLabel("251836"); // TODO: 실제 데이터 바인딩 필요
        lRoomIdValue.setFont(getFont().deriveFont(Font.BOLD, 42f));
        lRoomIdValue.setForeground(BLACK);
        lRoomIdValue.setAlignmentX(CENTER_ALIGNMENT);
        panel.add(lRoomIdValue);
        panel.add(Box.createVerticalGlue());
        
        return panel;
    }
    
    private JPanel createRoomIdHiddenIndicator() {
        JPanel indicator = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(GRAY);
                int y = getHeight() / 2;
                g.drawLine(20, y, getWidth() - 20, y);
            }
        };
        indicator.setOpaque(false);
        indicator.setVisible(true);
        return indicator;
    }
    
    private JPanel createReadyButtonSection() {
        JPanel section = new JPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setAlignmentX(LEFT_ALIGNMENT);
        applySectionStyle(section);

        bReady = createReadyButton();
        section.add(bReady);

        return section;
    }
    
    private Button createReadyButton() {
        Button button = new Button("Ready");
        button.setFont(getFont().deriveFont(Font.BOLD, 52f));
        button.setForeground(WHITE);
        
        button.setButtonColors(LIGHT_GRAY, LIGHT_GRAY, LIGHT_GRAY.darker());
        
        button.setMinimumSize(new Dimension(250, 100));
        button.setMaximumSize(new Dimension(Short.MAX_VALUE, 100));
        button.setPreferredSize(new Dimension(250, 100));
        button.setBorder(BorderFactory.createLineBorder(GRAY, 2));
        button.addActionListener(e -> {
            // TODO: 서버에 준비 상태 전송
            if (button.getText().equals("Ready")) {
                button.setText("Wait");
                button.setButtonColors(BLUE, BLUE, BLUE_PRESSED);
            } else {
                button.setText("Ready");
                button.setButtonColors(LIGHT_GRAY, LIGHT_GRAY, LIGHT_GRAY.darker());
            }
        });
        button.setAlignmentX(CENTER_ALIGNMENT);
        
        return button;
    }
    
    private JPanel createPlayerEntry(String name, boolean isMe) {
        JPanel entry = new JPanel(new BorderLayout(10, 0));
        entry.setOpaque(false);
        entry.setMaximumSize(new Dimension(Short.MAX_VALUE, 35));
        entry.setAlignmentX(LEFT_ALIGNMENT);

        JLabel lName = new JLabel((isMe ? "👑 " : "") + name);
        lName.setFont(getFont().deriveFont(18f));
        lName.setForeground(BLACK);
        entry.add(lName, BorderLayout.WEST);

        JLabel lReadyStatus = new JLabel("Ready", SwingConstants.CENTER);
        lReadyStatus.setFont(getFont().deriveFont(14f));
        lReadyStatus.setForeground(BLACK);
        lReadyStatus.setBackground(LIGHT_GRAY);
        lReadyStatus.setOpaque(true);
        lReadyStatus.setPreferredSize(new Dimension(70, 28));
        lReadyStatus.setBorder(BorderFactory.createLineBorder(LIGHT_GRAY, 1));
        
        entry.add(lReadyStatus, BorderLayout.EAST);
        
        return entry;
    }
    
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        g.setColor(TRANSLUCENT_WHITE);
        g.fillRect(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);
        
        g.setColor(LIGHT_GRAY);
        g.drawLine(72, 110, WINDOW_WIDTH - 72, 110);
    }
}