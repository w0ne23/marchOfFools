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
	private NetworkManager networkManager;
	
	public Frame() {
		setTitle("MarchOfFools");
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		getContentPane().setPreferredSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));
		pack();
		
		networkManager = new NetworkManager(this);

        ResourceManager.loadAllResources();

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
	public void switchSceneWithoutHistory(Scene newScene) {
	    if (newScene == null) {
	        System.err.println("ERROR: Cannot switch to null scene");
	        return;
	    }
	    
	    if (currentScene != null) {
	        currentScene.onExit();
	        remove(currentScene);
	    }
	    
	    newScene.setContext(this, this);
	    currentScene = newScene;
	    add(newScene);
	    
	    revalidate();
	    repaint();
	}
	
	@Override
	public void clearHistory() {
	    for (Scene scene : history) {
	        scene.onExit();
	    }
	    history.clear();
	}

	@Override
	public void goBack() {
	    if (!history.isEmpty()) {
	        Scene prev = history.pop();
	        
	        if (currentScene != null) {
	            currentScene.onExit();
	            remove(currentScene);
	        }
	        
	        prev.setContext(this, this);
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
	
}
