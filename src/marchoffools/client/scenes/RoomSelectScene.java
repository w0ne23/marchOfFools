package marchoffools.client.scenes;

import marchoffools.common.message.RoomInfoMessage;

import marchoffools.client.network.NetworkManager;
import marchoffools.client.network.NetworkListener;
import marchoffools.client.core.Scene;
import marchoffools.client.ui.Button;

import static marchoffools.client.core.Assets.Backgrounds.DEFAULT;
import static marchoffools.client.core.Assets.Colors.*;
import static marchoffools.client.core.Config.*;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import marchoffools.common.protocol.MessageType;
import marchoffools.common.message.RoomActionMessage;
import marchoffools.common.message.RoomListMessage;
import marchoffools.common.message.RoomListMessage.RoomSummary;

public class RoomSelectScene extends Scene implements NetworkListener {

    private JPanel pMenuWrapper;
    private JPanel pMenu;
    private JPanel pLoading;
    private JPanel pRoomList;
    private JPanel pRoomListContent;
    
    private boolean roomListVisible = false;
    
    public RoomSelectScene() {
        super(DEFAULT);

        JLabel lTitle = new JLabel("게임 참여");
        lTitle.setFont(getFont().deriveFont(52f));
        lTitle.setSize(lTitle.getPreferredSize());
        lTitle.setLocation((WINDOW_WIDTH - lTitle.getWidth())/2, (WINDOW_HEIGHT - lTitle.getHeight())/8);
        add(lTitle);
        
        pMenuWrapper = createMenuWrapper();
        pLoading = createLoadingPanel();
        pRoomList = createRoomListPanel();
        
        add(pMenuWrapper);
        add(pLoading);
        add(pRoomList);
        
        setFocusable(false);
    }

    // ==========================================
    //        연결 관련 메서드
    // ==========================================
    
    private NetworkManager getConnectedNetworkManager() {
        NetworkManager nm = getNetworkManager();
        
        if (nm == null) {
            System.err.println("서버에 연결되어 있지 않습니다");
            return null;
        }
        
        if (!nm.isConnected()) {
            System.err.println("서버에 연결되어 있지 않습니다");
            return null;
        }
        
        return nm;
    }
    
    private boolean ensureConnected() {
        System.out.println("ensureConnected() called");
        
        NetworkManager nm = getNetworkManager();
        
        if (nm == null) {
            System.err.println("네트워크 매니저를 사용할 수 없습니다.");
            return false;
        }
        
        if (nm.isConnected()) {
            System.out.println("이미 서버에 연결되어 있습니다.");
            return true;
        }
        
        return connectToServer(nm);
    }
    
    private boolean connectToServer(NetworkManager nm) {
        System.out.println("connectToServer() called");
        
        if (nm == null) {
            System.err.println("오류: NetworkManager가 null입니다!");
            return false;
        }
        
        String playerName = JOptionPane.showInputDialog(
            this, 
            "플레이어 이름을 입력하세요:", 
            "서버 연결", 
            JOptionPane.PLAIN_MESSAGE
        );
        
        if (playerName == null) {
            System.out.println("플레이어 이름 입력 취소됨");
            return false;
        }
        
        if (playerName.trim().isEmpty()) {
            playerName = "Player_" + (System.currentTimeMillis() % 10000);
            System.out.println("자동 생성된 이름: " + playerName);
        }
        
        System.out.println("서버 연결 시도: " + playerName);
        
        boolean success = nm.connect("localhost", 12345, playerName);

        if (success) {
            System.out.println("서버 연결 성공!");
        } else {
            System.out.println("서버 연결 실패");
        }
        
        return success;
    }

    // ==========================================
    //        UI 생성 메서드
    // ==========================================
    
    private JPanel createMenuWrapper() {
        JPanel wrapper = new JPanel(null);
        wrapper.setOpaque(false);
        wrapper.setBounds(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);
        
        pMenu = createButtonPanel();
        wrapper.add(pMenu);
        
        return wrapper;
    }

