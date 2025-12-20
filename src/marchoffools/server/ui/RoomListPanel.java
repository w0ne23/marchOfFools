package marchoffools.server.ui;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import marchoffools.server.game.Room;

/**
 * 방 목록 패널
 */
public class RoomListPanel extends JPanel {
    
    private ServerGUI parent;
    private JPanel roomButtonsPanel;
    private Map<String, JButton> roomButtons = new HashMap<>();
    private String selectedRoomId;
    
    public RoomListPanel(ServerGUI parent) {
        this.parent = parent;
        initComponents();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("방 목록"));
        setPreferredSize(new Dimension(200, 0));
        setBackground(new Color(250, 250, 250));
        
        // 방 버튼 패널
        roomButtonsPanel = new JPanel();
        roomButtonsPanel.setLayout(new BoxLayout(roomButtonsPanel, BoxLayout.Y_AXIS));
        roomButtonsPanel.setBackground(Color.WHITE);
        
        JScrollPane scrollPane = new JScrollPane(roomButtonsPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setBorder(null);
        add(scrollPane, BorderLayout.CENTER);
        
        // 하단 버튼 패널
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
        bottomPanel.setBackground(new Color(250, 250, 250));
        
        JButton refreshBtn = new JButton("새로고침");
        refreshBtn.addActionListener(e -> parent.refreshRooms());
        bottomPanel.add(refreshBtn);
        
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    public void addRoom(String roomId) {
        if (roomButtons.containsKey(roomId)) return;
        
        JButton btn = createRoomButton(roomId);
        roomButtons.put(roomId, btn);
        roomButtonsPanel.add(btn);
        roomButtonsPanel.add(Box.createVerticalStrut(2));
        roomButtonsPanel.revalidate();
        roomButtonsPanel.repaint();
    }
    
    public void removeRoom(String roomId) {
        JButton btn = roomButtons.remove(roomId);
        if (btn != null) {
            int index = -1;
            for (int i = 0; i < roomButtonsPanel.getComponentCount(); i++) {
                if (roomButtonsPanel.getComponent(i) == btn) {
                    index = i;
                    break;
                }
            }
            if (index >= 0) {
                roomButtonsPanel.remove(index);
                if (index < roomButtonsPanel.getComponentCount()) {
                    roomButtonsPanel.remove(index);
                }
            }
            roomButtonsPanel.revalidate();
            roomButtonsPanel.repaint();
        }
    }
    
    public void updateRoom(Room room) {
        JButton btn = roomButtons.get(room.getRoomId());
        if (btn != null) {
            String status = getStatusText(room);
            String text = String.format("<html><center>%s<br><b>%s</b> (%d/2)</center></html>", 
                status, room.getRoomId(), room.getPlayerCount());
            btn.setText(text);
            
            // 상태별 배경색
            if (room.isPlaying()) {
                btn.setBackground(new Color(255, 200, 200));
            } else if (room.isFull()) {
                btn.setBackground(new Color(200, 255, 200));
            } else {
                btn.setBackground(new Color(255, 255, 200));
            }
        }
    }
    
    public void clear() {
        roomButtons.clear();
        roomButtonsPanel.removeAll();
        roomButtonsPanel.revalidate();
        roomButtonsPanel.repaint();
        selectedRoomId = null;
    }
    
    private JButton createRoomButton(String roomId) {
        JButton btn = new JButton();
        btn.setText(String.format("<html><center>[대기]<br><b>%s</b> (0/2)</center></html>", roomId));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setBackground(new Color(255, 255, 200));
        btn.setOpaque(true);
        btn.setBorderPainted(true);
        btn.setFocusPainted(false);
        
        btn.addActionListener(e -> {
            selectedRoomId = roomId;
            parent.selectRoom(roomId);
            highlightSelected(roomId);
        });
        
        return btn;
    }
    
    private void highlightSelected(String roomId) {
        for (Map.Entry<String, JButton> entry : roomButtons.entrySet()) {
            JButton btn = entry.getValue();
            if (entry.getKey().equals(roomId)) {
                btn.setBorder(BorderFactory.createLineBorder(new Color(0, 120, 215), 3));
            } else {
                btn.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
            }
        }
    }
    
    private String getStatusText(Room room) {
        if (room.isPlaying()) return "[게임 중]";
        if (room.isFull()) return "[준비 가능]";
        return "[대기]";
    }
}