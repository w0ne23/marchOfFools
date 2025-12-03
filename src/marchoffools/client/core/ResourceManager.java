package marchoffools.client.core;

import java.awt.Image;
import java.util.HashMap;
import java.util.Map;
import javax.swing.ImageIcon;

public class ResourceManager {
    
    private static final Map<String, Image> imageCache = new HashMap<>();
    
    // 게임 시작 전(로딩 화면 등)에 이 메서드를 한 번만 호출
    public static void loadAllResources() {
        try {
            loadImage("player", "/assets/testCharacter.png");
            loadImage("obstacle", "/assets/testObstacle.png");
            loadImage("enemy_eagle", "/assets/testEnemy2.png");
            
            System.out.println("All resources loaded successfully.");
        } catch (Exception e) {
            System.err.println("Resource loading failed: " + e.getMessage());
        }
    }
    
    // 실제 로딩 로직
    private static void loadImage(String key, String path) {
        try {
            Image img = new ImageIcon(ResourceManager.class.getResource(path)).getImage();
            if (img.getWidth(null) == -1) {
                System.err.println("Image not found: " + path);
            }
            imageCache.put(key, img);
        } catch (Exception e) {
            System.err.println("Error loading image " + path + ": " + e.getMessage());
        }
    }
    
    // 게임 객체들이 이미지를 요청할 때 사용
    public static Image getImage(String key) {
        return imageCache.get(key);
    }
}