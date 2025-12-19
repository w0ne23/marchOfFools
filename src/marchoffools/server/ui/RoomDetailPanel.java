package marchoffools.server.ui;

import javax.swing.*;
import java.awt.*;
import java.util.List;

import marchoffools.server.game.Room;
import marchoffools.common.model.PlayerInfo;

/**
 * 방 상세 정보 및 로그 표시 패널
 */
public class RoomDetailPanel extends JPanel {
    
    private ServerGUI parent;
    
    // 방 정보
    private JLabel roomIdLabel;
    private JLabel statusLabel;
    private JPanel playersPanel;
    
    // 로그 영역
    private JTextArea chatLog;
    private JTextArea gameLog;
    
    // 액션 버튼
    private JButton kickRoomBtn;
    private JButton clearLogBtn;
    
    private String currentRoomId;
    
    public RoomDetailPanel(ServerGUI parent) {
        this.parent = parent;
        initComponents();
        layoutComponents();
    }
    
    private void initComponents() {
        // 방 정보 라벨
        roomIdLabel = new JLabel("📋 방을 선택하세요");
        roomIdLabel.setFont(new Font("맑은 고딕", Font.BOLD, 16));
        
        statusLabel = new JLabel("");
        statusLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        
        // 플레이어 패널
        playersPanel = new JPanel();
        playersPanel.setLayout(new BoxLayout(playersPanel, BoxLayout.Y_AXIS));
        playersPanel.setBorder(BorderFactory.createTitledBorder("👥 플레이어"));
        playersPanel.setBackground(Color.WHITE);
        playersPanel.setPreferredSize(new Dimension(0, 80));
        
        // 로그 영역
        chatLog = new JTextArea();
        chatLog.setEditable(false);
        chatLog.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        chatLog.setLineWrap(true);
        chatLog.setWrapStyleWord(true);
        
        gameLog = new JTextArea();
        gameLog.setEditable(false);
        gameLog.setFont(new Font("Consolas", Font.PLAIN, 12));
        gameLog.setBackground(new Color(40, 44, 52));
        gameLog.setForeground(new Color(171, 178, 191));
        gameLog.setLineWrap(true);
        
        // 버튼
        kickRoomBtn = new JButton("🚪 방 강제 종료");
        kickRoomBtn.setEnabled(false);
        kickRoomBtn.setBackground(new Color(244, 67, 54));
        kickRoomBtn.addActionListener(e -> {
            if (currentRoomId != null) {
                parent.kickRoom(currentRoomId);
            }
        });
        
        clearLogBtn = new JButton("🚮 로그 지우기");
        clearLogBtn.addActionListener(e -> {
            chatLog.setText("");
            gameLog.setText("");
        });
    }
    