    private JPanel createButtonPanel() {
        JPanel bp = new JPanel(new GridLayout(5, 1, 0, 10));
        
        int menuW = WINDOW_WIDTH * 3 / 10;
        int menuH = WINDOW_HEIGHT / 2;
        
        bp.setSize(menuW, menuH);
        bp.setLocation((WINDOW_WIDTH - menuW) / 2, (WINDOW_HEIGHT - menuH) * 3 / 4);
        bp.setOpaque(false);
        
        Button bCreateRoom = new Button("방 만들기");
        bCreateRoom.addActionListener(e -> {
            if (!ensureConnected()) {
                System.out.println("연결 실패 - 방 만들기 취소");
                return;
            }
            
            hideRoomList();
            pMenuWrapper.setVisible(false);
            pLoading.setVisible(true);
            sendRoomRequest("CREATE");
        });
        bp.add(bCreateRoom);
        
        Button bQuickJoin = new Button("빠른 참여");
        bQuickJoin.addActionListener(e -> {
            if (!ensureConnected()) {
                System.out.println("연결 실패 - 빠른 참여 취소");
                return;
            }
            
            hideRoomList();
            pMenuWrapper.setVisible(false);
            pLoading.setVisible(true);
            sendRoomRequest("QUICK_MATCH");
        });
        bp.add(bQuickJoin);

        Button bShowRoomList = new Button("방 목록 보기");
        bShowRoomList.addActionListener(e -> {
            if (!ensureConnected()) {
                System.out.println("연결 실패 - 방 목록 취소");
                return;
            }
            
            if (roomListVisible) {
                hideRoomList();
            } else {
                showRoomList();
                requestRoomList();
            }
        });
        bp.add(bShowRoomList);
        
        bp.add(new JLabel(""));  
        
        Button bBack = new Button("돌아가기");
        bBack.addActionListener(e -> {
            NetworkManager nm = getNetworkManager();
            if (nm != null && nm.isConnected()) {
                nm.disconnect();
                System.out.println("서버 연결 해제됨");
            }
            goBack();
        });
        bp.add(bBack);
        
        return bp;
    }
    
