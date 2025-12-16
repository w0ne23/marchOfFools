package marchoffools.client.ui;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Color;
import java.awt.BasicStroke;
import java.awt.Font;

import marchoffools.client.core.ResourceManager;

/**
 * 게임 내 모든 시각적 객체를 표현하는 통합 클래스
 */
public class Sprite {
    
    // 타입 상수
	// 위치가 적절하지 않은 것 같음.
    public static final int TYPE_PLAYER = 0;
    public static final int TYPE_OBSTACLE = 1;
    public static final int TYPE_SKILL = 2;
    
    // 위치 및 크기
    protected double x;
    protected double y;
    protected int width;
    protected int height;
    
    // 이미지
    protected Image image;
    protected String imageKey;  // ResourceManager의 키
    
    // 공통 속성
    protected boolean visible;
    protected int type;
    
    // 대체 색상(이미지가 없을 떄)
    protected Color fillColor;
    protected Color strokeColor;
    protected int strokeWidth;
    protected boolean dashed;
    
    // 텍스트
    protected String label;
    protected Color labelColor;
    
    // 객체별 데이터
    protected int subType;        // 장애물 타입, 스킬 ID 등
    protected int state;          // 플레이어 상태 등
    protected boolean destroyed;  // 장애물 파괴 여부
    protected boolean invincible; // 플레이어 무적 여부
    
    public Sprite(int type, double x, double y, int width, int height) {
        this.type = type;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.visible = true;
        this.strokeWidth = 2;
        this.labelColor = Color.WHITE;
    }
    
    // ResourceManager를 통해 이미지 설정
    public void setImage(String key) {
        this.image = ResourceManager.getImage(key);
    }
    
    // 스프라이트 그리기
    // 추후 분기 없이 구현....
    public void draw(Graphics2D g2d) {
        if (!visible) return;
        
        int ix = (int) x;
        int iy = (int) y;
        
        switch (type) {
            case TYPE_PLAYER:
                drawPlayer(g2d, ix, iy);
                break;
            case TYPE_OBSTACLE:
                drawObstacle(g2d, ix, iy);
                break;
            case TYPE_SKILL:
                drawSkillRange(g2d, ix, iy);
                break;
        }
    }
    
    private void drawPlayer(Graphics2D g2d, int ix, int iy) {
        // 무적 상태 표시
        if (invincible) {
            long time = System.currentTimeMillis();
            
            // 노란색 테두리 (점선)
            g2d.setColor(new Color(255, 255, 0, 200));
            g2d.setStroke(new BasicStroke(3, BasicStroke.CAP_BUTT, 
                BasicStroke.JOIN_BEVEL, 0, new float[]{9}, 0));
            g2d.drawRect(ix - 10, iy - 10, width + 20, height + 20);
            
            // 깜빡임
            if ((time / 100) % 2 == 0) {
                g2d.setColor(new Color(255, 255, 0, 100));
                g2d.fillRect(ix - 5, iy - 5, width + 10, height + 10);
            }
        }
        
        // 이미지 또는 색상
        // 사각 히트박스라 이미지가 히트박스보다 커야 판정이 괜찮아짐
        // color 하드코딩... 캐릭터 선택지 늘리면 GameCharacter 클래스로 저장?
        if (image != null) {
            int displayWidth = (int)(width * 1.2);
            int displayHeight = (int)(height * 1.2);
            int offsetX = (width - displayWidth) / 2;
            int offsetY = (height - displayHeight) / 2;
            g2d.drawImage(image, ix + offsetX, iy + offsetY, displayWidth, displayHeight, null);
        } else {
            g2d.setColor(fillColor != null ? fillColor : new Color(120, 170, 255));
            g2d.fillRect(ix, iy, width, height);
        }
//        
//        // 테두리
//        g2d.setColor(Color.WHITE);
//        g2d.setStroke(new BasicStroke(2));
//        g2d.drawRect(ix, iy, width, height);
//        
//        // 라벨
//        if (label != null) {
//            g2d.setFont(new Font("SansSerif", Font.BOLD, 12));
//            g2d.setColor(labelColor);
//            g2d.drawString(label, ix + 5, iy + height / 2);
//        }
//        
        // 상태 표시
        String stateText = getPlayerStateText();
        if (!stateText.isEmpty()) {
            g2d.drawString(stateText, ix + 5, iy + height / 2 + 15);
        }
    }
    
