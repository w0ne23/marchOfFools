package marchoffools.client.scene;

import marchoffools.client.scene.Scene;
import marchoffools.client.ui.Button;

import static marchoffools.client.util.Assets.Backgrounds.DEFAULT;
import static marchoffools.client.util.Assets.Colors.*;
import static marchoffools.client.util.Config.*;

import java.awt.Graphics;
import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;

public class TitleScene extends Scene {

	public TitleScene() {
		super(DEFAULT);
		
		JLabel lTitle = new JLabel("Game Title");
		lTitle.setFont(getFont().deriveFont(60f));
		lTitle.setLocation(150, WINDOW_HEIGHT/5);
		lTitle.setSize(lTitle.getPreferredSize());
		add(lTitle);
		
		JPanel p = new JPanel(new GridLayout(5, 0));
		p.setOpaque(false);
        p.setSize(WINDOW_WIDTH*3/10, WINDOW_HEIGHT*2/5);
        p.setLocation(100, WINDOW_HEIGHT*2/5);
		
		Button bTutorial = new Button("튜토리얼");
//		bTutorial.addActionListener(e -> getCallback().switchScene(new TutorialScene()));
        p.add(bTutorial);
        
        Button bSelectRoom = new Button("방 만들기/참여하기");
		bSelectRoom.addActionListener(e -> switchTo(new RoomSelectScene()));
        p.add(bSelectRoom);

		Button bStatus = new Button("점수 통계");
//		bStatus.addActionListener(e -> getCallback().switchScene(new WordsScene()));
        p.add(bStatus);
        
		Button bSettings = new Button("설정");
//		bSettings.addActionListener(e -> getCallback().switchScene(new WordsScene()));
        p.add(bSettings);
        
		Button bExit = new Button("나가기");
		bExit.addActionListener(e -> System.exit(0));
        p.add(bExit);
        
        add(p);
        
		setFocusable(false);
    }

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.setColor(TRANSLUCENT_WHITE);
		g.fillRect(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);
	}
}