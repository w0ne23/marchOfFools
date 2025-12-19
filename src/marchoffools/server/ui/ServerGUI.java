package marchoffools.server.ui;

import javax.swing.*;
import java.awt.*;

import marchoffools.common.message.RoomInfoMessage;
import marchoffools.common.protocol.MessageType;
import marchoffools.common.protocol.Packet;
import marchoffools.server.game.Room;
import marchoffools.server.network.ClientHandler;
import marchoffools.server.network.GameServer;

/**
 * 서버 관리 GUI 메인 프레임
 */
public class ServerGUI extends JFrame implements ServerEventListener {
    
    private GameServer server;
    
    // UI 컴포넌트
    private ServerInfoPanel infoPanel;
    private RoomListPanel roomListPanel;
    private RoomDetailPanel roomDetailPanel;
    private LogPanel serverLogPanel;
    
    // 현재 선택된 방
    private String selectedRoomId;
    
    public ServerGUI() {
        setTitle("MarchOfFools_Server");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);
        
        initComponents();
        layoutComponents();
        
        ServerLogger.getInstance().addListener(this::handleLog);
    }
    
    private void initComponents() {
        infoPanel = new ServerInfoPanel(this);
        roomListPanel = new RoomListPanel(this);
        roomDetailPanel = new RoomDetailPanel(this);
        serverLogPanel = new LogPanel("서버 로그");
    }
    
    private void layoutComponents() {
        setLayout(new BorderLayout(5, 5));
        
        // 상단: 서버 정보
        add(infoPanel, BorderLayout.NORTH);
        
        // 중앙: 방 목록 + 상세 정보
        JSplitPane centerSplit = new JSplitPane(
            JSplitPane.HORIZONTAL_SPLIT,
            roomListPanel,
            roomDetailPanel
        );
        centerSplit.setDividerLocation(200);
        add(centerSplit, BorderLayout.CENTER);
        
        // 하단: 서버 로그
        serverLogPanel.setPreferredSize(new Dimension(0, 200));
        add(serverLogPanel, BorderLayout.SOUTH);
    }
    
    // ========== 서버 제어 ==========
    
    public void startServer(int port) {
        if (server != null) {
            ServerLogger.getInstance().warn("서버가 이미 실행 중입니다");
            return;
        }
        
        server = new GameServer(port, this);
        infoPanel.setServerStatus(true, port);
    }
    
    public void stopServer() {
        if (server == null) {
            ServerLogger.getInstance().warn("서버가 실행 중이 아닙니다");
            return;
        }
        
        server.stopServer();
        server = null;
        infoPanel.setServerStatus(false, 0);
        
        // UI 초기화
        roomListPanel.clear();
        roomDetailPanel.clear();
    }
    
    // ========== 방 관리 ==========
    
    public void selectRoom(String roomId) {
        this.selectedRoomId = roomId;
        
        if (server != null && roomId != null) {
            Room room = server.getRoomManager().getRoom(roomId);
            roomDetailPanel.showRoom(room);
        }
    }
    
    public void kickRoom(String roomId) {
        if (server == null) return;
        
        Room room = server.getRoomManager().getRoom(roomId);
        if (room == null) {
            ServerLogger.getInstance().warn("방을 찾을 수 없습니다: " + roomId);
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "방 " + roomId + "를 강제 종료하시겠습니까?\n모든 플레이어가 퇴장됩니다.",
            "방 강제 종료",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );
        
        if (confirm == JOptionPane.YES_OPTION) {
        	
        	RoomInfoMessage kickMsg = new RoomInfoMessage(RoomInfoMessage.ROOM_KICKED, roomId);
            kickMsg.setStatus(RoomInfoMessage.ROOM_KICKED); 
            Packet packet = new Packet(MessageType.ROOM_INFO, kickMsg);
            room.broadcastPacket(packet);
        	
            // 게임 세션 종료
            if (room.getGameSession() != null) {
                room.getGameSession().stopGame();
            }
            
            for (ClientHandler handler : room.getHandlers()) {
                handler.clearCurrentRoom();
            }
            
            server.getRoomManager().removeRoom(roomId);
            server.notifyRoomRemoved(roomId);
            
            ServerLogger.getInstance().warn("방 강제 종료: " + roomId);
        }
    }
    
    public void refreshRooms() {
        if (server == null) return;
        
        roomListPanel.clear();
        for (Room room : server.getRoomManager().getAllRooms()) {
            roomListPanel.addRoom(room.getRoomId());
            roomListPanel.updateRoom(room);
        }
    }
    
    // ========== ServerEventListener 구현 ==========
    
    @Override
    public void onClientConnected(String clientId, String clientName, String ip) {
        SwingUtilities.invokeLater(() -> {
            updateStats();
        });
    }
    
    @Override
    public void onClientDisconnected(String clientId, String clientName) {
        SwingUtilities.invokeLater(() -> {
            updateStats();
        });
    }
    
    @Override
    public void onRoomCreated(String roomId, String hostName) {
        SwingUtilities.invokeLater(() -> {
            roomListPanel.addRoom(roomId);
            updateStats();
        });
    }
    
    @Override
    public void onRoomRemoved(String roomId) {
        SwingUtilities.invokeLater(() -> {
            roomListPanel.removeRoom(roomId);
            if (roomId.equals(selectedRoomId)) {
                roomDetailPanel.clear();
                selectedRoomId = null;
            }
            updateStats();
            ServerLogger.getInstance().clearRoomLogs(roomId);
        });
    }
    
    @Override
    public void onRoomUpdated(Room room) {
        SwingUtilities.invokeLater(() -> {
            roomListPanel.updateRoom(room);
            if (room.getRoomId().equals(selectedRoomId)) {
                roomDetailPanel.showRoom(room);
            }
            updateStats();
        });
    }
    
    @Override
    public void onGameStarted(String roomId) {
        SwingUtilities.invokeLater(() -> {
            if (server != null) {
                Room room = server.getRoomManager().getRoom(roomId);
                if (room != null) {
                    roomListPanel.updateRoom(room);
                    if (roomId.equals(selectedRoomId)) {
                        roomDetailPanel.showRoom(room);
                    }
                }
            }
            updateStats();
        });
    }
    
    @Override
    public void onGameEnded(String roomId, int score) {
        SwingUtilities.invokeLater(() -> {
            if (server != null) {
                Room room = server.getRoomManager().getRoom(roomId);
                if (room != null) {
                    roomListPanel.updateRoom(room);
                    if (roomId.equals(selectedRoomId)) {
                        roomDetailPanel.showRoom(room);
                    }
                }
            }
            updateStats();
        });
    }
    
    // ServerLogger에서 처리됨
    @Override
    public void onChatMessage(String roomId, String sender, String message) {}
    @Override
    public void onGameLog(String roomId, String log) {}
    @Override
    public void onServerLog(String level, String message) {}
    
    // ========== 로그 처리 ==========
    
    private void handleLog(ServerLogger.LogLevel level, String timestamp, 
                           String message, String roomId) {
        SwingUtilities.invokeLater(() -> {
            if (roomId == null) {
                // 서버 로그
                serverLogPanel.appendLog(timestamp, level.name(), message);
            } else if (roomId.equals(selectedRoomId)) {
                // 선택된 방의 로그
                roomDetailPanel.appendLog(level, timestamp, message);
            }
        });
    }
    
    // ========== 통계 업데이트 ==========
    
    private void updateStats() {
        if (server != null) {
            int roomCount = server.getRoomManager().getRoomCount();
            int playerCount = server.getClientCount();
            int gameCount = server.getPlayingRoomCount();
            infoPanel.updateStats(roomCount, playerCount, gameCount);
        }
    }
    
    // ========== Getters ==========
    
    public GameServer getServer() {
        return server;
    }
    
    public String getSelectedRoomId() {
        return selectedRoomId;
    }
}