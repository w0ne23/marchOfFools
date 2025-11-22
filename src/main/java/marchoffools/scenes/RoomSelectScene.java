package marchoffools.scenes;

import static marchoffools.Config.*;
import static marchoffools.Assets.Backgrounds.DEFAULT;
import static marchoffools.Assets.Colors.*;

import marchoffools.Scene;
import marchoffools.Button;

import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class RoomSelectScene extends Scene {

	private JPanel pMenu, pInput, pLoading;
	
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
				sendRoomRequest();
			}
		});
        bp.add(bCreateRoom);
        
        Button bQuickJoin = new Button("빠른 참여");
		bQuickJoin.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
		        pMenu.setVisible(false);
		        pLoading.setVisible(true);
				sendRoomRequest();
			}
		});
        bp.add(bQuickJoin);

		Button bJoinWithID = new Button("방 ID로 참여");
		bJoinWithID.addActionListener(e -> pInput.setVisible(true));
        bp.add(bJoinWithID);

        pInput = new JPanel(new FlowLayout());
        pInput.setOpaque(false);
        
        // 코드 형식에 맞는 문자만 입력되도록
        JTextField t = new JTextField(8);
        t.setHorizontalAlignment(0);
        t.setFont(getFont().deriveFont(28f));
        pInput.add(t);
        
        Button bGo = new Button("입장");
        bGo.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
		        pMenu.setVisible(false);
		        pLoading.setVisible(true);
				sendRoomRequest();
			}
		});
        pInput.add(bGo);

        Button bCancle = new Button("취소");
        bCancle.addActionListener(e -> pInput.setVisible(false));
        pInput.add(bCancle);
        
        bp.add(pInput);
        pInput.setVisible(false);
        
        bp.add(new JLabel(""));
        
		Button bBack = new Button("돌아가기");
		bBack.addActionListener(e -> goBack());
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
				sendRoomRequest(); // 입장 요청 취소 또는 퇴장 요청
		        pLoading.setVisible(false);
		        pMenu.setVisible(true);
			}
		});
        lp.add(bBack);
        
		return lp;
	}
	
	private void sendRoomRequest() {

//        SwingWorker...
//        worker.execute();
        
    }
	
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.setColor(TRANSLUCENT_WHITE);
		g.fillRect(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);
	}
}