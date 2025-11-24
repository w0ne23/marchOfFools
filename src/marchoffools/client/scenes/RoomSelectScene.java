package marchoffools.client.scenes;

import static marchoffools.common.message.RoomActionMessage.*;

import marchoffools.client.network.NetworkManager;
import marchoffools.client.core.Scene;
import marchoffools.client.ui.Button;

import static marchoffools.client.core.Assets.Backgrounds.DEFAULT;
import static marchoffools.client.core.Assets.Colors.*;
import static marchoffools.client.core.Config.*;

import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import marchoffools.common.protocol.MessageType;
import marchoffools.common.message.RoomActionMessage;

public class RoomSelectScene extends Scene {

    private JPanel pMenu, pInput, pLoading;
    private JTextField tfRoomId;
    
    public RoomSelectScene() {
        super(DEFAULT);

        JLabel lTitle = new JLabel("게임 참여");
        lTitle.setFont(getFont().deriveFont(52f));
        lTitle.setSize(lTitle.getPreferredSize());
        lTitle.setLocation((WINDOW_WIDTH - lTitle.getWidth())/2, (WINDOW_HEIGHT - lTitle.getHeight())/8);
        add(lTitle);
        
        pMenu = createButtonPanel();
        pLoading = createLoadingPanel();
        
        add(pMenu);
        add(pLoading);
        
        setFocusable(false);
    }

    // 연결 보장 메서드
    private boolean ensureConnected() {
        System.out.println("ensureConnected() called");
        
        NetworkManager nm = getNetworkManager();
        
        if (nm == null) {
            System.err.println("네트워크 매니저를 사용할 수 없습니다.");
            return false;
        }
        
        // 이미 연결되어 있으면 OK
        if (nm.isConnected()) {
            System.out.println("이미 서버에 연결되어 있습니다.");
            return true;
        }
        
        // 연결 시도
        return connectToServer(nm);
    }
    
    private boolean connectToServer(NetworkManager nm) {
        System.out.println("connectToServer() called");
        
        if (nm == null) {
            System.err.println("오류: NetworkManager가 null입니다!");
            return false;
        }
        
        // 플레이어 이름 입력
        String playerName = JOptionPane.showInputDialog(
            this, 
            "플레이어 이름을 입력하세요:", 
            "서버 연결", 
            JOptionPane.PLAIN_MESSAGE
        );
        
        // 입력 취소
        if (playerName == null) {
            System.out.println("플레이어 이름 입력 취소됨");
            return false;
        }
        
        // 빈 이름이면 자동 생성
        if (playerName.trim().isEmpty()) {
            playerName = "Player_" + (System.currentTimeMillis() % 10000);
            System.out.println("자동 생성된 이름: " + playerName);
        }
        
        System.out.println("서버 연결 시도: " + playerName);
        
        // 서버 연결
        boolean success = nm.connect("localhost", 12345, playerName);

        if (success) {
            System.out.println("서버 연결 성공!");
        } else {
            System.out.println("서버 연결 실패");
        }
        
        return success;
    }

