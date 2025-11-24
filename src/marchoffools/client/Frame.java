package marchoffools.client;

import static marchoffools.client.core.Config.*;

import java.awt.Dimension;
import java.util.Stack;
import javax.swing.JFrame;

import marchoffools.client.core.*;
import marchoffools.client.scenes.*;
import marchoffools.client.network.NetworkManager;

public class Frame extends JFrame implements SceneContext, NetworkContext{

	private Scene currentScene;
	private Stack<Scene> history = new Stack<>();
	private NetworkManager NetworkManager;
	
	public Frame() {
		setTitle("MarchOfFools");
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		getContentPane().setPreferredSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));
		pack();
		
		NetworkManager = new NetworkManager(this);
//		
//		// [↓↓↓ 임시 테스트 코드 ↓↓↓]
//	    // 서버가 12345 포트로 실행 중이라고 가정
//	    String TEST_HOST = "127.0.0.1";
//	    int TEST_PORT = 12345;
//	    String TEST_PLAYER_NAME = "Tester_" + (int)(Math.random() * 1000); 

		switchScene(new TitleScene());
		
		setVisible(true);
		setResizable(false);
		setFocusable(false);
	}
	
	@Override
	public NetworkManager getNetworkManager() {
        return NetworkManager;
    }

	@Override
	public void switchScene(Scene newScene) {
	    if (newScene == null) {
	        System.err.println("ERROR: Cannot switch to null scene");
	        return;
	    }
	    
	    if (currentScene != null) {
	    	history.push(currentScene);
	        remove(currentScene);
	    }
		
    	newScene.setContext(this, this);
    	
        currentScene = newScene;
        add(newScene);
        
        revalidate();
        repaint();
	}

	@Override
	public void goBack() {
	    if (!history.isEmpty()) {
	        Scene prev = history.pop();
	        
	        remove(currentScene);
	        
	        currentScene = prev;
	        add(prev);
	        
	        revalidate();
	        repaint();
	    }
	}
	
}