    private JPanel createRoomListPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(true);
        panel.setBackground(WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(LIGHT_GRAY, 2),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        int panelW = WINDOW_WIDTH / 2;
        int panelH = WINDOW_HEIGHT / 2;
        int panelX = WINDOW_WIDTH / 2 - 120;
        int panelY = (WINDOW_HEIGHT - panelH) * 3 / 4;
        
        panel.setBounds(panelX, panelY, panelW, panelH);
        panel.setVisible(false);
        
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        
        JLabel lHeader = new JLabel("방 목록");
        lHeader.setFont(getFont().deriveFont(20f));
        lHeader.setForeground(BLACK);
        header.add(lHeader, BorderLayout.WEST);
        
        Button bRefresh = new Button("새로고침");
        bRefresh.setFont(getFont().deriveFont(14f));
        bRefresh.addActionListener(e -> requestRoomList());
        header.add(bRefresh, BorderLayout.EAST);
        
        panel.add(header, BorderLayout.NORTH);
        
        // 방 목록 내용
        pRoomListContent = new JPanel();
        pRoomListContent.setLayout(new BoxLayout(pRoomListContent, BoxLayout.Y_AXIS));
        pRoomListContent.setOpaque(false);
        
        JScrollPane scrollPane = new JScrollPane(pRoomListContent);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // 닫기 버튼
        Button bClose = new Button("닫기");
        bClose.addActionListener(e -> hideRoomList());
        panel.add(bClose, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createLoadingPanel() {
        JPanel lp = new JPanel(null);
        lp.setBounds(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);
        lp.setOpaque(false);
        lp.setVisible(false);
        
        JLabel lLoading = new JLabel("입장하는 중...");
        lLoading.setFont(getFont().deriveFont(24f));
        lLoading.setSize(lLoading.getPreferredSize());
        lLoading.setLocation((WINDOW_WIDTH - lLoading.getWidth()) / 2, WINDOW_HEIGHT * 2 / 5);
        lp.add(lLoading);
        
        Button bBack = new Button("취소");
        bBack.setSize(bBack.getPreferredSize());
        bBack.setLocation((WINDOW_WIDTH - bBack.getWidth()) / 2, WINDOW_HEIGHT - 150);
        bBack.addActionListener(e -> {
            cancelRoomRequest();
            pLoading.setVisible(false);
            pMenuWrapper.setVisible(true);
        });
        lp.add(bBack);
        
        return lp;
    }

    // ==========================================
    //        방 목록 UI 제어
    // ==========================================
    
    private void showRoomList() {
        roomListVisible = true;
        
        int menuW = pMenu.getWidth();
        int menuH = pMenu.getHeight();
        int newX = WINDOW_WIDTH / 6 - menuW / 2;
        int newY = (WINDOW_HEIGHT - menuH) * 3 / 4;
        pMenu.setLocation(newX, newY);
        
        pRoomList.setVisible(true);
        
        revalidate();
        repaint();
    }
    
    private void hideRoomList() {
        roomListVisible = false;
        
        int menuW = pMenu.getWidth();
        int menuH = pMenu.getHeight();
        int newX = (WINDOW_WIDTH - menuW) / 2;
        int newY = (WINDOW_HEIGHT - menuH) * 3 / 4;
        pMenu.setLocation(newX, newY);
        
        pRoomList.setVisible(false);
        
        revalidate();
        repaint();
    }
    
    private void updateRoomList(List<RoomSummary> rooms) {
        pRoomListContent.removeAll();
        
        if (rooms.isEmpty()) {
            JLabel lEmpty = new JLabel("생성된 방이 없습니다");
            lEmpty.setFont(getFont().deriveFont(14f));
            lEmpty.setForeground(GRAY);
            lEmpty.setAlignmentX(CENTER_ALIGNMENT);
            pRoomListContent.add(Box.createVerticalGlue());
            pRoomListContent.add(lEmpty);
            pRoomListContent.add(Box.createVerticalGlue());
        } else {
            for (RoomSummary room : rooms) {
                RoomItemPanel roomItem = new RoomItemPanel(room);
                pRoomListContent.add(roomItem);
                pRoomListContent.add(Box.createRigidArea(new Dimension(0, 8)));
            }
        }
        
        pRoomListContent.revalidate();
        pRoomListContent.repaint();
    }

    // ==========================================
    //        서버 통신 메서드
    // ==========================================
    
    private void requestRoomList() {
        System.out.println("방 목록 요청");
        
        NetworkManager nm = getConnectedNetworkManager();
        if (nm == null) return;
        
        RoomActionMessage msg = new RoomActionMessage(
            nm.getPlayerId(),
            RoomActionMessage.LIST_ROOMS
        );
        
        nm.sendMessage(MessageType.ROOM_ACTION, msg);
    }
    
    @Override
    public void onRoomInfo(RoomInfoMessage msg) {
        System.out.println("RoomSelectScene received RoomInfo - switching to LobbyScene");
        
        SwingUtilities.invokeLater(() -> {
            pLoading.setVisible(false);
            
            LobbyScene lobbyScene = new LobbyScene();
            switchToWithoutHistory(lobbyScene);
            
            lobbyScene.updateRoomInfo(msg);
        });
    }
    
    private void joinRoom(String roomId) {
        System.out.println("방 참여 요청: " + roomId);
        
        NetworkManager nm = getConnectedNetworkManager();
        if (nm == null) return;
        
        hideRoomList();
        pMenuWrapper.setVisible(false);
        pLoading.setVisible(true);
        
        RoomActionMessage msg = new RoomActionMessage(
            nm.getPlayerId(),
            RoomActionMessage.JOIN_ROOM
        );
        msg.setRoomId(roomId);
        
        nm.sendMessage(MessageType.ROOM_ACTION, msg);
    }
    
    private void sendRoomRequest(String requestType) {
        System.out.println("방 요청: " + requestType);
        
        NetworkManager nm = getConnectedNetworkManager();
        if (nm == null) {
            pLoading.setVisible(false);
            pMenuWrapper.setVisible(true);
            return;
        }
        
        RoomActionMessage msg = new RoomActionMessage(
            nm.getPlayerId(),
            getActionType(requestType)
        );
        
        nm.sendMessage(MessageType.ROOM_ACTION, msg);
        System.out.println("서버에 " + requestType + " 요청 전송됨");
    }
    
    private void cancelRoomRequest() {
        System.out.println("방 요청 취소");
        
        NetworkManager nm = getConnectedNetworkManager();
        if (nm == null) {
            System.out.println("취소할 요청이 없음 (연결 안 됨)");
            return;
        }
        
        RoomActionMessage cancelMsg = new RoomActionMessage(
            nm.getPlayerId(),
            RoomActionMessage.CANCEL_MATCH
        );
        
        nm.sendMessage(MessageType.ROOM_ACTION, cancelMsg);
        System.out.println("서버에 CANCEL 요청 전송됨");
    }
    
    private int getActionType(String requestType) {
        switch (requestType) {
            case "CREATE":
                return RoomActionMessage.CREATE_ROOM;
            case "QUICK_MATCH":
                return RoomActionMessage.QUICK_MATCH;
            default:
                System.err.println("알 수 없는 요청 타입: " + requestType);
                return RoomActionMessage.ACTION_NONE;
        }
    }

    // ==========================================
    //        NetworkListener 구현
    // ==========================================
    
    @Override
    public void onRoomList(RoomListMessage msg) {
        System.out.println("방 목록 수신: " + msg.getRooms().size() + "개");
        
        SwingUtilities.invokeLater(() -> {
            updateRoomList(msg.getRooms());
        });
    }
    
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(TRANSLUCENT_WHITE);
        g.fillRect(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);
    }
    
    // ==========================================
    //        내부 클래스: RoomItemPanel
    // ==========================================
    
    private class RoomItemPanel extends JPanel {
        
        private final RoomSummary room;
        
        public RoomItemPanel(RoomSummary room) {
            this.room = room;
            
            initLayout();
            initStyle();
            initComponents();
        }
        
        private void initLayout() {
            setLayout(new BorderLayout(10, 0));
            setMaximumSize(new Dimension(Short.MAX_VALUE, 60));
            setAlignmentX(LEFT_ALIGNMENT);
        }
        
        private void initStyle() {
            setOpaque(true);
            setBackground(isDisabled() ? LIGHT_GRAY : WHITE);
            setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LIGHT_GRAY, 1),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
            ));
        }
        
        private void initComponents() {
            add(createInfoPanel(), BorderLayout.CENTER);
            add(createJoinButton(), BorderLayout.EAST);
        }
        
        private JPanel createInfoPanel() {
            JPanel infoPanel = new JPanel();
            infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
            infoPanel.setOpaque(false);
            
            JLabel lRoomId = new JLabel("방 #" + room.getRoomId());
            lRoomId.setFont(getFont().deriveFont(14f));
            lRoomId.setForeground(BLACK);
            infoPanel.add(lRoomId);
            
            JLabel lStatus = new JLabel(buildStatusText());
            lStatus.setFont(getFont().deriveFont(12f));
            lStatus.setForeground(GRAY);
            infoPanel.add(lStatus);
            
            return infoPanel;
        }
        
        private Button createJoinButton() {
            Button bJoin = new Button(getButtonText());
            bJoin.setFont(getFont().deriveFont(14f));
            bJoin.setPreferredSize(new Dimension(80, 35));
            
            if (isDisabled()) {
                bJoin.setEnabled(false);
                bJoin.setButtonColors(WHITE, DARK_GRAY, DARK_GRAY);
            } else {
                bJoin.setButtonColors(LIGHT_GRAY, GRAY, GRAY);
                bJoin.addActionListener(e -> joinRoom(room.getRoomId()));
            }
            
            return bJoin;
        }
        
        private String buildStatusText() {
            StringBuilder sb = new StringBuilder();
            sb.append(room.getHostName())
              .append(" · ")
              .append(room.getPlayerCount())
              .append("/")
              .append(room.getMaxPlayers());
            
            if (room.isPlaying()) {
                sb.append(" · 게임 중");
            }
            
            return sb.toString();
        }
        
        private String getButtonText() {
            if (room.isPlaying()) return "게임중";
            if (room.isFull()) return "만원";
            return "참여";
        }
        
        private boolean isDisabled() {
            return room.isPlaying() || room.isFull();
        }
    }
}