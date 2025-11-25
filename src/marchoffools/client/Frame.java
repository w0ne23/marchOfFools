package marchoffools.client;

import static marchoffools.client.core.Config.*;

import java.awt.Dimension;
import java.util.Stack;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import marchoffools.client.core.*;
import marchoffools.client.scenes.*;
import marchoffools.common.message.RoomInfoMessage;
import marchoffools.client.network.NetworkManager;

public class Frame extends JFrame implements SceneContext, NetworkContext{

	private Scene currentScene;
	private Stack<Scene> history = new Stack<>();
	private NetworkManager networkManager;
	
	public Frame() {
		setTitle("MarchOfFools");
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		getContentPane().setPreferredSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));
		pack();
		
		networkManager = new NetworkManager(this);
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
        return networkManager;
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
	
	@Override
	public Scene getCurrentScene() {
	    return currentScene;
	}
	
	public void handleRoomInfoReceived(RoomInfoMessage msg) {
        SwingUtilities.invokeLater(() -> {
            Scene current = getCurrentScene();

            if (current instanceof LobbyScene) {
                System.out.println("LobbyScene 업데이트");
                ((LobbyScene) current).updateRoomInfo(msg);
            } else {
                System.out.println("LobbyScene으로 전환");
                LobbyScene lobbyScene = new LobbyScene();
                switchScene(lobbyScene);
                lobbyScene.updateRoomInfo(msg);
            }
        });
    }
	
}
