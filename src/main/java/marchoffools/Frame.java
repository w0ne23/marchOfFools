package marchoffools;

import static marchoffools.Config.*;

import marchoffools.scenes.*;

import java.awt.Dimension;
import java.util.Stack;

import javax.swing.JFrame;

public class Frame extends JFrame implements SceneContext{

	private Scene currentScene;
	private Stack<Scene> history = new Stack<>();
	
	public Frame() {
		setTitle("MarchOfFools");
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		getContentPane().setPreferredSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));
		pack();

		switchScene(new TitleScene());
		
		setVisible(true);
		setResizable(false);
		setFocusable(false);
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
