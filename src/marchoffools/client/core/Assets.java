package marchoffools.client.core;

import java.awt.Color;
import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

public class Assets {

    public static class Backgrounds {
        public static final String DEFAULT  = "src/assets/testTitle.png";
    }
    
    public static class Sounds {
        public static final String BGM_LOBBY = "src/assets/sounds/bgm_lobby.wav";
//        public static final String BGM_GAME  = "src/assets/sounds/bgm_game.wav";
        
        // 효과음
//        public static final String SFX_CLICK = "src/assets/sounds/click.wav";
//        public static final String SFX_JUMP  = "src/assets/sounds/jump_01.wav";
//        public static final String SFX_SLIDE  = "src/assets/sounds/slide.wav";
//        public static final String SFX_ATTACK  = "src/assets/sounds/attack.wav";
//        public static final String SFX_SHIELD  = "src/assets/sounds/shield.wav";
//        public static final String SFX_SPECIAL  = "src/assets/sounds/special.wav";
//        public static final String SFX_INVINCIBLE  = "src/assets/sounds/invincible.wav";
    }

    public static class Colors {
        public static final Color TRANSPARENT = new Color(0, 0, 0, 0);
        
        public static final Color WHITE = new Color(255, 255, 255);
        public static final Color TRANSLUCENT_WHITE = new Color(255, 255, 255, 127);

        public static final Color BLACK = new Color(28, 27, 31);
        
        public static final Color GRAY = new Color(126, 126, 126);
        public static final Color LIGHT_GRAY = new Color(217, 217, 217);
        public static final Color DARK_GRAY = new Color(105, 105, 105);

        public static final Color BLUE = new Color(120, 170, 255); 
        public static final Color BLUE_HOVERED = new Color(130, 190, 255); 
        public static final Color BLUE_PRESSED = new Color(110, 150, 230); 
        
        public static final Color GREEN = new Color(46, 204, 113);
        public static final Color RED = new Color(231, 76, 60);
    }
    
    public static class Characters {
        private static final Map<String, ImageIcon> icons = new HashMap<>();

        public static ImageIcon get(String name) {
            return icons.get(name);
        }

        public static void load() {
            loadIcon("knight_warrior",  "src/assets/warrior.png"); 
            loadIcon("knight_archer", "src/assets/archer.png");
            loadIcon("knight_axe",   "src/assets/axe.png");

            loadIcon("horse_unicorn",  "src/assets/unicorn.png");
            loadIcon("horse_griffin",    "src/assets/griffin.png");
            loadIcon("horse_dragon", "src/assets/dragon.png");
        }
        
        private static void loadIcon(String key, String path) {
            try {
                File file = new File(path);
                if (file.exists()) {
                    Image img = ImageIO.read(file);
                    Image scaledImg = img.getScaledInstance(80, 80, Image.SCALE_SMOOTH);
                    icons.put(key, new ImageIcon(scaledImg));
                    System.out.println("이미지 로드 성공: " + key);
                } else {
                    System.err.println("이미지 파일을 찾을 수 없음: " + path);
                }
            } catch (IOException e) {
                System.err.println("이미지 로드 중 에러: " + path);
                e.printStackTrace();
            }
        }
    }
}