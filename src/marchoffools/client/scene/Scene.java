package marchoffools.client.scene;

import java.awt.Graphics;
import java.awt.Image;

import javax.swing.JPanel;
import javax.swing.ImageIcon;

public abstract class Scene extends JPanel {
	
	private SceneContext context = null;

	protected ImageIcon icon;
	protected Image img;
	

	public Scene(String icon) {
		this.icon = new ImageIcon(icon);
		img = this.icon.getImage();
		
		setLayout(null);
	}

	public void setContext(SceneContext context) {
		this.context = context;
	}
	
	public SceneContext getContext() {
        return context;
    }

    protected void switchTo(Scene newScene) {
        if (context != null) {
            context.switchScene(newScene);
        }
    }
    
    protected void goBack() {
    	if (context != null) {
    		context.goBack();
    	}
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
