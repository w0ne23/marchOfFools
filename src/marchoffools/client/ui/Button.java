package marchoffools.client.ui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JButton;

import static marchoffools.client.core.Assets.Colors.*; 

public class Button extends JButton {

    // 세 가지 상태에 대한 색상 저장
    private Color defaultColor;
    private Color hoverColor;
    private Color clickColor;

    public Button(String text) {
        super(text);
        
        setFont(getFont().deriveFont(28f));
        
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false); // 배경을 paintComponent에서 직접 그리기 위해 false 설정
        setOpaque(false);
        setFocusable(false);

        this.defaultColor = TRANSPARENT;
        this.hoverColor = TRANSLUCENT_WHITE;
        this.clickColor = WHITE;
        
        setBackground(defaultColor);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                setBackground(hoverColor);
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                setBackground(defaultColor);
                repaint();
            }
            
            @Override
            public void mousePressed(MouseEvent e) {
                setBackground(clickColor);
                repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (contains(e.getPoint())) {
                    setBackground(hoverColor);
                } else {
                    setBackground(defaultColor);
                }
                repaint();
            }
        });
    }

    /**
     * 버튼의 상태별 색상을 설정합니다.
     * @param defaultColor 기본 배경색
     * @param hoverColor 마우스를 올렸을 때 색상
     * @param clickColor 눌렀을 때 색상
     */
    public void setButtonColors(Color defaultColor, Color hoverColor, Color clickColor) {
        this.defaultColor = defaultColor;
        this.hoverColor = hoverColor;
        this.clickColor = clickColor;
        
        setBackground(defaultColor);
    }

    @Override
    protected void paintComponent(Graphics g) {
        g.setColor(getBackground());
        g.fillRect(0, 0, getWidth(), getHeight());
        
        super.paintComponent(g);
    }
}