    private JPanel createButtonPanel() {
        JPanel bp = new JPanel(new GridLayout(6, 0));
        
        int menuW = WINDOW_WIDTH*3/5;
        int menuH = WINDOW_HEIGHT/2;
        
        bp.setSize(menuW, menuH);
        bp.setLocation((WINDOW_WIDTH - menuW)/2, (WINDOW_HEIGHT - menuH)*3/4);
        bp.setOpaque(false);
        
        // ✅ 방 만들기 버튼
        Button bCreateRoom = new Button("방 만들기");
        bCreateRoom.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // ✅ 연결 확인
                if (!ensureConnected()) {
                    System.out.println("연결 실패 - 방 만들기 취소");
                    return;
                }
                
                pMenu.setVisible(false);
                pLoading.setVisible(true);
                sendRoomRequest("CREATE");
            }
        });
        bp.add(bCreateRoom);
        
        // ✅ 빠른 참여 버튼
        Button bQuickJoin = new Button("빠른 참여");
        bQuickJoin.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // ✅ 연결 확인
                if (!ensureConnected()) {
                    System.out.println("연결 실패 - 빠른 참여 취소");
                    return;
                }
                
                pMenu.setVisible(false);
                pLoading.setVisible(true);
                sendRoomRequest("QUICK_MATCH");
            }
        });
        bp.add(bQuickJoin);

        // ✅ 방 ID로 참여 버튼
        Button bJoinWithID = new Button("방 ID로 참여");
        bJoinWithID.addActionListener(e -> {
            // ✅ 연결 확인
            if (!ensureConnected()) {
                System.out.println("연결 실패 - 방 ID 참여 취소");
                return;
            }
            
            pInput.setVisible(true);
        });
        bp.add(bJoinWithID);

        // 방 ID 입력 패널
        pInput = new JPanel(new FlowLayout());
        pInput.setOpaque(false);
        
        tfRoomId = new JTextField(8);
        tfRoomId.setHorizontalAlignment(0);
        tfRoomId.setFont(getFont().deriveFont(28f));
        pInput.add(tfRoomId);
        
        Button bGo = new Button("입장");
        bGo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String roomId = tfRoomId.getText().trim();
                
                // 방 ID 입력 확인
                if (roomId.isEmpty()) {
                    JOptionPane.showMessageDialog(
                        RoomSelectScene.this,
                        "방 ID를 입력해주세요.",
                        "입력 오류",
                        JOptionPane.WARNING_MESSAGE
                    );
                    return;
                }
                
                pInput.setVisible(false);
                pMenu.setVisible(false);
                pLoading.setVisible(true);
                sendRoomRequest("JOIN");
            }
        });
        pInput.add(bGo);

        Button bCancel = new Button("취소");
        bCancel.addActionListener(e -> {
            tfRoomId.setText("");
            pInput.setVisible(false);
        });
        pInput.add(bCancel);
        
        bp.add(pInput);
        pInput.setVisible(false);
        
        bp.add(new JLabel(""));
        
        // ✅ 뒤로가기 버튼 (연결 해제 추가)
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
        bBack.setLocation((WINDOW_WIDTH - bBack.getWidth())/2, WINDOW_HEIGHT - 150);
        bBack.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cancelRoomRequest();
                pLoading.setVisible(false);
                pMenu.setVisible(true);
            }
        });
        lp.add(bBack);
        
        return lp;
    }
    
    // ✅ Phase 2 구현: 방 요청 전송
    private void sendRoomRequest(String requestType) {
        System.out.println("방 요청: " + requestType);
        
        NetworkManager nm = getNetworkManager();
        
        // NetworkManager 체크
        if (nm == null) {
            System.err.println("오류: NetworkManager가 null입니다!");
            pLoading.setVisible(false);
            pMenu.setVisible(true);
            JOptionPane.showMessageDialog(
                this,
                "네트워크 매니저를 사용할 수 없습니다.",
                "오류",
                JOptionPane.ERROR_MESSAGE
            );
            return;
        }
        
        // 연결 상태 체크
        if (!nm.isConnected()) {
            System.err.println("오류: 서버에 연결되어 있지 않습니다!");
            pLoading.setVisible(false);
            pMenu.setVisible(true);
            JOptionPane.showMessageDialog(
                this,
                "서버에 연결되어 있지 않습니다.",
                "연결 오류",
                JOptionPane.ERROR_MESSAGE
            );
            return;
        }
        
        // RoomActionMessage 생성
        RoomActionMessage msg = new RoomActionMessage(
            nm.getPlayerId(),
            getActionType(requestType)
        );
        
        // JOIN 요청인 경우 방 ID 설정
        if ("JOIN".equals(requestType)) {
            String roomId = tfRoomId.getText().trim();
            msg.setRoomId(roomId);
            System.out.println("방 ID 설정: " + roomId);
        }
        
        // 서버에 메시지 전송
        nm.sendMessage(MessageType.ROOM_ACTION, msg);
        System.out.println("서버에 " + requestType + " 요청 전송됨");
        
        // TODO: 서버 응답 대기
        // 현재는 응답 처리가 NetworkManager.handleRoomInfo()에서 이루어짐
        // 나중에 RoomScene으로 전환하는 로직 추가 필요
    }
    
    // ✅ Phase 2 구현: 요청 취소
    private void cancelRoomRequest() {
        System.out.println("방 요청 취소");
        
        NetworkManager nm = getNetworkManager();
        
        if (nm == null || !nm.isConnected()) {
            System.out.println("취소할 요청이 없음 (연결 안 됨)");
            return;
        }
        
        // CANCEL 액션 메시지 전송
        RoomActionMessage cancelMsg = new RoomActionMessage(
            nm.getPlayerId(),
            RoomActionMessage.CANCEL_MATCH
        );
        
        nm.sendMessage(MessageType.ROOM_ACTION, cancelMsg);
        System.out.println("서버에 CANCEL 요청 전송됨");
    }
    
    // ✅ Phase 2 구현: 요청 타입 변환
    private int getActionType(String requestType) {
        switch (requestType) {
            case "CREATE":
                return RoomActionMessage.CREATE_ROOM;
            case "JOIN":
                return RoomActionMessage.JOIN_ROOM;
            case "QUICK_MATCH":
                return RoomActionMessage.QUICK_MATCH;
            default:
                System.err.println("알 수 없는 요청 타입: " + requestType);
                return RoomActionMessage.ACTION_NONE;
        }
    }
    
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(TRANSLUCENT_WHITE);
        g.fillRect(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);
    }
}