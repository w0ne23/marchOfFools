package marchoffools.client.scene;

import marchoffools.client.Frame;
import marchoffools.client.network.ClientSocket;
import marchoffools.client.scene.Scene;
import marchoffools.client.ui.Button;

import static marchoffools.client.util.Assets.Backgrounds.DEFAULT;
import static marchoffools.client.util.Assets.Colors.*;
import static marchoffools.client.util.Config.*;

import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

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
		
		connectToServer();

		setFocusable(false);
    }
	
	private void connectToServer() {
	    System.out.println("===== connectToServer 호출됨 =====");  // 디버깅용
	    
	    Frame frame = (Frame) getContext();
	    
	    if (frame == null) {
	        System.err.println("오류: Frame이 null입니다!");
	        return;
	    }
	    
	    ClientSocket socket = frame.getClientSocket();
	    
	    if (socket == null) {
	        System.err.println("오류: ClientSocket이 null입니다!");
	        return;
	    }
	    
	    // 이미 연결되어 있으면 건너뛰기
	    if (socket.isConnected()) {
	        System.out.println("이미 서버에 연결되어 있습니다.");
	        return;
	    }
	    
	    // 플레이어 이름 입력
	    String playerName = JOptionPane.showInputDialog(
	        this, 
	        "플레이어 이름을 입력하세요:", 
	        "서버 연결", 
	        JOptionPane.PLAIN_MESSAGE
	    );
	    
	    // 취소하면 타이틀로 돌아가기
	    if (playerName == null) {
	        System.out.println("플레이어 이름 입력 취소됨");
	        goBack();
	        return;
	    }
	    
	    // 빈 이름이면 자동 생성
	    if (playerName.trim().isEmpty()) {
	        playerName = "Player_" + (System.currentTimeMillis() % 10000);
	        System.out.println("자동 생성된 이름: " + playerName);
	    }
	    
	    System.out.println("서버 연결 시도: " + playerName);
	    
	    // 서버 연결
	    boolean success = socket.connect("localhost", 12345, playerName);
	    
	    // 연결 실패 시 타이틀로 돌아가기
	    if (!success) {
	        System.out.println("서버 연결 실패 - 타이틀로 복귀");
	        goBack();
	    } else {
	        System.out.println("서버 연결 성공!");
	    }
	}

	private JPanel createButtonPanel() {
		JPanel bp = new JPanel(new GridLayout(6, 0));
		
		int menuW = WINDOW_WIDTH*3/5;
		int menuH = WINDOW_HEIGHT/2;
		
		bp.setSize(menuW, menuH);
		bp.setLocation((WINDOW_WIDTH - menuW)/2, (WINDOW_HEIGHT - menuH)*3/4);
		bp.setOpaque(false);
		
		Button bCreateRoom = new Button("방 만들기");
		bCreateRoom.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
		        pMenu.setVisible(false);
		        pLoading.setVisible(true);
		        sendRoomRequest("CREATE");
			}
		});
        bp.add(bCreateRoom);
        
        Button bQuickJoin = new Button("빠른 참여");
		bQuickJoin.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
		        pMenu.setVisible(false);
		        pLoading.setVisible(true);
		        sendRoomRequest("QUICK_MATCH");
			}
		});
        bp.add(bQuickJoin);

		Button bJoinWithID = new Button("방 ID로 참여");
		bJoinWithID.addActionListener(e -> pInput.setVisible(true));
        bp.add(bJoinWithID);

        pInput = new JPanel(new FlowLayout());
        pInput.setOpaque(false);
        
        // 코드 형식에 맞는 문자만 입력되도록
        tfRoomId = new JTextField(8);  // 필드 초기화
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
            tfRoomId.setText("");  // 입력 초기화
            pInput.setVisible(false);
        });
        pInput.add(bCancel);
        
        bp.add(pInput);
        pInput.setVisible(false);
        
        bp.add(new JLabel(""));
        
		Button bBack = new Button("돌아가기");
		bBack.addActionListener(e -> {
            // 서버 연결 종료
            Frame frame = (Frame) getContext();
            frame.getClientSocket().disconnect();
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
	
	private void sendRoomRequest(String requestType) {
        System.out.println("방 요청: " + requestType);
        
        // TODO: Phase 2에서 구현
        // 지금은 알림만 표시
        JOptionPane.showMessageDialog(
            this,
            requestType + " 기능은 Phase 2에서 구현됩니다!",
            "알림",
            JOptionPane.INFORMATION_MESSAGE
        );
        
        // 로딩 패널 숨기고 메뉴 다시 표시
        pLoading.setVisible(false);
        pMenu.setVisible(true);
        
        /*
        // Phase 2에서 이렇게 구현 예정:
        
        Frame frame = (Frame) getContext();
        ClientSocket socket = frame.getClientSocket();
        
        RoomActionMessage msg = new RoomActionMessage(
            socket.getPlayerId(),
            getActionType(requestType)
        );
        
        if ("JOIN".equals(requestType)) {
            msg.setRoomId(tfRoomId.getText().trim());
        }
        
        socket.sendMessage(MessageType.ROOM_ACTION, msg);
        */
    }
	
	// 요청 취소
    private void cancelRoomRequest() {
        System.out.println("방 요청 취소");
        
        // TODO: Phase 2에서 구현
        // 서버에 취소 메시지 전송 (필요한 경우)
    }
    
    /*
    // Phase 2에서 추가 예정
    private int getActionType(String requestType) {
        switch (requestType) {
            case "CREATE":
                return RoomActionMessage.CREATE_ROOM;
            case "JOIN":
                return RoomActionMessage.JOIN_ROOM;
            case "QUICK_MATCH":
                return RoomActionMessage.QUICK_MATCH;
            default:
                return RoomActionMessage.ACTION_NONE;
        }
    }
    */
	
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.setColor(TRANSLUCENT_WHITE);
		g.fillRect(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);
	}
}