    private void drawObstacle(Graphics2D g2d, int ix, int iy) {
        if (destroyed) return;
        
        // 이미지 또는 색상
        if (image != null) {
            int displayWidth = (int)(width * 1.2);
            int displayHeight = (int)(height * 1.2);
            int offsetX = (width - displayWidth) / 2;
            int offsetY = (height - displayHeight) / 2;
            g2d.drawImage(image, ix + offsetX, iy + offsetY, displayWidth, displayHeight, null);
        } else {
            // 타입별 색상
            Color color = getObstacleColor();
            g2d.setColor(color);
            g2d.fillRect(ix, iy, width, height);
        }
//        
//        // 테두리
//        g2d.setColor(Color.WHITE);
//        g2d.setStroke(new BasicStroke(2));
//        g2d.drawRect(ix, iy, width, height);
//        
//        // 타입 라벨
//        if (label != null) {
//            g2d.setFont(new Font("SansSerif", Font.BOLD, 14));
//            g2d.setColor(labelColor);
//            
//            int textWidth = g2d.getFontMetrics().stringWidth(label);
//            int textX = ix + (width - textWidth) / 2;
//            int textY = iy + height / 2 + 5;
//            
//            g2d.drawString(label, textX, textY);
//        }
    }
    
    private void drawSkillRange(Graphics2D g2d, int ix, int iy) {
        // 반투명 색상 채우기
        if (fillColor != null) {
            g2d.setColor(fillColor);
            g2d.fillRect(ix, iy, width, height);
        }
        if (strokeColor != null) {
            g2d.setColor(strokeColor);
            
            if (dashed) {
                g2d.setStroke(new BasicStroke(strokeWidth, BasicStroke.CAP_BUTT, 
                    BasicStroke.JOIN_BEVEL, 0, new float[]{9}, 0));
            } else {
                g2d.setStroke(new BasicStroke(strokeWidth));
            }
            
            g2d.drawRect(ix, iy, width, height);
        }
//        
//        // 스킬 이름
//        if (label != null) {
//            g2d.setFont(new Font("SansSerif", Font.BOLD, 16));
//            
//            int textX = ix + width / 2 - label.length() * 4;
//            int textY = iy + height / 2 + 5;
//            
//            // 텍스트 배경
//            g2d.setColor(new Color(0, 0, 0, 150));
//            g2d.fillRect(textX - 5, textY - 15, label.length() * 9, 20);
//            
//            // 텍스트
//            g2d.setColor(labelColor);
//            g2d.drawString(label, textX, textY);
//        }
    }
    
    // DEBUG: 플레이어 상태 텍스트
    private String getPlayerStateText() {
        switch (state) {
            case 1: return "JUMP";
            case 2: return "SLIDE";
            case 3: return "ATK";
            default: return "";
        }
    }
    
    // 장애물 타입별 색상
    private Color getObstacleColor() {
        switch (subType) {
            case 0: return new Color(139, 69, 19);    // GROUND - 갈색
            case 1: return new Color(135, 206, 250);  // AIR - 하늘색
            case 2: return new Color(144, 238, 144);  // SOFT_MONSTER - 연두색
            case 3: return new Color(255, 69, 0);     // HARD_MONSTER - 빨간색
            case 4: return new Color(128, 0, 128);    // BOSS_MONSTER - 보라색
            default: return Color.GRAY;
        }
    }
    
    /**
     * 바운딩 박스 반환
     */
    public Rectangle getBounds() {
        return new Rectangle((int)x, (int)y, width, height);
    }
    
    /**
     * 다른 스프라이트와 충돌 체크
     */
    public boolean intersects(Sprite other) {
        return getBounds().intersects(other.getBounds());
    }
    
    // ========== Getters and Setters ==========
    
    public double getX() { return x; }
    public void setX(double x) { this.x = x; }
    
    public double getY() { return y; }
    public void setY(double y) { this.y = y; }
    
    public int getWidth() { return width; }
    public void setWidth(int width) { this.width = width; }
    
    public int getHeight() { return height; }
    public void setHeight(int height) { this.height = height; }
    
    public Image getImage() { return image; }
    public void setImage(Image image) { this.image = image; }
    
    public boolean isVisible() { return visible; }
    public void setVisible(boolean visible) { this.visible = visible; }
    
    public int getType() { return type; }
    
    public Color getFillColor() { return fillColor; }
    public void setFillColor(Color fillColor) { this.fillColor = fillColor; }
    
    public Color getStrokeColor() { return strokeColor; }
    public void setStrokeColor(Color strokeColor) { this.strokeColor = strokeColor; }
    
    public int getStrokeWidth() { return strokeWidth; }
    public void setStrokeWidth(int strokeWidth) { this.strokeWidth = strokeWidth; }
    
    public boolean isDashed() { return dashed; }
    public void setDashed(boolean dashed) { this.dashed = dashed; }
    
    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
    
    public Color getLabelColor() { return labelColor; }
    public void setLabelColor(Color labelColor) { this.labelColor = labelColor; }
    
    public int getSubType() { return subType; }
    public void setSubType(int subType) { this.subType = subType; }
    
    public int getState() { return state; }
    public void setState(int state) { this.state = state; }
    
    public boolean isDestroyed() { return destroyed; }
    public void setDestroyed(boolean destroyed) { this.destroyed = destroyed; }
    
    public boolean isInvincible() { return invincible; }
    public void setInvincible(boolean invincible) { this.invincible = invincible; }
}