    private void layoutComponents() {
        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        // 상단: 방 정보
        JPanel topPanel = new JPanel(new BorderLayout(5, 5));
        topPanel.setBackground(new Color(250, 250, 250));
        topPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEtchedBorder(),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0));
        infoPanel.setOpaque(false);
        infoPanel.add(roomIdLabel);
        infoPanel.add(statusLabel);
        
        topPanel.add(infoPanel, BorderLayout.NORTH);
        topPanel.add(playersPanel, BorderLayout.CENTER);
        
        // 중앙: 로그 영역 (채팅 | 게임)
        JSplitPane logSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        logSplit.setResizeWeight(0.5);
        
        JPanel chatPanel = new JPanel(new BorderLayout());
        chatPanel.setBorder(BorderFactory.createTitledBorder("💬 채팅 로그"));
        JScrollPane chatScroll = new JScrollPane(chatLog);
        chatPanel.add(chatScroll, BorderLayout.CENTER);
        
        JPanel gamePanel = new JPanel(new BorderLayout());
        gamePanel.setBorder(BorderFactory.createTitledBorder("🎮 게임 로그"));
        JScrollPane gameScroll = new JScrollPane(gameLog);
        gamePanel.add(gameScroll, BorderLayout.CENTER);
        
        logSplit.setLeftComponent(chatPanel);
        logSplit.setRightComponent(gamePanel);
        
        // 하단: 액션 버튼
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        buttonPanel.add(clearLogBtn);
        buttonPanel.add(kickRoomBtn);
        
        add(topPanel, BorderLayout.NORTH);
        add(logSplit, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    public void showRoom(Room room) {
        if (room == null) {
            clear();
            return;
        }
        
        currentRoomId = room.getRoomId();
        
        // 방 정보 업데이트
        roomIdLabel.setText("📋 방 ID: " + room.getRoomId());
        
        String status;
        Color statusColor;
        if (room.isPlaying()) {
            status = "🔴 게임 중";
            statusColor = new Color(192, 0, 0);
        } else if (room.isFull()) {
            status = "🟢 대기 (준비 가능)";
            statusColor = new Color(0, 128, 0);
        } else {
            status = "🟡 대기 중 (" + room.getPlayerCount() + "/2)";
            statusColor = new Color(200, 150, 0);
        }
        statusLabel.setText(status);
        statusLabel.setForeground(statusColor);
        
        // 플레이어 정보 업데이트
        playersPanel.removeAll();
        for (PlayerInfo player : room.getPlayers().values()) {
            JLabel playerLabel = new JLabel(formatPlayerInfo(player, room.getHostId()));
            playerLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 13));
            playerLabel.setBorder(BorderFactory.createEmptyBorder(2, 10, 2, 10));
            playersPanel.add(playerLabel);
        }
        
        if (room.getPlayerCount() < 2) {
            JLabel waitingLabel = new JLabel("    (플레이어 대기 중...)");
            waitingLabel.setForeground(Color.GRAY);
            waitingLabel.setFont(new Font("맑은 고딕", Font.ITALIC, 12));
            playersPanel.add(waitingLabel);
        }
        
        playersPanel.revalidate();
        playersPanel.repaint();
        
        // 저장된 로그 불러오기
        loadRoomLogs(room.getRoomId());
        
        // 버튼 활성화
        kickRoomBtn.setEnabled(true);
    }
    
    private void loadRoomLogs(String roomId) {
        ServerLogger logger = ServerLogger.getInstance();
        
        // 채팅 로그
        chatLog.setText("");
        List<String> chatLogs = logger.getRoomChatLogs(roomId);
        for (String log : chatLogs) {
            chatLog.append(log + "\n");
        }
        
        // 게임 로그
        gameLog.setText("");
        List<String> gameLogs = logger.getRoomGameLogs(roomId);
        for (String log : gameLogs) {
            gameLog.append(log + "\n");
        }
        
        // 스크롤 맨 아래로
        chatLog.setCaretPosition(chatLog.getDocument().getLength());
        gameLog.setCaretPosition(gameLog.getDocument().getLength());
    }
    
    private String formatPlayerInfo(PlayerInfo player, String hostId) {
        StringBuilder sb = new StringBuilder();
        
        // 방장 표시
        if (player.getPlayerId().equals(hostId)) {
            sb.append("👑 ");
        } else {
            sb.append("    ");
        }
        
        sb.append(player.getPlayerName());
        
        // 역할 표시
        String role = switch (player.getRole()) {
            case 1 -> " | 🛡 기사";
            case 2 -> " | 🐴 말";
            default -> " | ❓ 미선택";
        };
        sb.append(role);
        
        // 준비 상태
        if (player.isReady()) {
            sb.append(" | ✅ 준비 완료");
        } else {
            sb.append(" | ⏳ 대기");
        }
        
        return sb.toString();
    }
    
    public void clear() {
        currentRoomId = null;
        roomIdLabel.setText("📋 방을 선택하세요");
        statusLabel.setText("");
        playersPanel.removeAll();
        playersPanel.revalidate();
        playersPanel.repaint();
        chatLog.setText("");
        gameLog.setText("");
        kickRoomBtn.setEnabled(false);
    }
    
    public void appendLog(ServerLogger.LogLevel level, String timestamp, String message) {
        String formatted = "[" + timestamp + "] " + message + "\n";
        
        if (level == ServerLogger.LogLevel.CHAT) {
            chatLog.append(formatted);
            chatLog.setCaretPosition(chatLog.getDocument().getLength());
        } else if (level == ServerLogger.LogLevel.GAME) {
            gameLog.append(formatted);
            gameLog.setCaretPosition(gameLog.getDocument().getLength());
        }
    }
}