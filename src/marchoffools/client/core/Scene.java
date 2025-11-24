package marchoffools.client.core;

import java.awt.Graphics;
import java.awt.Image;

import javax.swing.JPanel;

import marchoffools.client.network.NetworkManager;

import javax.swing.ImageIcon;

public abstract class Scene extends JPanel {
	
	private SceneContext sceneContext;
	private NetworkContext networkContext;

	protected ImageIcon icon;
	protected Image img;
	
	
	public Scene(String icon) {
		this.icon = new ImageIcon(icon);
		img = this.icon.getImage();
	    if (img == null || img.getWidth(null) == -1) {
	        System.err.println("Warning: Failed to load image: " + icon);
	    }
		
		setLayout(null);
		setOpaque(true);
	}

	public void setContext(SceneContext sceneContext, NetworkContext networkContext) {
		this.sceneContext = sceneContext;
		this.networkContext = networkContext;
	}
	
    protected void switchTo(Scene newScene) {
        if (sceneContext != null) {
            sceneContext.switchScene(newScene);
        }
    }
    
    protected void goBack() {
    	if (sceneContext != null) {
    		sceneContext.goBack();
    	}
    }
    
    protected NetworkManager getNetworkManager() {
    	return networkContext != null ? networkContext.getNetworkManager() : null;
    }
    
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
        
	    if (img != null) {
	        int pw = getWidth();
	        int ph = getHeight();
	        int iw = img.getWidth(this);
	        int ih = img.getHeight(this);
	        
	        double scale =
	        		Math.max((double) pw / iw, (double) ph / ih);
	        
	        int w = (int) (iw * scale);
	        int h = (int) (ih * scale);
	        int x = (pw - w) / 2;
	        int y = (ph - h) / 2;
	        
	        g.drawImage(img, x, y, w, h, this);
	    }
	}

}
