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
        public static final String DEFAULT  = "/assets/testTitle.png";
    }
    
    public static class Sounds {
        public static final String BGM_LOBBY = "/assets/sounds/bgm_lobby.wav";
//        public static final String BGM_GAME  = "/assets/sounds/bgm_game.wav";
        
        // 효과음
//        public static final String SFX_CLICK = "/assets/sounds/click.wav";
//        public static final String SFX_JUMP  = "/assets/sounds/jump_01.wav";
//        public static final String SFX_SLIDE  = "/assets/sounds/slide.wav";
//        public static final String SFX_ATTACK  = "/assets/sounds/attack.wav";
//        public static final String SFX_SHIELD  = "/assets/sounds/shield.wav";
//        public static final String SFX_SPECIAL  = "/assets/sounds/special.wav";
//        public static final String SFX_INVINCIBLE  = "/assets/sounds/invincible.wav";
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
        public static final String KNIGHT_WARRIOR = "/assets/warrior.png";
        public static final String KNIGHT_ARCHER = "/assets/archer.png";
        public static final String KNIGHT_AXE = "/assets/axe.png";
        
        public static final String HORSE_UNICORN = "/assets/unicorn.png";
        public static final String HORSE_GRIFFIN = "/assets/griffin.png";
        public static final String HORSE_DRAGON = "/assets/dragon.png";
    }
}