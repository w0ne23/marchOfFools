package marchoffools.server.ui;

import javax.swing.*;
import java.awt.*;
import java.net.InetAddress;

import marchoffools.common.config.ServerConfig;

/**
 * 서버 정보 표시 및 제어 패널
 */
public class ServerInfoPanel extends JPanel {
    
    private ServerGUI parent;
    
    private JLabel statusLabel;
    private JLabel ipLabel;
    private JLabel statsLabel;
    
    private JButton startBtn;
    private JButton stopBtn;
    
    private JSpinner portSpinner;
    private JCheckBox debugCheckBox;
    
    public ServerInfoPanel(ServerGUI parent) {
        this.parent = parent;
        initComponents();
        layoutComponents();
    }
    
    private void initComponents() {
        // 상태 라벨
        statusLabel = new JLabel("🔴 중지됨");
        statusLabel.setFont(new Font("맑은 고딕", Font.BOLD, 15));
        
        // IP 라벨
        try {
            String ip = InetAddress.getLocalHost().getHostAddress();
            ipLabel = new JLabel("IP: " + ip);
        } catch (Exception e) {
            ipLabel = new JLabel("IP: 알 수 없음");
        }
        
        // 통계 라벨
        statsLabel = new JLabel("📊 방: 0개 | 접속자: 0명 | 게임 중: 0개");
        
        // 포트 입력
        int defaultPort = ServerConfig.getServerPort();
        portSpinner = new JSpinner(new SpinnerNumberModel(defaultPort, 1024, 65535, 1));
        portSpinner.setPreferredSize(new Dimension(80, 25));
        
        // 디버그 모드 체크박스
        debugCheckBox = new JCheckBox("디버그");
        debugCheckBox.addActionListener(e -> {
            ServerLogger.getInstance().setDebugMode(debugCheckBox.isSelected());
        });
        
        // 시작 버튼
        startBtn = new JButton("\u25B6 시작");
        startBtn.setBackground(new Color(76, 175, 80));
        startBtn.addActionListener(e -> {
            int port = (Integer) portSpinner.getValue();
            parent.startServer(port);
        });
        
        // 중지 버튼
        stopBtn = new JButton("\u25FC 중지");
        stopBtn.setEnabled(false);
        stopBtn.addActionListener(e -> parent.stopServer());
    }
    
    private void layoutComponents() {
        setLayout(new FlowLayout(FlowLayout.LEFT, 15, 10));
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEtchedBorder(),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        setBackground(new Color(245, 245, 245));
        
        add(new JLabel("💻 서버"));
        add(statusLabel);
        add(createSeparator());
        add(ipLabel);
        add(new JLabel("포트:"));
        add(portSpinner);
        add(createSeparator());
        add(statsLabel);
        add(createSeparator());
        add(debugCheckBox);
        add(Box.createHorizontalStrut(20));
        add(startBtn);
        add(stopBtn);
    }
    
    private JSeparator createSeparator() {
        JSeparator sep = new JSeparator(SwingConstants.VERTICAL);
        sep.setPreferredSize(new Dimension(1, 25));
        return sep;
    }
    
    public void setServerStatus(boolean running, int port) {
        if (running) {
            statusLabel.setText("🟢 실행 중 (:" + port + ")");
            statusLabel.setForeground(new Color(0, 128, 0));
            startBtn.setEnabled(false);
            stopBtn.setEnabled(true);
            portSpinner.setEnabled(false);
        } else {
            statusLabel.setText("🔴 중지됨");
            statusLabel.setForeground(new Color(192, 0, 0));
            startBtn.setEnabled(true);
            stopBtn.setEnabled(false);
            portSpinner.setEnabled(true);
        }
    }
    
    public void updateStats(int roomCount, int playerCount, int gameCount) {
        statsLabel.setText(String.format(
            "📊 방: %d개 | 접속자: %d명 | 게임 중: %d개",
            roomCount, playerCount, gameCount
        ));
    }
}