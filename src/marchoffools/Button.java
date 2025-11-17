package marchoffools;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JButton;

// 일단은 단일 클래스로 구현 후 디자인작업...
public class Button extends JButton{

	public Button(String text) {
		super(text);
		
		setFont(getFont().deriveFont(28f));
		
		setBackground(Assets.Colors.TRANSLUCENT_WHITE);
		setFocusPainted(false);
		setBorderPainted(false);
		setOpaque(false);
		setContentAreaFilled(false);
		setFocusable(false);
		
		addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                setContentAreaFilled(true);
                if (getParent() != null) {
                    getParent().repaint();
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                setContentAreaFilled(false);
                if (getParent() != null) {
                    getParent().repaint();
                }
            }
            
            @Override
            public void mouseReleased(MouseEvent e) {
                if (getParent() != null) {
                    getParent().repaint();
                }
            }
            
        });
	}
	
}
