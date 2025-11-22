package marchoffools.client;

import static marchoffools.client.util.Config.*;

import java.awt.Dimension;
import java.util.Stack;
import javax.swing.JFrame;

import marchoffools.client.scene.*;
import marchoffools.client.network.ClientSocket;

public class Frame extends JFrame implements SceneContext{

	private Scene currentScene;
	private Stack<Scene> history = new Stack<>();
	private ClientSocket clientSocket;
	
	public Frame() {
		setTitle("MarchOfFools");
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		getContentPane().setPreferredSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));
		pack();
		
		clientSocket = new ClientSocket(this);
		
		// [↓↓↓ 임시 테스트 코드 ↓↓↓]
	    // 서버가 12345 포트로 실행 중이라고 가정
	    String TEST_HOST = "127.0.0.1";
	    int TEST_PORT = 12345;
	    String TEST_PLAYER_NAME = "Tester_" + (int)(Math.random() * 1000); 
	    
	    // 연결 시도
	    if (clientSocket.connect(TEST_HOST, TEST_PORT, TEST_PLAYER_NAME)) {
	        System.out.println("임시 연결 성공! 서버 로그 확인 필요.");
	    }
	    // [↑↑↑ 임시 테스트 코드 ↑↑↑]

		switchScene(new TitleScene());
		
		setVisible(true);
		setResizable(false);
		setFocusable(false);
	}
	
	public ClientSocket getClientSocket() {
        return clientSocket;
    }

	@Override
	public void switchScene(Scene newScene) {
	    if (currentScene != null) {
	    	history.push(currentScene);
	        remove(currentScene);
	    }
		
    	newScene.setContext(this);
    	
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
